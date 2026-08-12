package com.dori.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.util.UUID

class NoteRepository(
    private val noteDao: NoteDao,
    private val labelDao: LabelDao,
    private val deviceIdentityDao: DeviceIdentityDao,
    private val syncPeerStateDao: SyncPeerStateDao,
    private val substanceDao: SubstanceDao,
    private val substanceEntryDao: SubstanceEntryDao,
    private val deviceName: String
) {

    fun getLooseNotes(): Flow<List<Note>> = noteDao.getLooseNotes()

    fun getNotesForDate(date: LocalDate): Flow<List<Note>> = noteDao.getNotesForDate(date)

    fun getNoteById(id: Long): Flow<Note?> = noteDao.getNoteById(id)

    suspend fun insert(note: Note): Long = noteDao.insert(stampForWrite(note))

    suspend fun update(note: Note) = noteDao.update(stampForWrite(note))

    /** Permanent delete - only for notes that were never meaningfully saved (discarded while empty). */
    suspend fun deleteById(id: Long) = noteDao.deleteById(id)

    /** Tombstone delete - for anything that may already exist on a peer, so the deletion propagates. */
    suspend fun softDeleteNoteById(id: Long) {
        val deviceId = getOrCreateDeviceIdentity().deviceId
        noteDao.softDeleteById(id, System.currentTimeMillis(), deviceId)
    }

    fun getAllLabels(): Flow<List<Label>> = labelDao.getAllLabels()

    suspend fun insertLabel(label: Label): Long = labelDao.insert(stampForWrite(label))

    suspend fun updateLabel(label: Label) = labelDao.update(stampForWrite(label))

    suspend fun deleteLabel(label: Label) {
        val deviceId = getOrCreateDeviceIdentity().deviceId
        noteDao.clearLabel(label.id)
        labelDao.softDeleteById(label.id, System.currentTimeMillis(), deviceId)
    }

    // --- Substance tracker ----------------------------------------------

    fun getAllSubstances(): Flow<List<Substance>> = substanceDao.getAllSubstances()

    suspend fun insertSubstance(substance: Substance): Long = substanceDao.insert(stampForWrite(substance))

    suspend fun updateSubstance(substance: Substance) = substanceDao.update(stampForWrite(substance))

    /** Tombstones the substance and cascades to every entry that references it, so entries don't outlive it. */
    suspend fun deleteSubstance(substance: Substance) {
        val deviceId = getOrCreateDeviceIdentity().deviceId
        val now = System.currentTimeMillis()
        substanceEntryDao.softDeleteBySubstanceId(substance.id, now, deviceId)
        substanceDao.softDeleteById(substance.id, now, deviceId)
    }

    fun getEntriesBetween(startMillis: Long, endMillis: Long): Flow<List<SubstanceEntry>> =
        substanceEntryDao.getEntriesBetween(startMillis, endMillis)

    fun getEntryById(id: Long): Flow<SubstanceEntry?> = substanceEntryDao.getEntryById(id)

    suspend fun insertEntry(entry: SubstanceEntry): Long = substanceEntryDao.insert(stampForWrite(entry))

    suspend fun updateEntry(entry: SubstanceEntry) = substanceEntryDao.update(stampForWrite(entry))

    suspend fun softDeleteEntryById(id: Long) {
        val deviceId = getOrCreateDeviceIdentity().deviceId
        substanceEntryDao.softDeleteById(id, System.currentTimeMillis(), deviceId)
    }

    // --- Sync support -------------------------------------------------

    @Volatile
    private var cachedDeviceIdentity: DeviceIdentity? = null
    private val identityMutex = Mutex()

    /**
     * SyncEngine calls this from three concurrent coroutines (server,
     * broadcaster, listener) right at startup, all racing to resolve the
     * device identity before anything exists in the table yet - without the
     * mutex, a plain "check cache, then insert if missing" is not atomic
     * across coroutines and multiple inserts collide on the single-row
     * primary key.
     */
    suspend fun getOrCreateDeviceIdentity(): DeviceIdentity {
        cachedDeviceIdentity?.let { return it }
        return identityMutex.withLock {
            cachedDeviceIdentity?.let { return@withLock it }
            val identity = deviceIdentityDao.get()
                ?: DeviceIdentity(deviceId = UUID.randomUUID().toString(), deviceName = deviceName)
                    .also { deviceIdentityDao.insert(it) }
            cachedDeviceIdentity = identity
            identity
        }
    }

    /**
     * uuid is the row's stable identity and is only ever assigned once (kept
     * across edits); originDeviceId means "which device produced this exact
     * version" and is refreshed on every write, including updates - two
     * devices editing the same already-synced note at the exact same
     * millisecond need genuinely different originDeviceId values for the
     * last-write-wins tiebreak to converge instead of both sides deciding
     * "I win."
     */
    private suspend fun stampForWrite(note: Note): Note {
        val deviceId = getOrCreateDeviceIdentity().deviceId
        val uuid = note.uuid.ifBlank { UUID.randomUUID().toString() }
        return note.copy(uuid = uuid, originDeviceId = deviceId)
    }

    private suspend fun stampForWrite(label: Label): Label {
        val deviceId = getOrCreateDeviceIdentity().deviceId
        val uuid = label.uuid.ifBlank { UUID.randomUUID().toString() }
        return label.copy(uuid = uuid, originDeviceId = deviceId)
    }

    private suspend fun stampForWrite(substance: Substance): Substance {
        val deviceId = getOrCreateDeviceIdentity().deviceId
        val uuid = substance.uuid.ifBlank { UUID.randomUUID().toString() }
        return substance.copy(uuid = uuid, originDeviceId = deviceId)
    }

    private suspend fun stampForWrite(entry: SubstanceEntry): SubstanceEntry {
        val deviceId = getOrCreateDeviceIdentity().deviceId
        val uuid = entry.uuid.ifBlank { UUID.randomUUID().toString() }
        return entry.copy(uuid = uuid, originDeviceId = deviceId)
    }

    suspend fun getNotesUpdatedSince(sinceMillis: Long): List<Note> = noteDao.getNotesUpdatedSince(sinceMillis)

    suspend fun getLabelsUpdatedSince(sinceMillis: Long): List<Label> = labelDao.getLabelsUpdatedSince(sinceMillis)

    suspend fun getSubstancesUpdatedSince(sinceMillis: Long): List<Substance> = substanceDao.getSubstancesUpdatedSince(sinceMillis)

    suspend fun getEntriesUpdatedSince(sinceMillis: Long): List<SubstanceEntry> = substanceEntryDao.getEntriesUpdatedSince(sinceMillis)

    suspend fun getPeerState(peerDeviceId: String): SyncPeerState? = syncPeerStateDao.get(peerDeviceId)

    suspend fun setPeerSyncedNow(peerDeviceId: String) {
        syncPeerStateDao.upsert(SyncPeerState(peerDeviceId, System.currentTimeMillis()))
    }

    /** Merges an incoming label by last-write-wins on updatedAt (tiebreak: originDeviceId). */
    suspend fun applyRemoteLabel(remote: Label) {
        val local = labelDao.getLabelByUuid(remote.uuid)
        if (local == null) {
            labelDao.insert(remote.copy(id = 0))
        } else if (remoteWins(remote.updatedAt, remote.originDeviceId, local.updatedAt, local.originDeviceId)) {
            labelDao.update(remote.copy(id = local.id))
        }
    }

    /**
     * Merges an incoming note by last-write-wins on updatedAt (tiebreak:
     * originDeviceId), resolving its labelUuid to this device's local
     * labelId. Skips notes that are open in an editor right now (see
     * OpenEditorRegistry) so a pending local autosave can't clobber the
     * just-applied remote change - it'll be picked up on the next sync pass.
     */
    suspend fun applyRemoteNote(remote: Note) {
        val local = noteDao.getNoteByUuid(remote.uuid)
        if (local != null && OpenEditorRegistry.isOpen(local.id)) return

        val resolvedLabelId = remote.labelUuid?.let { labelDao.getLabelByUuid(it)?.id }
        if (local == null) {
            noteDao.insert(remote.copy(id = 0, labelId = resolvedLabelId))
        } else if (remoteWins(remote.updatedAt, remote.originDeviceId, local.updatedAt, local.originDeviceId)) {
            noteDao.update(remote.copy(id = local.id, labelId = resolvedLabelId))
        }
    }

    /** Merges an incoming substance by last-write-wins on updatedAt (tiebreak: originDeviceId). */
    suspend fun applyRemoteSubstance(remote: Substance) {
        val local = substanceDao.getSubstanceByUuid(remote.uuid)
        if (local == null) {
            substanceDao.insert(remote.copy(id = 0))
        } else if (remoteWins(remote.updatedAt, remote.originDeviceId, local.updatedAt, local.originDeviceId)) {
            substanceDao.update(remote.copy(id = local.id))
        }
    }

    /**
     * Merges an incoming entry by last-write-wins on updatedAt (tiebreak:
     * originDeviceId), resolving its substanceUuid to this device's local
     * substanceId. Unlike a note's optional label, an entry without a
     * resolvable substance isn't meaningful, so - unlike applyRemoteNote -
     * this skips the entry entirely rather than inserting it with a dangling
     * reference; it'll be retried once the substance itself has synced.
     */
    suspend fun applyRemoteEntry(remote: SubstanceEntry) {
        val resolvedSubstanceId = substanceDao.getSubstanceByUuid(remote.substanceUuid)?.id ?: return
        val local = substanceEntryDao.getEntryByUuid(remote.uuid)
        if (local == null) {
            substanceEntryDao.insert(remote.copy(id = 0, substanceId = resolvedSubstanceId))
        } else if (remoteWins(remote.updatedAt, remote.originDeviceId, local.updatedAt, local.originDeviceId)) {
            substanceEntryDao.update(remote.copy(id = local.id, substanceId = resolvedSubstanceId))
        }
    }

    private fun remoteWins(remoteUpdatedAt: Long, remoteDeviceId: String, localUpdatedAt: Long, localDeviceId: String): Boolean {
        if (remoteUpdatedAt != localUpdatedAt) return remoteUpdatedAt > localUpdatedAt
        return remoteDeviceId > localDeviceId
    }
}
