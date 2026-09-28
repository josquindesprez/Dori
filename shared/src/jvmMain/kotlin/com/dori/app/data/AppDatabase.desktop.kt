package com.dori.app.data

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import java.io.File

// The common/JVM `Migration` actual only exposes migrate(SQLiteConnection) -
// unlike the Android actual, it has no SupportSQLiteDatabase overload, so
// this can't share code with AppDatabase.android.kt's MIGRATION_3_4 even
// though the SQL is identical.
private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
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
        connection.execSQL(
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
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_substance_entries_occurredAt` ON `substance_entries` (`occurredAt`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_substance_entries_substanceId` ON `substance_entries` (`substanceId`)")
    }
}

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `sync_group` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `groupId` TEXT NOT NULL,
                `groupKey` TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

private fun appDataDir(): File {
    val home = System.getProperty("user.home")
    // Roaming AppData, not Local: Local is where the installer puts the app's
    // own binaries (a per-user MSI install defaults to %LOCALAPPDATA%\Dori,
    // which would collide with a same-named data folder there), and Roaming
    // is the conventional home for user data that isn't the program itself.
    System.getenv("APPDATA")?.let { return File(it, "Dori") }
    if (System.getProperty("os.name").orEmpty().startsWith("Mac")) {
        return File(home, "Library/Application Support/Dori")
    }
    val xdgDataHome = System.getenv("XDG_DATA_HOME")?.takeIf { it.isNotBlank() }
    return File(xdgDataHome ?: "$home/.local/share", "Dori")
}

// Before XDG support, Linux and macOS kept the database in ~/Dori, which is
// also a natural place for a checkout of this repo. Move it over once.
private fun moveLegacyDatabase(dbFile: File) {
    if (System.getenv("APPDATA") != null) return
    val legacyDb = File(System.getProperty("user.home"), "Dori/dori.db")
    if (dbFile.exists() || !legacyDb.isFile || legacyDb == dbFile) return
    for (suffix in listOf("", "-wal", "-shm")) {
        val src = File(legacyDb.path + suffix)
        if (src.isFile) src.copyTo(File(dbFile.path + suffix))
    }
    for (suffix in listOf("", "-wal", "-shm", ".lck")) {
        File(legacyDb.path + suffix).delete()
    }
}

fun createDesktopDatabase(): AppDatabase {
    val appDataDir = appDataDir()
    appDataDir.mkdirs()
    val dbFile = File(appDataDir, "dori.db")
    moveLegacyDatabase(dbFile)

    return Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
        .build()
}
