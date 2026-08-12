package com.dori.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE diaryDate IS NULL AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getLooseNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE diaryDate = :date AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getNotesForDate(date: LocalDate): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Long): Flow<Note?>

    @Query("SELECT * FROM notes WHERE uuid = :uuid LIMIT 1")
    suspend fun getNoteByUuid(uuid: String): Note?

    @Query("SELECT * FROM notes WHERE updatedAt > :sinceMillis")
    suspend fun getNotesUpdatedSince(sinceMillis: Long): List<Note>

    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    /** Permanent delete - only for notes that were never meaningfully saved (discarded while empty). */
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Tombstone delete - for anything that may already exist on a peer, so the deletion propagates. */
    @Query("UPDATE notes SET isDeleted = 1, updatedAt = :updatedAt, originDeviceId = :originDeviceId WHERE id = :id")
    suspend fun softDeleteById(id: Long, updatedAt: Long, originDeviceId: String)

    @Query("UPDATE notes SET labelId = NULL, labelUuid = NULL WHERE labelId = :labelId")
    suspend fun clearLabel(labelId: Long)
}
