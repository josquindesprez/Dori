package com.dori.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {

    @Query("SELECT * FROM labels WHERE isDeleted = 0 ORDER BY name COLLATE NOCASE ASC")
    fun getAllLabels(): Flow<List<Label>>

    @Query("SELECT * FROM labels WHERE uuid = :uuid LIMIT 1")
    suspend fun getLabelByUuid(uuid: String): Label?

    @Query("SELECT * FROM labels WHERE updatedAt > :sinceMillis")
    suspend fun getLabelsUpdatedSince(sinceMillis: Long): List<Label>

    @Insert
    suspend fun insert(label: Label): Long

    @Update
    suspend fun update(label: Label)

    @Query("UPDATE labels SET isDeleted = 1, updatedAt = :updatedAt, originDeviceId = :originDeviceId WHERE id = :id")
    suspend fun softDeleteById(id: Long, updatedAt: Long, originDeviceId: String)
}
