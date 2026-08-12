package com.dori.app.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SubstanceDao_Impl(
  __db: RoomDatabase,
) : SubstanceDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSubstance: EntityInsertAdapter<Substance>

  private val __updateAdapterOfSubstance: EntityDeleteOrUpdateAdapter<Substance>
  init {
    this.__db = __db
    this.__insertAdapterOfSubstance = object : EntityInsertAdapter<Substance>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `substances` (`id`,`name`,`unit`,`colorArgb`,`updatedAt`,`uuid`,`isDeleted`,`originDeviceId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Substance) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.unit)
        statement.bindLong(4, entity.colorArgb.toLong())
        statement.bindLong(5, entity.updatedAt)
        statement.bindText(6, entity.uuid)
        val _tmp: Int = if (entity.isDeleted) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        statement.bindText(8, entity.originDeviceId)
      }
    }
    this.__updateAdapterOfSubstance = object : EntityDeleteOrUpdateAdapter<Substance>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `substances` SET `id` = ?,`name` = ?,`unit` = ?,`colorArgb` = ?,`updatedAt` = ?,`uuid` = ?,`isDeleted` = ?,`originDeviceId` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Substance) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.unit)
        statement.bindLong(4, entity.colorArgb.toLong())
        statement.bindLong(5, entity.updatedAt)
        statement.bindText(6, entity.uuid)
        val _tmp: Int = if (entity.isDeleted) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        statement.bindText(8, entity.originDeviceId)
        statement.bindLong(9, entity.id)
      }
    }
  }

  public override suspend fun insert(substance: Substance): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfSubstance.insertAndReturnId(_connection, substance)
    _result
  }

  public override suspend fun update(substance: Substance): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfSubstance.handle(_connection, substance)
  }

  public override fun getAllSubstances(): Flow<List<Substance>> {
    val _sql: String = "SELECT * FROM substances WHERE isDeleted = 0 ORDER BY name COLLATE NOCASE ASC"
    return createFlow(__db, false, arrayOf("substances")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfColorArgb: Int = getColumnIndexOrThrow(_stmt, "colorArgb")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfOriginDeviceId: Int = getColumnIndexOrThrow(_stmt, "originDeviceId")
        val _result: MutableList<Substance> = mutableListOf()
        while (_stmt.step()) {
          val _item: Substance
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpColorArgb: Int
          _tmpColorArgb = _stmt.getLong(_columnIndexOfColorArgb).toInt()
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpIsDeleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp != 0
          val _tmpOriginDeviceId: String
          _tmpOriginDeviceId = _stmt.getText(_columnIndexOfOriginDeviceId)
          _item = Substance(_tmpId,_tmpName,_tmpUnit,_tmpColorArgb,_tmpUpdatedAt,_tmpUuid,_tmpIsDeleted,_tmpOriginDeviceId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getSubstanceByUuid(uuid: String): Substance? {
    val _sql: String = "SELECT * FROM substances WHERE uuid = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, uuid)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfColorArgb: Int = getColumnIndexOrThrow(_stmt, "colorArgb")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfOriginDeviceId: Int = getColumnIndexOrThrow(_stmt, "originDeviceId")
        val _result: Substance?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpColorArgb: Int
          _tmpColorArgb = _stmt.getLong(_columnIndexOfColorArgb).toInt()
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpIsDeleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp != 0
          val _tmpOriginDeviceId: String
          _tmpOriginDeviceId = _stmt.getText(_columnIndexOfOriginDeviceId)
          _result = Substance(_tmpId,_tmpName,_tmpUnit,_tmpColorArgb,_tmpUpdatedAt,_tmpUuid,_tmpIsDeleted,_tmpOriginDeviceId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getSubstancesUpdatedSince(sinceMillis: Long): List<Substance> {
    val _sql: String = "SELECT * FROM substances WHERE updatedAt > ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sinceMillis)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfColorArgb: Int = getColumnIndexOrThrow(_stmt, "colorArgb")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfOriginDeviceId: Int = getColumnIndexOrThrow(_stmt, "originDeviceId")
        val _result: MutableList<Substance> = mutableListOf()
        while (_stmt.step()) {
          val _item: Substance
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpColorArgb: Int
          _tmpColorArgb = _stmt.getLong(_columnIndexOfColorArgb).toInt()
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpIsDeleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp != 0
          val _tmpOriginDeviceId: String
          _tmpOriginDeviceId = _stmt.getText(_columnIndexOfOriginDeviceId)
          _item = Substance(_tmpId,_tmpName,_tmpUnit,_tmpColorArgb,_tmpUpdatedAt,_tmpUuid,_tmpIsDeleted,_tmpOriginDeviceId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun softDeleteById(
    id: Long,
    updatedAt: Long,
    originDeviceId: String,
  ) {
    val _sql: String = "UPDATE substances SET isDeleted = 1, updatedAt = ?, originDeviceId = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, updatedAt)
        _argIndex = 2
        _stmt.bindText(_argIndex, originDeviceId)
        _argIndex = 3
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
