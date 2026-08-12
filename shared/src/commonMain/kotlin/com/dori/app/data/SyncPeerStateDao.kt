package com.dori.app.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface SyncPeerStateDao {
    @Query("SELECT * FROM sync_peer_state WHERE peerDeviceId = :peerDeviceId")
    suspend fun get(peerDeviceId: String): SyncPeerState?

    @Upsert
    suspend fun upsert(state: SyncPeerState)
}
