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
import kotlin.Double
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
public class SubstanceEntryDao_Impl(
  __db: RoomDatabase,
) : SubstanceEntryDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSubstanceEntry: EntityInsertAdapter<SubstanceEntry>

  private val __updateAdapterOfSubstanceEntry: EntityDeleteOrUpdateAdapter<SubstanceEntry>
  init {
    this.__db = __db
    this.__insertAdapterOfSubstanceEntry = object : EntityInsertAdapter<SubstanceEntry>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `substance_entries` (`id`,`substanceId`,`quantity`,`price`,`source`,`comment`,`occurredAt`,`createdAt`,`updatedAt`,`uuid`,`substanceUuid`,`isDeleted`,`originDeviceId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SubstanceEntry) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.substanceId)
        statement.bindDouble(3, entity.quantity)
        statement.bindDouble(4, entity.price)
        statement.bindText(5, entity.source)
        statement.bindText(6, entity.comment)
        statement.bindLong(7, entity.occurredAt)
        statement.bindLong(8, entity.createdAt)
        statement.bindLong(9, entity.updatedAt)
        statement.bindText(10, entity.uuid)
        statement.bindText(11, entity.substanceUuid)
        val _tmp: Int = if (entity.isDeleted) 1 else 0
        statement.bindLong(12, _tmp.toLong())
        statement.bindText(13, entity.originDeviceId)
      }
    }
    this.__updateAdapterOfSubstanceEntry = object : EntityDeleteOrUpdateAdapter<SubstanceEntry>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `substance_entries` SET `id` = ?,`substanceId` = ?,`quantity` = ?,`price` = ?,`source` = ?,`comment` = ?,`occurredAt` = ?,`createdAt` = ?,`updatedAt` = ?,`uuid` = ?,`substanceUuid` = ?,`isDeleted` = ?,`originDeviceId` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SubstanceEntry) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.substanceId)
        statement.bindDouble(3, entity.quantity)
        statement.bindDouble(4, entity.price)
        statement.bindText(5, entity.source)
        statement.bindText(6, entity.comment)
        statement.bindLong(7, entity.occurredAt)
        statement.bindLong(8, entity.createdAt)
        statement.bindLong(9, entity.updatedAt)
        statement.bindText(10, entity.uuid)
        statement.bindText(11, entity.substanceUuid)
        val _tmp: Int = if (entity.isDeleted) 1 else 0
        statement.bindLong(12, _tmp.toLong())
        statement.bindText(13, entity.originDeviceId)
        statement.bindLong(14, entity.id)
      }
    }
  }

  public override suspend fun insert(entry: SubstanceEntry): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfSubstanceEntry.insertAndReturnId(_connection, entry)
    _result
  }

  public override suspend fun update(entry: SubstanceEntry): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfSubstanceEntry.handle(_connection, entry)
  }

  public override fun getEntriesBetween(startMillis: Long, endMillis: Long): Flow<List<SubstanceEntry>> {
    val _sql: String = "SELECT * FROM substance_entries WHERE occurredAt >= ? AND occurredAt < ? AND isDeleted = 0 ORDER BY occurredAt DESC"
    return createFlow(__db, false, arrayOf("substance_entries")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, startMillis)
        _argIndex = 2
        _stmt.bindLong(_argIndex, endMillis)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubstanceId: Int = getColumnIndexOrThrow(_stmt, "substanceId")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfPrice: Int = getColumnIndexOrThrow(_stmt, "price")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfComment: Int = getColumnIndexOrThrow(_stmt, "comment")
        val _columnIndexOfOccurredAt: Int = getColumnIndexOrThrow(_stmt, "occurredAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfSubstanceUuid: Int = getColumnIndexOrThrow(_stmt, "substanceUuid")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfOriginDeviceId: Int = getColumnIndexOrThrow(_stmt, "originDeviceId")
        val _result: MutableList<SubstanceEntry> = mutableListOf()
        while (_stmt.step()) {
          val _item: SubstanceEntry
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSubstanceId: Long
          _tmpSubstanceId = _stmt.getLong(_columnIndexOfSubstanceId)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpPrice: Double
          _tmpPrice = _stmt.getDouble(_columnIndexOfPrice)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpComment: String
          _tmpComment = _stmt.getText(_columnIndexOfComment)
          val _tmpOccurredAt: Long
          _tmpOccurredAt = _stmt.getLong(_columnIndexOfOccurredAt)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpSubstanceUuid: String
          _tmpSubstanceUuid = _stmt.getText(_columnIndexOfSubstanceUuid)
          val _tmpIsDeleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp != 0
          val _tmpOriginDeviceId: String
          _tmpOriginDeviceId = _stmt.getText(_columnIndexOfOriginDeviceId)
          _item = SubstanceEntry(_tmpId,_tmpSubstanceId,_tmpQuantity,_tmpPrice,_tmpSource,_tmpComment,_tmpOccurredAt,_tmpCreatedAt,_tmpUpdatedAt,_tmpUuid,_tmpSubstanceUuid,_tmpIsDeleted,_tmpOriginDeviceId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getEntryById(id: Long): Flow<SubstanceEntry?> {
    val _sql: String = "SELECT * FROM substance_entries WHERE id = ?"
    return createFlow(__db, false, arrayOf("substance_entries")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubstanceId: Int = getColumnIndexOrThrow(_stmt, "substanceId")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfPrice: Int = getColumnIndexOrThrow(_stmt, "price")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfComment: Int = getColumnIndexOrThrow(_stmt, "comment")
        val _columnIndexOfOccurredAt: Int = getColumnIndexOrThrow(_stmt, "occurredAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfSubstanceUuid: Int = getColumnIndexOrThrow(_stmt, "substanceUuid")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfOriginDeviceId: Int = getColumnIndexOrThrow(_stmt, "originDeviceId")
        val _result: SubstanceEntry?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSubstanceId: Long
          _tmpSubstanceId = _stmt.getLong(_columnIndexOfSubstanceId)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpPrice: Double
          _tmpPrice = _stmt.getDouble(_columnIndexOfPrice)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpComment: String
          _tmpComment = _stmt.getText(_columnIndexOfComment)
          val _tmpOccurredAt: Long
          _tmpOccurredAt = _stmt.getLong(_columnIndexOfOccurredAt)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpSubstanceUuid: String
          _tmpSubstanceUuid = _stmt.getText(_columnIndexOfSubstanceUuid)
          val _tmpIsDeleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp != 0
          val _tmpOriginDeviceId: String
          _tmpOriginDeviceId = _stmt.getText(_columnIndexOfOriginDeviceId)
          _result = SubstanceEntry(_tmpId,_tmpSubstanceId,_tmpQuantity,_tmpPrice,_tmpSource,_tmpComment,_tmpOccurredAt,_tmpCreatedAt,_tmpUpdatedAt,_tmpUuid,_tmpSubstanceUuid,_tmpIsDeleted,_tmpOriginDeviceId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getEntryByUuid(uuid: String): SubstanceEntry? {
    val _sql: String = "SELECT * FROM substance_entries WHERE uuid = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, uuid)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubstanceId: Int = getColumnIndexOrThrow(_stmt, "substanceId")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfPrice: Int = getColumnIndexOrThrow(_stmt, "price")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfComment: Int = getColumnIndexOrThrow(_stmt, "comment")
        val _columnIndexOfOccurredAt: Int = getColumnIndexOrThrow(_stmt, "occurredAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfSubstanceUuid: Int = getColumnIndexOrThrow(_stmt, "substanceUuid")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfOriginDeviceId: Int = getColumnIndexOrThrow(_stmt, "originDeviceId")
        val _result: SubstanceEntry?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSubstanceId: Long
          _tmpSubstanceId = _stmt.getLong(_columnIndexOfSubstanceId)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpPrice: Double
          _tmpPrice = _stmt.getDouble(_columnIndexOfPrice)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpComment: String
          _tmpComment = _stmt.getText(_columnIndexOfComment)
          val _tmpOccurredAt: Long
          _tmpOccurredAt = _stmt.getLong(_columnIndexOfOccurredAt)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpSubstanceUuid: String
          _tmpSubstanceUuid = _stmt.getText(_columnIndexOfSubstanceUuid)
          val _tmpIsDeleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp != 0
          val _tmpOriginDeviceId: String
          _tmpOriginDeviceId = _stmt.getText(_columnIndexOfOriginDeviceId)
          _result = SubstanceEntry(_tmpId,_tmpSubstanceId,_tmpQuantity,_tmpPrice,_tmpSource,_tmpComment,_tmpOccurredAt,_tmpCreatedAt,_tmpUpdatedAt,_tmpUuid,_tmpSubstanceUuid,_tmpIsDeleted,_tmpOriginDeviceId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getEntriesUpdatedSince(sinceMillis: Long): List<SubstanceEntry> {
    val _sql: String = "SELECT * FROM substance_entries WHERE updatedAt > ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sinceMillis)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubstanceId: Int = getColumnIndexOrThrow(_stmt, "substanceId")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfPrice: Int = getColumnIndexOrThrow(_stmt, "price")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfComment: Int = getColumnIndexOrThrow(_stmt, "comment")
        val _columnIndexOfOccurredAt: Int = getColumnIndexOrThrow(_stmt, "occurredAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfSubstanceUuid: Int = getColumnIndexOrThrow(_stmt, "substanceUuid")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfOriginDeviceId: Int = getColumnIndexOrThrow(_stmt, "originDeviceId")
        val _result: MutableList<SubstanceEntry> = mutableListOf()
        while (_stmt.step()) {
          val _item: SubstanceEntry
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSubstanceId: Long
          _tmpSubstanceId = _stmt.getLong(_columnIndexOfSubstanceId)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpPrice: Double
          _tmpPrice = _stmt.getDouble(_columnIndexOfPrice)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpComment: String
          _tmpComment = _stmt.getText(_columnIndexOfComment)
          val _tmpOccurredAt: Long
          _tmpOccurredAt = _stmt.getLong(_columnIndexOfOccurredAt)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpSubstanceUuid: String
          _tmpSubstanceUuid = _stmt.getText(_columnIndexOfSubstanceUuid)
          val _tmpIsDeleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp != 0
          val _tmpOriginDeviceId: String
          _tmpOriginDeviceId = _stmt.getText(_columnIndexOfOriginDeviceId)
          _item = SubstanceEntry(_tmpId,_tmpSubstanceId,_tmpQuantity,_tmpPrice,_tmpSource,_tmpComment,_tmpOccurredAt,_tmpCreatedAt,_tmpUpdatedAt,_tmpUuid,_tmpSubstanceUuid,_tmpIsDeleted,_tmpOriginDeviceId)
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
    val _sql: String = "UPDATE substance_entries SET isDeleted = 1, updatedAt = ?, originDeviceId = ? WHERE id = ?"
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

  public override suspend fun softDeleteBySubstanceId(
    substanceId: Long,
    updatedAt: Long,
    originDeviceId: String,
  ) {
    val _sql: String = "UPDATE substance_entries SET isDeleted = 1, updatedAt = ?, originDeviceId = ? WHERE substanceId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, updatedAt)
        _argIndex = 2
        _stmt.bindText(_argIndex, originDeviceId)
        _argIndex = 3
        _stmt.bindLong(_argIndex, substanceId)
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
