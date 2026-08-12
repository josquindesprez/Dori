package com.dori.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** How far this device has already synced with each peer it has met. */
@Entity(tableName = "sync_peer_state")
data class SyncPeerState(
    @PrimaryKey val peerDeviceId: String,
    val lastSyncedAt: Long
)
