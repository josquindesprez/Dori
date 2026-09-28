package com.dori.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Note::class, Label::class, DeviceIdentity::class, SyncPeerState::class, Substance::class, SubstanceEntry::class, SyncGroup::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun labelDao(): LabelDao
    abstract fun deviceIdentityDao(): DeviceIdentityDao
    abstract fun syncPeerStateDao(): SyncPeerStateDao
    abstract fun substanceDao(): SubstanceDao
    abstract fun substanceEntryDao(): SubstanceEntryDao
    abstract fun syncGroupDao(): SyncGroupDao
}
