package com.dori.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DeviceIdentityDao {
    @Query("SELECT * FROM device_identity WHERE id = 0")
    suspend fun get(): DeviceIdentity?

    @Insert
    suspend fun insert(identity: DeviceIdentity)
}
