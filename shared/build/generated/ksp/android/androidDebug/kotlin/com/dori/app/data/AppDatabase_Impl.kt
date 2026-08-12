package com.dori.app.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _noteDao: Lazy<NoteDao> = lazy {
    NoteDao_Impl(this)
  }

  private val _labelDao: Lazy<LabelDao> = lazy {
    LabelDao_Impl(this)
  }

  private val _deviceIdentityDao: Lazy<DeviceIdentityDao> = lazy {
    DeviceIdentityDao_Impl(this)
  }

  private val _syncPeerStateDao: Lazy<SyncPeerStateDao> = lazy {
    SyncPeerStateDao_Impl(this)
  }

  private val _substanceDao: Lazy<SubstanceDao> = lazy {
    SubstanceDao_Impl(this)
  }

  private val _substanceEntryDao: Lazy<SubstanceEntryDao> = lazy {
    SubstanceEntryDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(4, "0118c01e4ae2e19f271e82919fbfcf5b", "52c58d2410e44c3ca12c88fc39b50761") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `notes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `body` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `diaryDate` INTEGER, `labelId` INTEGER, `uuid` TEXT NOT NULL, `labelUuid` TEXT, `isDeleted` INTEGER NOT NULL, `originDeviceId` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `labels` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `colorArgb` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `uuid` TEXT NOT NULL, `isDeleted` INTEGER NOT NULL, `originDeviceId` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `device_identity` (`id` INTEGER NOT NULL, `deviceId` TEXT NOT NULL, `deviceName` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sync_peer_state` (`peerDeviceId` TEXT NOT NULL, `lastSyncedAt` INTEGER NOT NULL, PRIMARY KEY(`peerDeviceId`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `substances` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `unit` TEXT NOT NULL, `colorArgb` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `uuid` TEXT NOT NULL, `isDeleted` INTEGER NOT NULL, `originDeviceId` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `substance_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `substanceId` INTEGER NOT NULL, `quantity` REAL NOT NULL, `price` REAL NOT NULL, `source` TEXT NOT NULL, `comment` TEXT NOT NULL, `occurredAt` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `uuid` TEXT NOT NULL, `substanceUuid` TEXT NOT NULL, `isDeleted` INTEGER NOT NULL, `originDeviceId` TEXT NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_substance_entries_occurredAt` ON `substance_entries` (`occurredAt`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_substance_entries_substanceId` ON `substance_entries` (`substanceId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '0118c01e4ae2e19f271e82919fbfcf5b')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `notes`")
        connection.execSQL("DROP TABLE IF EXISTS `labels`")
        connection.execSQL("DROP TABLE IF EXISTS `device_identity`")
        connection.execSQL("DROP TABLE IF EXISTS `sync_peer_state`")
        connection.execSQL("DROP TABLE IF EXISTS `substances`")
        connection.execSQL("DROP TABLE IF EXISTS `substance_entries`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsNotes: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsNotes.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotes.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotes.put("body", TableInfo.Column("body", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotes.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotes.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotes.put("diaryDate", TableInfo.Column("diaryDate", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotes.put("labelId", TableInfo.Column("labelId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotes.put("uuid", TableInfo.Column("uuid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotes.put("labelUuid", TableInfo.Column("labelUuid", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotes.put("isDeleted", TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotes.put("originDeviceId", TableInfo.Column("originDeviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysNotes: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesNotes: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoNotes: TableInfo = TableInfo("notes", _columnsNotes, _foreignKeysNotes, _indicesNotes)
        val _existingNotes: TableInfo = read(connection, "notes")
        if (!_infoNotes.equals(_existingNotes)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |notes(com.dori.app.data.Note).
              | Expected:
              |""".trimMargin() + _infoNotes + """
              |
              | Found:
              |""".trimMargin() + _existingNotes)
        }
        val _columnsLabels: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsLabels.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLabels.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLabels.put("colorArgb", TableInfo.Column("colorArgb", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLabels.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLabels.put("uuid", TableInfo.Column("uuid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLabels.put("isDeleted", TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLabels.put("originDeviceId", TableInfo.Column("originDeviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysLabels: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesLabels: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoLabels: TableInfo = TableInfo("labels", _columnsLabels, _foreignKeysLabels, _indicesLabels)
        val _existingLabels: TableInfo = read(connection, "labels")
        if (!_infoLabels.equals(_existingLabels)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |labels(com.dori.app.data.Label).
              | Expected:
              |""".trimMargin() + _infoLabels + """
              |
              | Found:
              |""".trimMargin() + _existingLabels)
        }
        val _columnsDeviceIdentity: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDeviceIdentity.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDeviceIdentity.put("deviceId", TableInfo.Column("deviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDeviceIdentity.put("deviceName", TableInfo.Column("deviceName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDeviceIdentity: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDeviceIdentity: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDeviceIdentity: TableInfo = TableInfo("device_identity", _columnsDeviceIdentity, _foreignKeysDeviceIdentity, _indicesDeviceIdentity)
        val _existingDeviceIdentity: TableInfo = read(connection, "device_identity")
        if (!_infoDeviceIdentity.equals(_existingDeviceIdentity)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |device_identity(com.dori.app.data.DeviceIdentity).
              | Expected:
              |""".trimMargin() + _infoDeviceIdentity + """
              |
              | Found:
              |""".trimMargin() + _existingDeviceIdentity)
        }
        val _columnsSyncPeerState: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSyncPeerState.put("peerDeviceId", TableInfo.Column("peerDeviceId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncPeerState.put("lastSyncedAt", TableInfo.Column("lastSyncedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSyncPeerState: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSyncPeerState: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSyncPeerState: TableInfo = TableInfo("sync_peer_state", _columnsSyncPeerState, _foreignKeysSyncPeerState, _indicesSyncPeerState)
        val _existingSyncPeerState: TableInfo = read(connection, "sync_peer_state")
        if (!_infoSyncPeerState.equals(_existingSyncPeerState)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |sync_peer_state(com.dori.app.data.SyncPeerState).
              | Expected:
              |""".trimMargin() + _infoSyncPeerState + """
              |
              | Found:
              |""".trimMargin() + _existingSyncPeerState)
        }
        val _columnsSubstances: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSubstances.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstances.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstances.put("unit", TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstances.put("colorArgb", TableInfo.Column("colorArgb", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstances.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstances.put("uuid", TableInfo.Column("uuid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstances.put("isDeleted", TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstances.put("originDeviceId", TableInfo.Column("originDeviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSubstances: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSubstances: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSubstances: TableInfo = TableInfo("substances", _columnsSubstances, _foreignKeysSubstances, _indicesSubstances)
        val _existingSubstances: TableInfo = read(connection, "substances")
        if (!_infoSubstances.equals(_existingSubstances)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |substances(com.dori.app.data.Substance).
              | Expected:
              |""".trimMargin() + _infoSubstances + """
              |
              | Found:
              |""".trimMargin() + _existingSubstances)
        }
        val _columnsSubstanceEntries: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSubstanceEntries.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstanceEntries.put("substanceId", TableInfo.Column("substanceId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstanceEntries.put("quantity", TableInfo.Column("quantity", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstanceEntries.put("price", TableInfo.Column("price", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstanceEntries.put("source", TableInfo.Column("source", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstanceEntries.put("comment", TableInfo.Column("comment", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstanceEntries.put("occurredAt", TableInfo.Column("occurredAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstanceEntries.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstanceEntries.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstanceEntries.put("uuid", TableInfo.Column("uuid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstanceEntries.put("substanceUuid", TableInfo.Column("substanceUuid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstanceEntries.put("isDeleted", TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubstanceEntries.put("originDeviceId", TableInfo.Column("originDeviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSubstanceEntries: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSubstanceEntries: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSubstanceEntries.add(TableInfo.Index("index_substance_entries_occurredAt", false, listOf("occurredAt"), listOf("ASC")))
        _indicesSubstanceEntries.add(TableInfo.Index("index_substance_entries_substanceId", false, listOf("substanceId"), listOf("ASC")))
        val _infoSubstanceEntries: TableInfo = TableInfo("substance_entries", _columnsSubstanceEntries, _foreignKeysSubstanceEntries, _indicesSubstanceEntries)
        val _existingSubstanceEntries: TableInfo = read(connection, "substance_entries")
        if (!_infoSubstanceEntries.equals(_existingSubstanceEntries)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |substance_entries(com.dori.app.data.SubstanceEntry).
              | Expected:
              |""".trimMargin() + _infoSubstanceEntries + """
              |
              | Found:
              |""".trimMargin() + _existingSubstanceEntries)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "notes", "labels", "device_identity", "sync_peer_state", "substances", "substance_entries")
  }

  public override fun clearAllTables() {
    super.performClear(false, "notes", "labels", "device_identity", "sync_peer_state", "substances", "substance_entries")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(NoteDao::class, NoteDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(LabelDao::class, LabelDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(DeviceIdentityDao::class, DeviceIdentityDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SyncPeerStateDao::class, SyncPeerStateDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SubstanceDao::class, SubstanceDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SubstanceEntryDao::class, SubstanceEntryDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun noteDao(): NoteDao = _noteDao.value

  public override fun labelDao(): LabelDao = _labelDao.value

  public override fun deviceIdentityDao(): DeviceIdentityDao = _deviceIdentityDao.value

  public override fun syncPeerStateDao(): SyncPeerStateDao = _syncPeerStateDao.value

  public override fun substanceDao(): SubstanceDao = _substanceDao.value

  public override fun substanceEntryDao(): SubstanceEntryDao = _substanceEntryDao.value
}
