package com.dori.app.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Whether Dori should require the device's biometric/PIN lock on launch and whenever it
 * returns from the background. Android-only concern (desktop has no such prompt), so this
 * lives in the app module rather than shared, backed by plain SharedPreferences and mirrored
 * into a StateFlow so the lock gate and the settings toggle observe the same live value.
 */
class AppLockPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isLockEnabled = MutableStateFlow(prefs.getBoolean(KEY_LOCK_ENABLED, false))
    val isLockEnabled: StateFlow<Boolean> = _isLockEnabled.asStateFlow()

    fun setLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOCK_ENABLED, enabled).apply()
        _isLockEnabled.value = enabled
    }

    companion object {
        private const val PREFS_NAME = "app_lock_prefs"
        private const val KEY_LOCK_ENABLED = "lock_enabled"
    }
}
