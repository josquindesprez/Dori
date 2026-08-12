package com.dori.app.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `labels` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `colorArgb` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("ALTER TABLE `notes` ADD COLUMN `labelId` INTEGER DEFAULT NULL")
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `notes` ADD COLUMN `uuid` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `notes` ADD COLUMN `labelUuid` TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE `notes` ADD COLUMN `isDeleted` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `notes` ADD COLUMN `originDeviceId` TEXT NOT NULL DEFAULT ''")

        db.execSQL("ALTER TABLE `labels` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `labels` ADD COLUMN `uuid` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `labels` ADD COLUMN `isDeleted` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `labels` ADD COLUMN `originDeviceId` TEXT NOT NULL DEFAULT ''")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `device_identity` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `deviceId` TEXT NOT NULL,
                `deviceName` TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `sync_peer_state` (
                `peerDeviceId` TEXT NOT NULL PRIMARY KEY,
                `lastSyncedAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // The backfill below needs this device's identity, so establish it now.
        val deviceId = UUID.randomUUID().toString()
        db.execSQL(
            "INSERT INTO `device_identity` (`id`, `deviceId`, `deviceName`) VALUES (0, ?, ?)",
            arrayOf<Any>(deviceId, "Android")
        )

        val now = System.currentTimeMillis()

        // Backfill uuid/originDeviceId for rows that existed before sync did.
        // updatedAt is left untouched for notes - this migration doesn't change
        // their content, just makes them eligible for sync.
        db.query("SELECT id FROM notes").use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow("id")
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                db.execSQL(
                    "UPDATE notes SET uuid = ?, originDeviceId = ? WHERE id = ?",
                    arrayOf<Any>(UUID.randomUUID().toString(), deviceId, id)
                )
            }
        }
        db.query("SELECT id FROM labels").use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow("id")
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                db.execSQL(
                    "UPDATE labels SET uuid = ?, originDeviceId = ?, updatedAt = ? WHERE id = ?",
                    arrayOf<Any>(UUID.randomUUID().toString(), deviceId, now, id)
                )
            }
        }

        // Notes referencing a label need that label's freshly-assigned uuid too.
        db.query(
            "SELECT n.id AS noteId, l.uuid AS labelUuid FROM notes n " +
                "JOIN labels l ON n.labelId = l.id WHERE n.labelId IS NOT NULL"
        ).use { cursor ->
            val noteIdIndex = cursor.getColumnIndexOrThrow("noteId")
            val labelUuidIndex = cursor.getColumnIndexOrThrow("labelUuid")
            while (cursor.moveToNext()) {
                val noteId = cursor.getLong(noteIdIndex)
                val labelUuid = cursor.getString(labelUuidIndex)
                db.execSQL("UPDATE notes SET labelUuid = ? WHERE id = ?", arrayOf<Any>(labelUuid, noteId))
            }
        }
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `substances` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `unit` TEXT NOT NULL,
                `colorArgb` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `uuid` TEXT NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                `originDeviceId` TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `substance_entries` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `substanceId` INTEGER NOT NULL,
                `quantity` REAL NOT NULL,
                `price` REAL NOT NULL,
                `source` TEXT NOT NULL,
                `comment` TEXT NOT NULL,
                `occurredAt` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `uuid` TEXT NOT NULL,
                `substanceUuid` TEXT NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                `originDeviceId` TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_substance_entries_occurredAt` ON `substance_entries` (`occurredAt`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_substance_entries_substanceId` ON `substance_entries` (`substanceId`)")
    }
}

fun createAndroidDatabase(context: Context): AppDatabase =
    Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "dori.db"
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()
