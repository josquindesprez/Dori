package com.dori.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubstanceEntryDao {

    @Query("SELECT * FROM substance_entries WHERE occurredAt >= :startMillis AND occurredAt < :endMillis AND isDeleted = 0 ORDER BY occurredAt DESC")
    fun getEntriesBetween(startMillis: Long, endMillis: Long): Flow<List<SubstanceEntry>>

    @Query("SELECT * FROM substance_entries WHERE id = :id")
    fun getEntryById(id: Long): Flow<SubstanceEntry?>

    @Query("SELECT * FROM substance_entries WHERE uuid = :uuid LIMIT 1")
    suspend fun getEntryByUuid(uuid: String): SubstanceEntry?

    @Query("SELECT * FROM substance_entries WHERE updatedAt > :sinceMillis")
    suspend fun getEntriesUpdatedSince(sinceMillis: Long): List<SubstanceEntry>

    @Insert
    suspend fun insert(entry: SubstanceEntry): Long

    @Update
    suspend fun update(entry: SubstanceEntry)

    @Query("UPDATE substance_entries SET isDeleted = 1, updatedAt = :updatedAt, originDeviceId = :originDeviceId WHERE id = :id")
    suspend fun softDeleteById(id: Long, updatedAt: Long, originDeviceId: String)

    /** Tombstones every entry belonging to a substance that's being deleted, so the deletions propagate. */
    @Query("UPDATE substance_entries SET isDeleted = 1, updatedAt = :updatedAt, originDeviceId = :originDeviceId WHERE substanceId = :substanceId")
    suspend fun softDeleteBySubstanceId(substanceId: Long, updatedAt: Long, originDeviceId: String)
}
