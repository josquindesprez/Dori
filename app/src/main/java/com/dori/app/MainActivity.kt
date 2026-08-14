package com.dori.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import com.dori.app.data.PendingShare
import com.dori.app.security.BiometricAuthenticator
import com.dori.app.ui.AppLockGate
import com.dori.app.ui.DoriApp
import com.dori.app.ui.theme.DoriTheme

class MainActivity : FragmentActivity() {

    private var pendingShareTrigger by mutableStateOf<Long?>(null)
    private lateinit var biometricAuthenticator: BiometricAuthenticator
    private lateinit var app: DoriApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        app = application as DoriApplication
        biometricAuthenticator = BiometricAuthenticator(this)
        handleIncomingIntent(intent)

        setContent {
            val lockEnabled by app.lockPreferences.isLockEnabled.collectAsState()

            DoriTheme {
                AppLockGate(
                    lockEnabled = lockEnabled,
                    isUnlocked = app.isUnlocked,
                    onAuthenticateRequest = ::requestUnlock
                ) {
                    DoriApp(pendingShareTrigger = pendingShareTrigger)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // The only point guaranteed to run after any in-flight onStop()/onSaveInstanceState()
        // has settled - see AppLockGate for why the prompt isn't triggered reactively instead.
        if (app.lockPreferences.isLockEnabled.value && !app.isUnlocked) {
            requestUnlock()
        }
    }

    private fun requestUnlock() {
        biometricAuthenticator.authenticate(
            title = "Unlock Dori",
            onSuccess = { app.isUnlocked = true }
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    /** A plain-text share (e.g. a quote from Research Reader) becomes a new, pre-filled note. */
    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") return
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
        PendingShare.offer(text)
        pendingShareTrigger = System.currentTimeMillis()
    }
}
