package com.dori.app.security

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private const val ALLOWED_AUTHENTICATORS =
    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

/**
 * Thin wrapper around androidx.biometric that accepts either a biometric (fingerprint/face)
 * or the device's own PIN/pattern/password as the unlock factor - whichever the user has
 * configured. Requires a FragmentActivity, which is why MainActivity extends that instead
 * of plain ComponentActivity.
 */
class BiometricAuthenticator(private val activity: FragmentActivity) {

    /** False means the device has no biometric or screen lock configured at all. */
    fun canAuthenticate(): Boolean {
        return BiometricManager.from(activity).canAuthenticate(ALLOWED_AUTHENTICATORS) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(title: String, onSuccess: () -> Unit, onFailure: () -> Unit = {}) {
        if (!canAuthenticate()) {
            // Nothing to check the user against - fail open rather than lock them out
            // of their own notes because no device lock is configured.
            onSuccess()
            return
        }

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder().setTitle(title)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            promptInfoBuilder.setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
        } else {
            @Suppress("DEPRECATION")
            promptInfoBuilder.setDeviceCredentialAllowed(true)
        }

        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onFailure()
                }
            }
        )
        prompt.authenticate(promptInfoBuilder.build())
    }
}
