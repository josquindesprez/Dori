package com.dori.app.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import java.time.LocalDate
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
public class NoteDao_Impl(
  __db: RoomDatabase,
) : NoteDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfNote: EntityInsertAdapter<Note>

  private val __converters: Converters = Converters()

  private val __updateAdapterOfNote: EntityDeleteOrUpdateAdapter<Note>
  init {
    this.__db = __db
    this.__insertAdapterOfNote = object : EntityInsertAdapter<Note>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `notes` (`id`,`title`,`body`,`createdAt`,`updatedAt`,`diaryDate`,`labelId`,`uuid`,`labelUuid`,`isDeleted`,`originDeviceId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Note) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.body)
        statement.bindLong(4, entity.createdAt)
        statement.bindLong(5, entity.updatedAt)
        val _tmpDiaryDate: LocalDate? = entity.diaryDate
        val _tmp: Long? = __converters.toEpochDay(_tmpDiaryDate)
        if (_tmp == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmp)
        }
        val _tmpLabelId: Long? = entity.labelId
        if (_tmpLabelId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpLabelId)
        }
        statement.bindText(8, entity.uuid)
        val _tmpLabelUuid: String? = entity.labelUuid
        if (_tmpLabelUuid == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpLabelUuid)
        }
        val _tmp_1: Int = if (entity.isDeleted) 1 else 0
        statement.bindLong(10, _tmp_1.toLong())
        statement.bindText(11, entity.originDeviceId)
      }
    }
    this.__updateAdapterOfNote = object : EntityDeleteOrUpdateAdapter<Note>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `notes` SET `id` = ?,`title` = ?,`body` = ?,`createdAt` = ?,`updatedAt` = ?,`diaryDate` = ?,`labelId` = ?,`uuid` = ?,`labelUuid` = ?,`isDeleted` = ?,`originDeviceId` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Note) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.body)
        statement.bindLong(4, entity.createdAt)
        statement.bindLong(5, entity.updatedAt)
        val _tmpDiaryDate: LocalDate? = entity.diaryDate
        val _tmp: Long? = __converters.toEpochDay(_tmpDiaryDate)
        if (_tmp == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmp)
        }
        val _tmpLabelId: Long? = entity.labelId
        if (_tmpLabelId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpLabelId)
        }
        statement.bindText(8, entity.uuid)
        val _tmpLabelUuid: String? = entity.labelUuid
        if (_tmpLabelUuid == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpLabelUuid)
        }
        val _tmp_1: Int = if (entity.isDeleted) 1 else 0
        statement.bindLong(10, _tmp_1.toLong())
        statement.bindText(11, entity.originDeviceId)
        statement.bindLong(12, entity.id)
      }
    }
  }

  public override suspend fun insert(note: Note): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfNote.insertAndReturnId(_connection, note)
    _result
  }

  public override suspend fun update(note: Note): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfNote.handle(_connection, note)
  }

  public override fun getLooseNotes(): Flow<List<Note>> {
    val _sql: String = "SELECT * FROM notes WHERE diaryDate IS NULL AND isDeleted = 0 ORDER BY updatedAt DESC"
    return createFlow(__db, false, arrayOf("notes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDiaryDate: Int = getColumnIndexOrThrow(_stmt, "diaryDate")
        val _columnIndexOfLabelId: Int = getColumnIndexOrThrow(_stmt, "labelId")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfLabelUuid: Int = getColumnIndexOrThrow(_stmt, "labelUuid")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfOriginDeviceId: Int = getColumnIndexOrThrow(_stmt, "originDeviceId")
        val _result: MutableList<Note> = mutableListOf()
        while (_stmt.step()) {
          val _item: Note
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: String
          _tmpBody = _stmt.getText(_columnIndexOfBody)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDiaryDate: LocalDate?
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfDiaryDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfDiaryDate)
          }
          _tmpDiaryDate = __converters.fromEpochDay(_tmp)
          val _tmpLabelId: Long?
          if (_stmt.isNull(_columnIndexOfLabelId)) {
            _tmpLabelId = null
          } else {
            _tmpLabelId = _stmt.getLong(_columnIndexOfLabelId)
          }
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpLabelUuid: String?
          if (_stmt.isNull(_columnIndexOfLabelUuid)) {
            _tmpLabelUuid = null
          } else {
            _tmpLabelUuid = _stmt.getText(_columnIndexOfLabelUuid)
          }
          val _tmpIsDeleted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_1 != 0
          val _tmpOriginDeviceId: String
          _tmpOriginDeviceId = _stmt.getText(_columnIndexOfOriginDeviceId)
          _item = Note(_tmpId,_tmpTitle,_tmpBody,_tmpCreatedAt,_tmpUpdatedAt,_tmpDiaryDate,_tmpLabelId,_tmpUuid,_tmpLabelUuid,_tmpIsDeleted,_tmpOriginDeviceId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getNotesForDate(date: LocalDate): Flow<List<Note>> {
    val _sql: String = "SELECT * FROM notes WHERE diaryDate = ? AND isDeleted = 0 ORDER BY updatedAt DESC"
    return createFlow(__db, false, arrayOf("notes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Long? = __converters.toEpochDay(date)
        if (_tmp == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, _tmp)
        }
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDiaryDate: Int = getColumnIndexOrThrow(_stmt, "diaryDate")
        val _columnIndexOfLabelId: Int = getColumnIndexOrThrow(_stmt, "labelId")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfLabelUuid: Int = getColumnIndexOrThrow(_stmt, "labelUuid")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfOriginDeviceId: Int = getColumnIndexOrThrow(_stmt, "originDeviceId")
        val _result: MutableList<Note> = mutableListOf()
        while (_stmt.step()) {
          val _item: Note
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: String
          _tmpBody = _stmt.getText(_columnIndexOfBody)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDiaryDate: LocalDate?
          val _tmp_1: Long?
          if (_stmt.isNull(_columnIndexOfDiaryDate)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getLong(_columnIndexOfDiaryDate)
          }
          _tmpDiaryDate = __converters.fromEpochDay(_tmp_1)
          val _tmpLabelId: Long?
          if (_stmt.isNull(_columnIndexOfLabelId)) {
            _tmpLabelId = null
          } else {
            _tmpLabelId = _stmt.getLong(_columnIndexOfLabelId)
          }
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpLabelUuid: String?
          if (_stmt.isNull(_columnIndexOfLabelUuid)) {
            _tmpLabelUuid = null
          } else {
            _tmpLabelUuid = _stmt.getText(_columnIndexOfLabelUuid)
          }
          val _tmpIsDeleted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_2 != 0
          val _tmpOriginDeviceId: String
          _tmpOriginDeviceId = _stmt.getText(_columnIndexOfOriginDeviceId)
          _item = Note(_tmpId,_tmpTitle,_tmpBody,_tmpCreatedAt,_tmpUpdatedAt,_tmpDiaryDate,_tmpLabelId,_tmpUuid,_tmpLabelUuid,_tmpIsDeleted,_tmpOriginDeviceId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getNoteById(id: Long): Flow<Note?> {
    val _sql: String = "SELECT * FROM notes WHERE id = ?"
    return createFlow(__db, false, arrayOf("notes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDiaryDate: Int = getColumnIndexOrThrow(_stmt, "diaryDate")
        val _columnIndexOfLabelId: Int = getColumnIndexOrThrow(_stmt, "labelId")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfLabelUuid: Int = getColumnIndexOrThrow(_stmt, "labelUuid")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfOriginDeviceId: Int = getColumnIndexOrThrow(_stmt, "originDeviceId")
        val _result: Note?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: String
          _tmpBody = _stmt.getText(_columnIndexOfBody)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDiaryDate: LocalDate?
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfDiaryDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfDiaryDate)
          }
          _tmpDiaryDate = __converters.fromEpochDay(_tmp)
          val _tmpLabelId: Long?
          if (_stmt.isNull(_columnIndexOfLabelId)) {
            _tmpLabelId = null
          } else {
            _tmpLabelId = _stmt.getLong(_columnIndexOfLabelId)
          }
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpLabelUuid: String?
          if (_stmt.isNull(_columnIndexOfLabelUuid)) {
            _tmpLabelUuid = null
          } else {
            _tmpLabelUuid = _stmt.getText(_columnIndexOfLabelUuid)
          }
          val _tmpIsDeleted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_1 != 0
          val _tmpOriginDeviceId: String
          _tmpOriginDeviceId = _stmt.getText(_columnIndexOfOriginDeviceId)
          _result = Note(_tmpId,_tmpTitle,_tmpBody,_tmpCreatedAt,_tmpUpdatedAt,_tmpDiaryDate,_tmpLabelId,_tmpUuid,_tmpLabelUuid,_tmpIsDeleted,_tmpOriginDeviceId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getNoteByUuid(uuid: String): Note? {
    val _sql: String = "SELECT * FROM notes WHERE uuid = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, uuid)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDiaryDate: Int = getColumnIndexOrThrow(_stmt, "diaryDate")
        val _columnIndexOfLabelId: Int = getColumnIndexOrThrow(_stmt, "labelId")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfLabelUuid: Int = getColumnIndexOrThrow(_stmt, "labelUuid")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfOriginDeviceId: Int = getColumnIndexOrThrow(_stmt, "originDeviceId")
        val _result: Note?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: String
          _tmpBody = _stmt.getText(_columnIndexOfBody)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDiaryDate: LocalDate?
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfDiaryDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfDiaryDate)
          }
          _tmpDiaryDate = __converters.fromEpochDay(_tmp)
          val _tmpLabelId: Long?
          if (_stmt.isNull(_columnIndexOfLabelId)) {
            _tmpLabelId = null
          } else {
            _tmpLabelId = _stmt.getLong(_columnIndexOfLabelId)
          }
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpLabelUuid: String?
          if (_stmt.isNull(_columnIndexOfLabelUuid)) {
            _tmpLabelUuid = null
          } else {
            _tmpLabelUuid = _stmt.getText(_columnIndexOfLabelUuid)
          }
          val _tmpIsDeleted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_1 != 0
          val _tmpOriginDeviceId: String
          _tmpOriginDeviceId = _stmt.getText(_columnIndexOfOriginDeviceId)
          _result = Note(_tmpId,_tmpTitle,_tmpBody,_tmpCreatedAt,_tmpUpdatedAt,_tmpDiaryDate,_tmpLabelId,_tmpUuid,_tmpLabelUuid,_tmpIsDeleted,_tmpOriginDeviceId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getNotesUpdatedSince(sinceMillis: Long): List<Note> {
    val _sql: String = "SELECT * FROM notes WHERE updatedAt > ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sinceMillis)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDiaryDate: Int = getColumnIndexOrThrow(_stmt, "diaryDate")
        val _columnIndexOfLabelId: Int = getColumnIndexOrThrow(_stmt, "labelId")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfLabelUuid: Int = getColumnIndexOrThrow(_stmt, "labelUuid")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfOriginDeviceId: Int = getColumnIndexOrThrow(_stmt, "originDeviceId")
        val _result: MutableList<Note> = mutableListOf()
        while (_stmt.step()) {
          val _item: Note
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: String
          _tmpBody = _stmt.getText(_columnIndexOfBody)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDiaryDate: LocalDate?
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfDiaryDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfDiaryDate)
          }
          _tmpDiaryDate = __converters.fromEpochDay(_tmp)
          val _tmpLabelId: Long?
          if (_stmt.isNull(_columnIndexOfLabelId)) {
            _tmpLabelId = null
          } else {
            _tmpLabelId = _stmt.getLong(_columnIndexOfLabelId)
          }
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpLabelUuid: String?
          if (_stmt.isNull(_columnIndexOfLabelUuid)) {
            _tmpLabelUuid = null
          } else {
            _tmpLabelUuid = _stmt.getText(_columnIndexOfLabelUuid)
          }
          val _tmpIsDeleted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_1 != 0
          val _tmpOriginDeviceId: String
          _tmpOriginDeviceId = _stmt.getText(_columnIndexOfOriginDeviceId)
          _item = Note(_tmpId,_tmpTitle,_tmpBody,_tmpCreatedAt,_tmpUpdatedAt,_tmpDiaryDate,_tmpLabelId,_tmpUuid,_tmpLabelUuid,_tmpIsDeleted,_tmpOriginDeviceId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: Long) {
    val _sql: String = "DELETE FROM notes WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
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
    val _sql: String = "UPDATE notes SET isDeleted = 1, updatedAt = ?, originDeviceId = ? WHERE id = ?"
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

  public override suspend fun clearLabel(labelId: Long) {
    val _sql: String = "UPDATE notes SET labelId = NULL, labelUuid = NULL WHERE labelId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, labelId)
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
