package com.dori.app.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface SyncGroupDao {
    @Query("SELECT * FROM sync_group WHERE id = 0")
    suspend fun get(): SyncGroup?

    @Upsert
    suspend fun upsert(group: SyncGroup)
}
