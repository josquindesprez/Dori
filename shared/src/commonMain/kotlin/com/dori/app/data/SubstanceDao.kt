package com.dori.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubstanceDao {

    @Query("SELECT * FROM substances WHERE isDeleted = 0 ORDER BY name COLLATE NOCASE ASC")
    fun getAllSubstances(): Flow<List<Substance>>

    @Query("SELECT * FROM substances WHERE uuid = :uuid LIMIT 1")
    suspend fun getSubstanceByUuid(uuid: String): Substance?

    @Query("SELECT * FROM substances WHERE updatedAt > :sinceMillis")
    suspend fun getSubstancesUpdatedSince(sinceMillis: Long): List<Substance>

    @Insert
    suspend fun insert(substance: Substance): Long

    @Update
    suspend fun update(substance: Substance)

    @Query("UPDATE substances SET isDeleted = 1, updatedAt = :updatedAt, originDeviceId = :originDeviceId WHERE id = :id")
    suspend fun softDeleteById(id: Long, updatedAt: Long, originDeviceId: String)
}
