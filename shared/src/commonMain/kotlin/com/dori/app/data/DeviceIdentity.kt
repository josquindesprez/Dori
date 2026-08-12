package com.dori.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single-row table: this installation's own stable identity for sync. */
@Entity(tableName = "device_identity")
data class DeviceIdentity(
    @PrimaryKey val id: Int = 0,
    val deviceId: String,
    val deviceName: String
)
