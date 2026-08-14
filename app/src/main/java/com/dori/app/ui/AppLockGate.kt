package com.dori.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Wraps the app's content behind a biometric/PIN check. The prompt itself is *not* triggered
 * reactively from here - MainActivity fires it from onResume(), which is the only point
 * guaranteed to run after any in-flight onStop()/onSaveInstanceState() has settled. Triggering
 * it the instant this composes into the locked branch would otherwise race the same
 * background transition that caused the lock in the first place: BiometricPrompt refuses to
 * start once the host Activity has saved its state, so the auto-prompt would silently fail on
 * a background→foreground(→prompt) cycle. The button below is what actually shows the prompt
 * on that path; it's also the retry after a dismissed/failed attempt.
 */
@Composable
fun AppLockGate(
    lockEnabled: Boolean,
    isUnlocked: Boolean,
    onAuthenticateRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    if (!lockEnabled || isUnlocked) {
        content()
        return
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(text = "Dori is locked", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Verify it's you to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )
            Button(onClick = onAuthenticateRequest) { Text("Unlock") }
        }
    }
}
