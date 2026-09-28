package com.dori.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table: the sync group this installation belongs to. Only devices
 * holding the same groupKey discover and sync with each other. Every install
 * starts in a group of its own; pairing makes the joining device adopt the
 * inviting device's group.
 */
@Entity(tableName = "sync_group")
data class SyncGroup(
    @PrimaryKey val id: Int = 0,
    val groupId: String,
    /** Base64 of 32 random bytes. */
    val groupKey: String
)
