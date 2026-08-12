package com.dori.app.sync

import com.dori.app.data.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

private const val DISCOVERY_PORT = 47823
private const val SYNC_PORT = 47824
private const val BROADCAST_INTERVAL_MS = 5000L
private const val MIN_RESYNC_INTERVAL_MS = 4000L
private const val SOCKET_TIMEOUT_MS = 8000

private data class PeerInfo(val deviceId: String, val deviceName: String, val address: InetAddress, val syncPort: Int)

/**
 * LAN-only, no-account sync: discovers other Dori instances on the same
 * network via a UDP broadcast heartbeat, then exchanges "what changed since
 * we last talked" over a plain TCP + JSON request/response. No auth beyond
 * being reachable on the network - matches the app's no-cloud, no-accounts
 * design, at the cost of trusting the local network.
 */
class SyncEngine(private val repository: NoteRepository) {

    private val json = Json { ignoreUnknownKeys = true }
    private var engineScope: CoroutineScope? = null
    private val lastAttempt = mutableMapOf<String, Long>()

    fun start() {
        if (engineScope != null) return
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        engineScope = scope
        scope.launch { runServer() }
        scope.launch { runBroadcaster() }
        scope.launch { runListener(scope) }
    }

    fun stop() {
        engineScope?.cancel()
        engineScope = null
    }

    /** Accepts incoming sync requests from peers that discovered us. */
    private suspend fun runServer() {
        val identity = repository.getOrCreateDeviceIdentity()
        val server = try {
            ServerSocket(SYNC_PORT)
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
                socket.use { handleIncomingConnection(it, identity.deviceId, identity.deviceName) }
            }
        }
    }

    private suspend fun handleIncomingConnection(socket: Socket, myDeviceId: String, myDeviceName: String) {
        socket.soTimeout = SOCKET_TIMEOUT_MS
        val input = DataInputStream(socket.getInputStream())
        val output = DataOutputStream(socket.getOutputStream())

        val requestJson = readFramedMessage(input) ?: return
        val request = try {
            json.decodeFromString(SyncRequest.serializer(), requestJson)
        } catch (e: Exception) {
            return
        }

        // Labels/substances first: incoming notes/entries resolve their
        // labelUuid/substanceUuid against whatever already exists locally,
        // including ones just applied.
        request.labels.forEach { repository.applyRemoteLabel(it.toLabel()) }
        request.substances.forEach { repository.applyRemoteSubstance(it.toSubstance()) }
        request.notes.forEach { repository.applyRemoteNote(it.toNote()) }
        request.entries.forEach { repository.applyRemoteEntry(it.toEntry()) }
        repository.setPeerSyncedNow(request.fromDeviceId)

        val serverWatermarkNow = System.currentTimeMillis()
        val outgoingLabels = repository.getLabelsUpdatedSince(request.sinceWatermark).map { it.toDto() }
        val outgoingNotes = repository.getNotesUpdatedSince(request.sinceWatermark).map { it.toDto() }
        val outgoingSubstances = repository.getSubstancesUpdatedSince(request.sinceWatermark).map { it.toDto() }
        val outgoingEntries = repository.getEntriesUpdatedSince(request.sinceWatermark).map { it.toDto() }

        val response = SyncResponse(
            fromDeviceId = myDeviceId,
            fromDeviceName = myDeviceName,
            labels = outgoingLabels,
            notes = outgoingNotes,
            substances = outgoingSubstances,
            entries = outgoingEntries,
            serverWatermarkNow = serverWatermarkNow
        )
        writeFramedMessage(output, json.encodeToString(response))
    }

    /** Announces us on the LAN so other Dori instances can find us. */
    private suspend fun runBroadcaster() {
        val identity = repository.getOrCreateDeviceIdentity()
        val socket = try {
            DatagramSocket().apply { broadcast = true }
        } catch (e: Exception) {
            return
        }
        socket.use {
            val beacon = json.encodeToString(DiscoveryBeacon(identity.deviceId, identity.deviceName, SYNC_PORT))
            val bytes = beacon.toByteArray(Charsets.UTF_8)
            val target = InetAddress.getByName("255.255.255.255")
            while (currentCoroutineIsActive()) {
                try {
                    socket.send(DatagramPacket(bytes, bytes.size, target, DISCOVERY_PORT))
                } catch (e: Exception) {
                    // Network momentarily unavailable - just retry next tick.
                }
                delay(BROADCAST_INTERVAL_MS)
            }
        }
    }

    /** Listens for other devices' beacons and triggers a sync attempt when one is heard. */
    private suspend fun runListener(scope: CoroutineScope) {
        val identity = repository.getOrCreateDeviceIdentity()
        val socket = try {
            DatagramSocket(DISCOVERY_PORT)
        } catch (e: Exception) {
            return
        }
        socket.use {
            val buffer = ByteArray(1024)
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

                val now = System.currentTimeMillis()
                val last = lastAttempt[beacon.deviceId] ?: 0L
                if (now - last < MIN_RESYNC_INTERVAL_MS) continue
                lastAttempt[beacon.deviceId] = now

                val peer = PeerInfo(beacon.deviceId, beacon.deviceName, packet.address, beacon.syncPort)
                scope.launch { syncWithPeer(peer, identity.deviceId, identity.deviceName) }
            }
        }
    }

    private suspend fun syncWithPeer(peer: PeerInfo, myDeviceId: String, myDeviceName: String) {
        val myWatermark = repository.getPeerState(peer.deviceId)?.lastSyncedAt ?: 0L
        val outgoingLabels = repository.getLabelsUpdatedSince(myWatermark).map { it.toDto() }
        val outgoingNotes = repository.getNotesUpdatedSince(myWatermark).map { it.toDto() }
        val outgoingSubstances = repository.getSubstancesUpdatedSince(myWatermark).map { it.toDto() }
        val outgoingEntries = repository.getEntriesUpdatedSince(myWatermark).map { it.toDto() }
        val request = SyncRequest(
            fromDeviceId = myDeviceId,
            fromDeviceName = myDeviceName,
            sinceWatermark = myWatermark,
            labels = outgoingLabels,
            notes = outgoingNotes,
            substances = outgoingSubstances,
            entries = outgoingEntries
        )

        try {
            Socket().use { socket ->
                socket.soTimeout = SOCKET_TIMEOUT_MS
                withContext(Dispatchers.IO) { socket.connect(java.net.InetSocketAddress(peer.address, peer.syncPort), SOCKET_TIMEOUT_MS) }
                val output = DataOutputStream(socket.getOutputStream())
                val input = DataInputStream(socket.getInputStream())

                writeFramedMessage(output, json.encodeToString(request))
                val responseJson = readFramedMessage(input) ?: return
                val response = json.decodeFromString(SyncResponse.serializer(), responseJson)

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

    private fun writeFramedMessage(output: DataOutputStream, message: String) {
        val bytes = message.toByteArray(Charsets.UTF_8)
        output.writeInt(bytes.size)
        output.write(bytes)
        output.flush()
    }

    private fun readFramedMessage(input: DataInputStream): String? {
        return try {
            val length = input.readInt()
            if (length <= 0 || length > 10_000_000) return null
            val bytes = ByteArray(length)
            input.readFully(bytes)
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun currentCoroutineIsActive(): Boolean = kotlin.coroutines.coroutineContext[Job]?.isActive ?: true
}
