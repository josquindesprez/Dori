package com.dori.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dori.app.sync.PairingState
import com.dori.app.sync.SyncEngine

/** Pairs this Dori with another one so they sync; mirrors the Android SyncDevicesSection. */
@Composable
fun SyncPane(engine: SyncEngine) {
    val state by engine.pairingState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Sync devices", style = MaterialTheme.typography.headlineSmall)

        when (val s = state) {
            PairingState.Idle -> {
                Hint("Paired devices sync automatically whenever they're on the same WiFi. To pair, choose \"Add a device\" here and \"Join a device\" on the other Dori, or the other way round.")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = engine::startInviting) { Text("Add a device") }
                    OutlinedButton(onClick = engine::startJoining) { Text("Join a device") }
                }
            }

            PairingState.Inviting -> {
                Busy("Waiting for a device to join… On the other Dori, choose \"Join a device\".")
                TextButton(onClick = engine::cancelPairing) { Text("Cancel") }
            }

            is PairingState.Joining -> {
                if (s.devices.isEmpty()) {
                    Busy("Looking for devices… On the other Dori, choose \"Add a device\".")
                } else {
                    Hint("Choose the device to join:")
                    s.devices.forEach { device ->
                        OutlinedButton(onClick = { engine.joinWith(device) }, modifier = Modifier.fillMaxWidth()) {
                            Text(device.deviceName.ifBlank { "Unnamed device" })
                        }
                    }
                }
                TextButton(onClick = engine::cancelPairing) { Text("Cancel") }
            }

            is PairingState.Connecting -> {
                Busy("Connecting to ${s.peerName}…")
                TextButton(onClick = engine::cancelPairing) { Text("Cancel") }
            }

            is PairingState.ConfirmCode -> {
                Hint("Check that ${s.peerName} shows the same code:")
                Text(
                    text = s.code.chunked(3).joinToString(" "),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Hint("If the codes differ, someone else may be trying to pair. Choose \"They differ\".")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { engine.confirmCode(true) }) { Text("Codes match") }
                    OutlinedButton(onClick = { engine.confirmCode(false) }) { Text("They differ") }
                }
            }

            is PairingState.WaitingForPeer -> {
                Busy("Waiting for ${s.peerName} to confirm…")
                TextButton(onClick = engine::cancelPairing) { Text("Cancel") }
            }

            is PairingState.Paired -> {
                Hint("Paired with ${s.peerName}. Your data will sync whenever both are on the same network.")
                TextButton(onClick = engine::dismissPairingResult) { Text("OK") }
            }

            is PairingState.Failed -> {
                Text(text = s.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = engine::dismissPairingResult) { Text("OK") }
            }
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun Busy(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        Hint(text)
    }
}
