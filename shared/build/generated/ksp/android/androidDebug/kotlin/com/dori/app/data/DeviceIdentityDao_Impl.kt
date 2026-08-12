package com.dori.app.`data`

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class DeviceIdentityDao_Impl(
  __db: RoomDatabase,
) : DeviceIdentityDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDeviceIdentity: EntityInsertAdapter<DeviceIdentity>
  init {
    this.__db = __db
    this.__insertAdapterOfDeviceIdentity = object : EntityInsertAdapter<DeviceIdentity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `device_identity` (`id`,`deviceId`,`deviceName`) VALUES (?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DeviceIdentity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.deviceId)
        statement.bindText(3, entity.deviceName)
      }
    }
  }

  public override suspend fun insert(identity: DeviceIdentity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDeviceIdentity.insert(_connection, identity)
  }

  public override suspend fun `get`(): DeviceIdentity? {
    val _sql: String = "SELECT * FROM device_identity WHERE id = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfDeviceName: Int = getColumnIndexOrThrow(_stmt, "deviceName")
        val _result: DeviceIdentity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpDeviceName: String
          _tmpDeviceName = _stmt.getText(_columnIndexOfDeviceName)
          _result = DeviceIdentity(_tmpId,_tmpDeviceId,_tmpDeviceName)
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
