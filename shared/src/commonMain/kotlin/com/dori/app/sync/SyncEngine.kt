package com.dori.app.sync

import com.dori.app.data.NoteRepository
import com.dori.app.data.SyncGroup
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean

private const val DISCOVERY_PORT = 47823
private const val SYNC_PORT = 47824
private const val BROADCAST_INTERVAL_MS = 5000L
private const val PAIRING_BROADCAST_INTERVAL_MS = 1500L
private const val MIN_RESYNC_INTERVAL_MS = 4000L
private const val SOCKET_TIMEOUT_MS = 8000
private const val PAIRING_TIMEOUT_MS = 120_000
private const val INVITE_WINDOW_MS = 180_000L
private const val PAIRABLE_DEVICE_TTL_MS = 10_000L

private const val SYNC_MODE = "sync-v1"
private const val PAIR_MODE = "pair-v1"

private data class PeerInfo(val deviceId: String, val address: InetAddress, val syncPort: Int)

/** A nearby Dori that currently has "Add a device" open. */
data class PairableDevice(
    val deviceId: String,
    val deviceName: String,
    internal val address: InetAddress,
    internal val port: Int
)

sealed interface PairingState {
    data object Idle : PairingState
    /** This device is waiting for another one to join its group. */
    data object Inviting : PairingState
    /** This device is looking for an inviting device to join. */
    data class Joining(val devices: List<PairableDevice>) : PairingState
    data class Connecting(val peerName: String) : PairingState
    /** Both devices show [code]; the user must check they are identical. */
    data class ConfirmCode(val code: String, val peerName: String) : PairingState
    data class WaitingForPeer(val peerName: String) : PairingState
    data class Paired(val peerName: String) : PairingState
    data class Failed(val reason: String) : PairingState
}

/**
 * LAN-only, no-account sync between the devices of one sync group.
 *
 * Discovery: a UDP broadcast heartbeat carrying a tag only group members can
 * produce or check, so Dori ignores every device outside its group.
 *
 * Sync: plain TCP. After a hello exchanging fresh random nonces, everything
 * is AES-256-GCM encrypted with keys derived from the group key and both
 * nonces - a device without the group key can neither read nor inject data,
 * and old traffic can't be replayed.
 *
 * Pairing: the inviter and joiner do an ECDH exchange (the joiner commits to
 * its key first, so a man in the middle can't pick keys to fake a match) and
 * both show a 6-digit code derived from it. Only after the user confirms the
 * codes match on both devices does the inviter send the group key over the
 * encrypted link.
 */
class SyncEngine(
    private val repository: NoteRepository,
    /** Overridable so tests can run several engines on one machine. */
    private val syncPort: Int = SYNC_PORT
) {

    private val json = Json { ignoreUnknownKeys = true }
    private var engineScope: CoroutineScope? = null
    private val lastAttempt = mutableMapOf<String, Long>()

    private val _pairingState = MutableStateFlow<PairingState>(PairingState.Idle)
    val pairingState: StateFlow<PairingState> = _pairingState.asStateFlow()

    @Volatile
    private var inviteOpenUntil = 0L
    private val pairingBusy = AtomicBoolean(false)
    private val pairableDevices = mutableMapOf<String, Pair<PairableDevice, Long>>()

    @Volatile
    private var pendingConfirmation: CompletableDeferred<Boolean>? = null

    @Volatile
    private var activePairingSocket: Socket? = null

    fun start() {
        if (engineScope != null) return
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        engineScope = scope
        scope.launch { runServer(scope) }
        scope.launch { runBroadcaster() }
        scope.launch { runListener(scope) }
    }

    fun stop() {
        engineScope?.cancel()
        engineScope = null
    }

    // --- Pairing controls (called from UI) -------------------------------

    /** Opens this device's group to one joiner for a few minutes. */
    fun startInviting() {
        val scope = engineScope ?: return
        if (pairingBusy.get()) return
        val until = System.currentTimeMillis() + INVITE_WINDOW_MS
        inviteOpenUntil = until
        _pairingState.value = PairingState.Inviting
        scope.launch {
            delay(INVITE_WINDOW_MS)
            if (inviteOpenUntil == until && _pairingState.value == PairingState.Inviting) {
                inviteOpenUntil = 0
                _pairingState.value = PairingState.Failed("No device joined in time.")
            }
        }
    }

    /** Starts listing nearby devices that are inviting. */
    fun startJoining() {
        if (pairingBusy.get()) return
        synchronized(pairableDevices) { pairableDevices.clear() }
        _pairingState.value = PairingState.Joining(emptyList())
    }

    fun joinWith(device: PairableDevice) {
        val scope = engineScope ?: return
        if (!pairingBusy.compareAndSet(false, true)) return
        scope.launch {
            try {
                runJoin(device)
            } finally {
                activePairingSocket = null
                pairingBusy.set(false)
            }
        }
    }

    /** The user's answer to "do both devices show the same code?". */
    fun confirmCode(matches: Boolean) {
        pendingConfirmation?.complete(matches)
    }

    fun cancelPairing() {
        inviteOpenUntil = 0
        pendingConfirmation?.complete(false)
        try {
            activePairingSocket?.close()
        } catch (e: Exception) {
        }
        _pairingState.value = PairingState.Idle
    }

    /** Clears a finished (paired or failed) state. */
    fun dismissPairingResult() {
        val state = _pairingState.value
        if (state is PairingState.Paired || state is PairingState.Failed) _pairingState.value = PairingState.Idle
    }

    // --- Server -----------------------------------------------------------

    /** Accepts incoming sync and pairing connections. */
    private suspend fun runServer(scope: CoroutineScope) {
        val server = try {
            ServerSocket(syncPort)
        } catch (e: Exception) {
            return
        }
        server.use {
            while (currentCoroutineIsActive()) {
                val socket = try {
                    withContext(Dispatchers.IO) { server.accept() }
                } catch (e: Exception) {
                    break
                }
                // One coroutine per connection: a pairing waits minutes for
                // the user and must not hold up regular syncs meanwhile.
                scope.launch {
                    socket.use {
                        try {
                            handleIncomingConnection(it)
                        } catch (e: Exception) {
                            // Malformed or hostile peer - drop the connection.
                        }
                    }
                }
            }
        }
    }

    private suspend fun handleIncomingConnection(socket: Socket) {
        socket.soTimeout = SOCKET_TIMEOUT_MS
        val input = DataInputStream(socket.getInputStream())
        val output = DataOutputStream(socket.getOutputStream())
        val hello = readJson(input, ClientHello.serializer()) ?: return
        when (hello.mode) {
            SYNC_MODE -> serveSync(hello, input, output)
            PAIR_MODE -> servePairing(socket, hello, input, output)
        }
    }

    private suspend fun serveSync(hello: ClientHello, input: DataInputStream, output: DataOutputStream) {
        val identity = repository.getOrCreateDeviceIdentity()
        val group = repository.getOrCreateSyncGroup()
        val clientNonce = SyncCrypto.unb64(hello.nonce)
        if (clientNonce.size != 32) return
        val serverNonce = SyncCrypto.randomBytes(32)
        writeJson(output, ServerHello.serializer(), ServerHello(deviceId = identity.deviceId, nonce = SyncCrypto.b64(serverNonce)))

        val channel = syncChannel(group, hello.deviceId, clientNonce, identity.deviceId, serverNonce, isClient = false)
        // Fails to decrypt unless the peer holds our group key.
        val request = readSealed(input, channel, SyncRequest.serializer()) ?: return
        if (request.fromDeviceId != hello.deviceId) return

        // Labels/substances first: incoming notes/entries resolve their
        // labelUuid/substanceUuid against whatever already exists locally,
        // including ones just applied.
        request.labels.forEach { repository.applyRemoteLabel(it.toLabel()) }
        request.substances.forEach { repository.applyRemoteSubstance(it.toSubstance()) }
        request.notes.forEach { repository.applyRemoteNote(it.toNote()) }
        request.entries.forEach { repository.applyRemoteEntry(it.toEntry()) }
        repository.setPeerSyncedNow(request.fromDeviceId)

        val serverWatermarkNow = System.currentTimeMillis()
        val response = SyncResponse(
            fromDeviceId = identity.deviceId,
            fromDeviceName = identity.deviceName,
            labels = repository.getLabelsUpdatedSince(request.sinceWatermark).map { it.toDto() },
            notes = repository.getNotesUpdatedSince(request.sinceWatermark).map { it.toDto() },
            substances = repository.getSubstancesUpdatedSince(request.sinceWatermark).map { it.toDto() },
            entries = repository.getEntriesUpdatedSince(request.sinceWatermark).map { it.toDto() },
            serverWatermarkNow = serverWatermarkNow
        )
        writeSealed(output, channel, SyncResponse.serializer(), response)
    }

    /** Inviter side of pairing. */
    private suspend fun servePairing(socket: Socket, hello: ClientHello, input: DataInputStream, output: DataOutputStream) {
        if (System.currentTimeMillis() > inviteOpenUntil) return
        if (!pairingBusy.compareAndSet(false, true)) return
        inviteOpenUntil = 0 // one joiner per invite
        activePairingSocket = socket
        val peerName = hello.deviceName.ifBlank { "another device" }
        try {
            socket.soTimeout = PAIRING_TIMEOUT_MS
            _pairingState.value = PairingState.Connecting(peerName)
            val identity = repository.getOrCreateDeviceIdentity()
            val group = repository.getOrCreateSyncGroup()
            val commitment = SyncCrypto.unb64(hello.commitment)
            if (commitment.size != 32) return fail("The other device sent an invalid request.")

            val myKeys = SyncCrypto.generateEcKeyPair()
            val myPublic = myKeys.public.encoded
            writeJson(
                output, ServerHello.serializer(),
                ServerHello(deviceId = identity.deviceId, deviceName = repository.deviceName, publicKey = SyncCrypto.b64(myPublic))
            )
            val reveal = readJson(input, PairReveal.serializer()) ?: return fail("The other device stopped responding.")
            val peerPublic = SyncCrypto.unb64(reveal.publicKey)
            if (!MessageDigest.isEqual(SyncCrypto.sha256(peerPublic), commitment)) {
                return fail("The other device's key didn't match its commitment.")
            }

            val secret = SyncCrypto.ecdh(myKeys, peerPublic)
            val transcript = pairingTranscript(hello.deviceId, commitment, identity.deviceId, myPublic, peerPublic)
            val channel = SecureChannel.derive(secret, transcript, "dori-pair-v1", isClient = false)
            val code = SyncCrypto.shortAuthString(secret, transcript)

            if (!awaitUserConfirmation(code, peerName)) return cancelled()
            _pairingState.value = PairingState.WaitingForPeer(peerName)
            writeSealed(output, channel, PairGrant.serializer(), PairGrant(group.groupId, group.groupKey))

            val ack = readSealed(input, channel, PairAck.serializer())
            _pairingState.value = if (ack?.accepted == true) {
                PairingState.Paired(peerName)
            } else {
                PairingState.Failed("Pairing was cancelled on $peerName.")
            }
        } catch (e: Exception) {
            if (_pairingState.value != PairingState.Idle) fail("Pairing failed: connection lost.")
        } finally {
            activePairingSocket = null
            pairingBusy.set(false)
        }
    }

    // --- Joiner -----------------------------------------------------------

    private suspend fun runJoin(device: PairableDevice) {
        val peerName = device.deviceName.ifBlank { "the other device" }
        _pairingState.value = PairingState.Connecting(peerName)
        try {
            Socket().use { socket ->
                activePairingSocket = socket
                socket.soTimeout = PAIRING_TIMEOUT_MS
                withContext(Dispatchers.IO) { socket.connect(InetSocketAddress(device.address, device.port), SOCKET_TIMEOUT_MS) }
                val input = DataInputStream(socket.getInputStream())
                val output = DataOutputStream(socket.getOutputStream())
                val identity = repository.getOrCreateDeviceIdentity()

                val myKeys = SyncCrypto.generateEcKeyPair()
                val myPublic = myKeys.public.encoded
                val commitment = SyncCrypto.sha256(myPublic)
                writeJson(
                    output, ClientHello.serializer(),
                    ClientHello(
                        mode = PAIR_MODE,
                        deviceId = identity.deviceId,
                        deviceName = repository.deviceName,
                        commitment = SyncCrypto.b64(commitment)
                    )
                )
                val serverHello = readJson(input, ServerHello.serializer())
                    ?: return fail("$peerName is no longer inviting.")
                val peerPublic = SyncCrypto.unb64(serverHello.publicKey)
                writeJson(output, PairReveal.serializer(), PairReveal(SyncCrypto.b64(myPublic)))

                val secret = SyncCrypto.ecdh(myKeys, peerPublic)
                val transcript = pairingTranscript(identity.deviceId, commitment, serverHello.deviceId, peerPublic, myPublic)
                val channel = SecureChannel.derive(secret, transcript, "dori-pair-v1", isClient = true)
                val code = SyncCrypto.shortAuthString(secret, transcript)

                if (!awaitUserConfirmation(code, peerName)) {
                    try {
                        writeSealed(output, channel, PairAck.serializer(), PairAck(false))
                    } catch (e: Exception) {
                    }
                    return cancelled()
                }
                _pairingState.value = PairingState.WaitingForPeer(peerName)
                val grant = readSealed(input, channel, PairGrant.serializer())
                    ?: return fail("Pairing was cancelled on $peerName.")
                SyncCrypto.unb64(grant.groupKey).also { if (it.size != 32) return fail("$peerName sent an invalid key.") }
                repository.joinSyncGroup(SyncGroup(groupId = grant.groupId, groupKey = grant.groupKey))
                writeSealed(output, channel, PairAck.serializer(), PairAck(true))
                _pairingState.value = PairingState.Paired(peerName)
            }
        } catch (e: Exception) {
            if (_pairingState.value != PairingState.Idle) fail("Couldn't reach $peerName.")
        }
    }

    private suspend fun awaitUserConfirmation(code: String, peerName: String): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        pendingConfirmation = deferred
        _pairingState.value = PairingState.ConfirmCode(code, peerName)
        return try {
            withTimeoutOrNull(PAIRING_TIMEOUT_MS.toLong()) { deferred.await() } ?: false
        } finally {
            pendingConfirmation = null
        }
    }

    private fun fail(reason: String) {
        _pairingState.value = PairingState.Failed(reason)
    }

    private fun cancelled() {
        if (_pairingState.value !is PairingState.Idle) _pairingState.value = PairingState.Failed("Pairing cancelled.")
    }

    // --- Discovery --------------------------------------------------------

    /** Announces us on the LAN so other devices of our group can find us. */
    private suspend fun runBroadcaster() {
        val identity = repository.getOrCreateDeviceIdentity()
        val socket = try {
            DatagramSocket().apply { broadcast = true }
        } catch (e: Exception) {
            return
        }
        socket.use {
            val target = InetAddress.getByName("255.255.255.255")
            while (currentCoroutineIsActive()) {
                // Rebuilt every tick: joining a group changes the tag, and
                // the pairing flag comes and goes.
                val group = repository.getOrCreateSyncGroup()
                val inviting = System.currentTimeMillis() < inviteOpenUntil
                val beacon = DiscoveryBeacon(
                    deviceId = identity.deviceId,
                    deviceName = if (inviting) repository.deviceName else "",
                    syncPort = syncPort,
                    groupTag = SyncCrypto.beaconTag(SyncCrypto.unb64(group.groupKey), identity.deviceId),
                    pairingOpen = inviting
                )
                val bytes = json.encodeToString(DiscoveryBeacon.serializer(), beacon).toByteArray(Charsets.UTF_8)
                try {
                    socket.send(DatagramPacket(bytes, bytes.size, target, DISCOVERY_PORT))
                } catch (e: Exception) {
                    // Network momentarily unavailable - just retry next tick.
                }
                delay(if (inviting) PAIRING_BROADCAST_INTERVAL_MS else BROADCAST_INTERVAL_MS)
            }
        }
    }

    /** Listens for beacons: syncs with group members, lists inviting devices while joining. */
    private suspend fun runListener(scope: CoroutineScope) {
        val identity = repository.getOrCreateDeviceIdentity()
        val socket = try {
            // Shared so a second Dori on the same machine (or a test) still hears beacons.
            DatagramSocket(null).apply {
                reuseAddress = true
                bind(InetSocketAddress(DISCOVERY_PORT))
            }
        } catch (e: Exception) {
            return
        }
        socket.use {
            val buffer = ByteArray(2048)
            while (currentCoroutineIsActive()) {
                val packet = DatagramPacket(buffer, buffer.size)
                try {
                    withContext(Dispatchers.IO) { socket.receive(packet) }
                } catch (e: Exception) {
                    continue
                }
                val text = String(packet.data, 0, packet.length, Charsets.UTF_8)
                val beacon = try {
                    json.decodeFromString(DiscoveryBeacon.serializer(), text)
                } catch (e: Exception) {
                    continue
                }
                if (beacon.deviceId == identity.deviceId) continue

                if (beacon.pairingOpen) noteInvitingDevice(beacon, packet.address)

                val group = repository.getOrCreateSyncGroup()
                if (!SyncCrypto.beaconTagMatches(SyncCrypto.unb64(group.groupKey), beacon.deviceId, beacon.groupTag)) continue

                val now = System.currentTimeMillis()
                val last = lastAttempt[beacon.deviceId] ?: 0L
                if (now - last < MIN_RESYNC_INTERVAL_MS) continue
                lastAttempt[beacon.deviceId] = now

                val peer = PeerInfo(beacon.deviceId, packet.address, beacon.syncPort)
                scope.launch { syncWithPeer(peer, identity.deviceId, identity.deviceName) }
            }
        }
    }

    private fun noteInvitingDevice(beacon: DiscoveryBeacon, address: InetAddress) {
        if (_pairingState.value !is PairingState.Joining) return
        val now = System.currentTimeMillis()
        val devices = synchronized(pairableDevices) {
            pairableDevices[beacon.deviceId] = PairableDevice(beacon.deviceId, beacon.deviceName, address, beacon.syncPort) to now
            pairableDevices.entries.removeAll { now - it.value.second > PAIRABLE_DEVICE_TTL_MS }
            pairableDevices.values.map { it.first }.sortedBy { it.deviceName }
        }
        // Re-check: the user may have picked a device or cancelled meanwhile.
        if (_pairingState.value is PairingState.Joining) _pairingState.value = PairingState.Joining(devices)
    }

    // --- Sync client ------------------------------------------------------

    private suspend fun syncWithPeer(peer: PeerInfo, myDeviceId: String, myDeviceName: String) {
        val group = repository.getOrCreateSyncGroup()
        val myWatermark = repository.getPeerState(peer.deviceId)?.lastSyncedAt ?: 0L
        val request = SyncRequest(
            fromDeviceId = myDeviceId,
            fromDeviceName = myDeviceName,
            sinceWatermark = myWatermark,
            labels = repository.getLabelsUpdatedSince(myWatermark).map { it.toDto() },
            notes = repository.getNotesUpdatedSince(myWatermark).map { it.toDto() },
            substances = repository.getSubstancesUpdatedSince(myWatermark).map { it.toDto() },
            entries = repository.getEntriesUpdatedSince(myWatermark).map { it.toDto() }
        )

        try {
            Socket().use { socket ->
                socket.soTimeout = SOCKET_TIMEOUT_MS
                withContext(Dispatchers.IO) { socket.connect(InetSocketAddress(peer.address, peer.syncPort), SOCKET_TIMEOUT_MS) }
                val output = DataOutputStream(socket.getOutputStream())
                val input = DataInputStream(socket.getInputStream())

                val clientNonce = SyncCrypto.randomBytes(32)
                writeJson(output, ClientHello.serializer(), ClientHello(mode = SYNC_MODE, deviceId = myDeviceId, nonce = SyncCrypto.b64(clientNonce)))
                val serverHello = readJson(input, ServerHello.serializer()) ?: return
                // The beacon was authenticated for this id; the reply must come from the same device.
                if (serverHello.deviceId != peer.deviceId) return
                val serverNonce = SyncCrypto.unb64(serverHello.nonce)
                if (serverNonce.size != 32) return

                val channel = syncChannel(group, myDeviceId, clientNonce, peer.deviceId, serverNonce, isClient = true)
                writeSealed(output, channel, SyncRequest.serializer(), request)
                val response = readSealed(input, channel, SyncResponse.serializer()) ?: return

                response.labels.forEach { repository.applyRemoteLabel(it.toLabel()) }
                response.substances.forEach { repository.applyRemoteSubstance(it.toSubstance()) }
                response.notes.forEach { repository.applyRemoteNote(it.toNote()) }
                response.entries.forEach { repository.applyRemoteEntry(it.toEntry()) }
                repository.setPeerSyncedNow(peer.deviceId)
            }
        } catch (e: Exception) {
            // Peer went offline mid-exchange, or network hiccup - next
            // beacon will trigger another attempt, nothing to do here.
        }
    }

    // --- Framing & crypto plumbing ----------------------------------------

    private fun syncChannel(
        group: SyncGroup,
        clientId: String,
        clientNonce: ByteArray,
        serverId: String,
        serverNonce: ByteArray,
        isClient: Boolean
    ): SecureChannel {
        val transcript = SyncCrypto.sha256(clientId.toByteArray(Charsets.UTF_8), clientNonce, serverId.toByteArray(Charsets.UTF_8), serverNonce)
        return SecureChannel.derive(SyncCrypto.unb64(group.groupKey), transcript, "dori-sync-v1", isClient)
    }

    private fun pairingTranscript(
        joinerId: String,
        commitment: ByteArray,
        inviterId: String,
        inviterPublic: ByteArray,
        joinerPublic: ByteArray
    ): ByteArray = SyncCrypto.sha256(
        joinerId.toByteArray(Charsets.UTF_8),
        commitment,
        inviterId.toByteArray(Charsets.UTF_8),
        inviterPublic,
        joinerPublic
    )

    private fun <T> writeJson(output: DataOutputStream, serializer: KSerializer<T>, value: T) =
        writeFramedMessage(output, json.encodeToString(serializer, value).toByteArray(Charsets.UTF_8))

    private fun <T> readJson(input: DataInputStream, serializer: KSerializer<T>): T? {
        val bytes = readFramedMessage(input) ?: return null
        return try {
            json.decodeFromString(serializer, String(bytes, Charsets.UTF_8))
        } catch (e: Exception) {
            null
        }
    }

    private fun <T> writeSealed(output: DataOutputStream, channel: SecureChannel, serializer: KSerializer<T>, value: T) =
        writeFramedMessage(output, channel.seal(json.encodeToString(serializer, value).toByteArray(Charsets.UTF_8)))

    /** Null if the peer went away, or the message wasn't sealed with this channel's key. */
    private fun <T> readSealed(input: DataInputStream, channel: SecureChannel, serializer: KSerializer<T>): T? {
        val sealed = readFramedMessage(input) ?: return null
        return try {
            json.decodeFromString(serializer, String(channel.open(sealed), Charsets.UTF_8))
        } catch (e: Exception) {
            null
        }
    }

    private fun writeFramedMessage(output: DataOutputStream, bytes: ByteArray) {
        output.writeInt(bytes.size)
        output.write(bytes)
        output.flush()
    }

    private fun readFramedMessage(input: DataInputStream): ByteArray? {
        return try {
            val length = input.readInt()
            if (length <= 0 || length > 10_000_000) return null
            val bytes = ByteArray(length)
            input.readFully(bytes)
            bytes
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun currentCoroutineIsActive(): Boolean = kotlin.coroutines.coroutineContext[Job]?.isActive ?: true
}
