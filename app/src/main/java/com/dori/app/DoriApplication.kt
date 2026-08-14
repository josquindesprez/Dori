package com.dori.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.dori.app.data.NoteRepository
import com.dori.app.data.createAndroidDatabase
import com.dori.app.settings.AppLockPreferences
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

    val lockPreferences by lazy { AppLockPreferences(this) }

    /**
     * Lives here (Application-scoped) rather than on the Activity so it survives Activity
     * recreation on rotation but still resets on a real process restart - exactly the
     * "cold start + real backgrounding" re-lock timing this is meant to implement.
     */
    var isUnlocked by mutableStateOf(false)

    override fun onCreate() {
        super.onCreate()
        // Runs for as long as this process is alive - i.e. "while the app is
        // open," including backgrounded but not killed by the OS. No manual
        // "sync now" step: discovery and exchange both happen automatically.
        SyncEngine(repository).start()

        // Process-level, not Activity-level: a screen rotation destroys and recreates
        // MainActivity without this firing, but truly backgrounding the app does.
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                if (lockPreferences.isLockEnabled.value) {
                    isUnlocked = false
                }
            }
        })
    }
}
