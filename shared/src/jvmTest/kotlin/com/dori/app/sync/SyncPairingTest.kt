package com.dori.app.sync

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.dori.app.data.AppDatabase
import com.dori.app.data.Note
import com.dori.app.data.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Runs real engines over the loopback/LAN broadcast of the test machine: pairs
 * two devices, checks a note syncs between them, and that a third, unpaired
 * device on the same network gets nothing.
 */
class SyncPairingTest {
    private val engines = mutableListOf<SyncEngine>()

    private fun device(name: String, port: Int): Pair<NoteRepository, SyncEngine> {
        val db = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        val repository = NoteRepository(
            noteDao = db.noteDao(),
            labelDao = db.labelDao(),
            deviceIdentityDao = db.deviceIdentityDao(),
            syncPeerStateDao = db.syncPeerStateDao(),
            substanceDao = db.substanceDao(),
            substanceEntryDao = db.substanceEntryDao(),
            syncGroupDao = db.syncGroupDao(),
            deviceName = name
        )
        val engine = SyncEngine(repository, syncPort = port).apply { start() }
        engines += engine
        return repository to engine
    }

    @AfterTest
    fun tearDown() = engines.forEach { it.stop() }

    private suspend fun <T : PairingState> SyncEngine.awaitState(type: Class<T>): T =
        withTimeout(20_000) { pairingState.first { type.isInstance(it) } }.let(type::cast)

    @Test
    fun pairedDevicesSyncAndStrangersDont() = runBlocking {
        val (phoneRepo, phone) = device("phone", 48824)
        val (laptopRepo, laptop) = device("laptop", 48825)
        val (strangerRepo, _) = device("stranger", 48826)

        assertNotEquals(phoneRepo.getOrCreateSyncGroup().groupKey, laptopRepo.getOrCreateSyncGroup().groupKey)

        phone.startInviting()
        laptop.startJoining()
        val joining = withTimeout(20_000) {
            laptop.pairingState.first { it is PairingState.Joining && it.devices.any { d -> d.deviceName == "phone" } }
        } as PairingState.Joining
        laptop.joinWith(joining.devices.first { it.deviceName == "phone" })

        val phoneCode = phone.awaitState(PairingState.ConfirmCode::class.java)
        val laptopCode = laptop.awaitState(PairingState.ConfirmCode::class.java)
        assertEquals(phoneCode.code, laptopCode.code)
        assertEquals("laptop", phoneCode.peerName)
        phone.confirmCode(true)
        laptop.confirmCode(true)
        phone.awaitState(PairingState.Paired::class.java)
        laptop.awaitState(PairingState.Paired::class.java)

        assertEquals(phoneRepo.getOrCreateSyncGroup(), laptopRepo.getOrCreateSyncGroup())
        assertNotEquals(phoneRepo.getOrCreateSyncGroup().groupKey, strangerRepo.getOrCreateSyncGroup().groupKey)

        phoneRepo.insert(Note(title = "secret", body = "only for my devices"))
        withTimeout(30_000) {
            while (laptopRepo.getLooseNotes().first().none { it.title == "secret" }) delay(250)
        }
        // The stranger has heard the same beacons for as long; give it one more round.
        delay(6_000)
        assertTrue(strangerRepo.getLooseNotes().first().isEmpty())
    }

    @Test
    fun mismatchedCodeRejectionKeepsGroupsApart() = runBlocking {
        val (aRepo, a) = device("a", 48834)
        val (bRepo, b) = device("b", 48835)
        val groupB = bRepo.getOrCreateSyncGroup()

        a.startInviting()
        b.startJoining()
        val joining = withTimeout(20_000) {
            b.pairingState.first { it is PairingState.Joining && it.devices.any { d -> d.deviceName == "a" } }
        } as PairingState.Joining
        b.joinWith(joining.devices.first { it.deviceName == "a" })
        b.awaitState(PairingState.ConfirmCode::class.java)
        a.awaitState(PairingState.ConfirmCode::class.java)

        b.confirmCode(true)
        a.confirmCode(false) // user on the inviter says the codes differ

        assertIs<PairingState.Failed>(b.awaitState(PairingState.Failed::class.java))
        assertEquals(groupB, bRepo.getOrCreateSyncGroup())
        assertNotEquals(aRepo.getOrCreateSyncGroup().groupKey, bRepo.getOrCreateSyncGroup().groupKey)
    }
}
