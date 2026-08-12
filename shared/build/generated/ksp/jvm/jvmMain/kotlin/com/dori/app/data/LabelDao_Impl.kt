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
public class LabelDao_Impl(
  __db: RoomDatabase,
) : LabelDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfLabel: EntityInsertAdapter<Label>

  private val __updateAdapterOfLabel: EntityDeleteOrUpdateAdapter<Label>
  init {
    this.__db = __db
    this.__insertAdapterOfLabel = object : EntityInsertAdapter<Label>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `labels` (`id`,`name`,`colorArgb`,`updatedAt`,`uuid`,`isDeleted`,`originDeviceId`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Label) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.colorArgb.toLong())
        statement.bindLong(4, entity.updatedAt)
        statement.bindText(5, entity.uuid)
        val _tmp: Int = if (entity.isDeleted) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindText(7, entity.originDeviceId)
      }
    }
    this.__updateAdapterOfLabel = object : EntityDeleteOrUpdateAdapter<Label>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `labels` SET `id` = ?,`name` = ?,`colorArgb` = ?,`updatedAt` = ?,`uuid` = ?,`isDeleted` = ?,`originDeviceId` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Label) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.colorArgb.toLong())
        statement.bindLong(4, entity.updatedAt)
        statement.bindText(5, entity.uuid)
        val _tmp: Int = if (entity.isDeleted) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindText(7, entity.originDeviceId)
        statement.bindLong(8, entity.id)
      }
    }
  }

  public override suspend fun insert(label: Label): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfLabel.insertAndReturnId(_connection, label)
    _result
  }

  public override suspend fun update(label: Label): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfLabel.handle(_connection, label)
  }

  public override fun getAllLabels(): Flow<List<Label>> {
    val _sql: String = "SELECT * FROM labels WHERE isDeleted = 0 ORDER BY name COLLATE NOCASE ASC"
    return createFlow(__db, false, arrayOf("labels")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfColorArgb: Int = getColumnIndexOrThrow(_stmt, "colorArgb")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfOriginDeviceId: Int = getColumnIndexOrThrow(_stmt, "originDeviceId")
        val _result: MutableList<Label> = mutableListOf()
        while (_stmt.step()) {
          val _item: Label
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
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
          _item = Label(_tmpId,_tmpName,_tmpColorArgb,_tmpUpdatedAt,_tmpUuid,_tmpIsDeleted,_tmpOriginDeviceId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLabelByUuid(uuid: String): Label? {
    val _sql: String = "SELECT * FROM labels WHERE uuid = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, uuid)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfColorArgb: Int = getColumnIndexOrThrow(_stmt, "colorArgb")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfOriginDeviceId: Int = getColumnIndexOrThrow(_stmt, "originDeviceId")
        val _result: Label?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
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
          _result = Label(_tmpId,_tmpName,_tmpColorArgb,_tmpUpdatedAt,_tmpUuid,_tmpIsDeleted,_tmpOriginDeviceId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLabelsUpdatedSince(sinceMillis: Long): List<Label> {
    val _sql: String = "SELECT * FROM labels WHERE updatedAt > ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sinceMillis)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfColorArgb: Int = getColumnIndexOrThrow(_stmt, "colorArgb")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfOriginDeviceId: Int = getColumnIndexOrThrow(_stmt, "originDeviceId")
        val _result: MutableList<Label> = mutableListOf()
        while (_stmt.step()) {
          val _item: Label
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
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
          _item = Label(_tmpId,_tmpName,_tmpColorArgb,_tmpUpdatedAt,_tmpUuid,_tmpIsDeleted,_tmpOriginDeviceId)
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
    val _sql: String = "UPDATE labels SET isDeleted = 1, updatedAt = ?, originDeviceId = ? WHERE id = ?"
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
