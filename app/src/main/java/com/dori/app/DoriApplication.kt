package com.dori.app

import android.app.Application
import com.dori.app.data.NoteRepository
import com.dori.app.data.createAndroidDatabase
import com.dori.app.sync.SyncEngine

class DoriApplication : Application() {

    val repository: NoteRepository by lazy {
        val database = createAndroidDatabase(this)
        NoteRepository(
            noteDao = database.noteDao(),
            labelDao = database.labelDao(),
            deviceIdentityDao = database.deviceIdentityDao(),
            syncPeerStateDao = database.syncPeerStateDao(),
            substanceDao = database.substanceDao(),
            substanceEntryDao = database.substanceEntryDao(),
            deviceName = "Android"
        )
    }

    override fun onCreate() {
        super.onCreate()
        // Runs for as long as this process is alive - i.e. "while the app is
        // open," including backgrounded but not killed by the OS. No manual
        // "sync now" step: discovery and exchange both happen automatically.
        SyncEngine(repository).start()
    }
}
