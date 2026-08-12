package com.dori.app.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SyncPeerStateDao_Impl(
  __db: RoomDatabase,
) : SyncPeerStateDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfSyncPeerState: EntityUpsertAdapter<SyncPeerState>
  init {
    this.__db = __db
    this.__upsertAdapterOfSyncPeerState = EntityUpsertAdapter<SyncPeerState>(object : EntityInsertAdapter<SyncPeerState>() {
      protected override fun createQuery(): String = "INSERT INTO `sync_peer_state` (`peerDeviceId`,`lastSyncedAt`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SyncPeerState) {
        statement.bindText(1, entity.peerDeviceId)
        statement.bindLong(2, entity.lastSyncedAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<SyncPeerState>() {
      protected override fun createQuery(): String = "UPDATE `sync_peer_state` SET `peerDeviceId` = ?,`lastSyncedAt` = ? WHERE `peerDeviceId` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SyncPeerState) {
        statement.bindText(1, entity.peerDeviceId)
        statement.bindLong(2, entity.lastSyncedAt)
        statement.bindText(3, entity.peerDeviceId)
      }
    })
  }

  public override suspend fun upsert(state: SyncPeerState): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfSyncPeerState.upsert(_connection, state)
  }

  public override suspend fun `get`(peerDeviceId: String): SyncPeerState? {
    val _sql: String = "SELECT * FROM sync_peer_state WHERE peerDeviceId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, peerDeviceId)
        val _columnIndexOfPeerDeviceId: Int = getColumnIndexOrThrow(_stmt, "peerDeviceId")
        val _columnIndexOfLastSyncedAt: Int = getColumnIndexOrThrow(_stmt, "lastSyncedAt")
        val _result: SyncPeerState?
        if (_stmt.step()) {
          val _tmpPeerDeviceId: String
          _tmpPeerDeviceId = _stmt.getText(_columnIndexOfPeerDeviceId)
          val _tmpLastSyncedAt: Long
          _tmpLastSyncedAt = _stmt.getLong(_columnIndexOfLastSyncedAt)
          _result = SyncPeerState(_tmpPeerDeviceId,_tmpLastSyncedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
