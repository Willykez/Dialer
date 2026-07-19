package com.willykez.dialer.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    isDefaultDialer: Boolean,
    ringtoneLabel: String,
    vibrateOnRing: Boolean,
    blockedNumbers: List<String>,
    showSimRow: Boolean,
    simPreferenceLabel: String,
    onBack: () -> Unit,
    onRequestDefaultDialer: () -> Unit,
    onPickRingtone: () -> Unit,
    onToggleVibrate: (Boolean) -> Unit,
    onUnblock: (String) -> Unit,
    onPickDefaultSim: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Settings", style = MaterialTheme.typography.titleLarge)
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                SettingsSectionTitle("Calling")
            }
            item {
                SettingsRow(
                    title = "Default phone app",
                    subtitle = if (isDefaultDialer) "This app is your default dialer" else "Tap to set as default dialer",
                    onClick = onRequestDefaultDialer
                )
            }
            item {
                SettingsRow(
                    title = "Phone ringtone",
                    subtitle = ringtoneLabel,
                    onClick = onPickRingtone
                )
            }
            if (showSimRow) {
                item {
                    SettingsRow(
                        title = "SIM for outgoing calls",
                        subtitle = simPreferenceLabel,
                        onClick = onPickDefaultSim
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vibrate on incoming calls", style = MaterialTheme.typography.titleMedium)
                    }
                    Switch(checked = vibrateOnRing, onCheckedChange = onToggleVibrate)
                }
            }
            item {
                HorizontalDivider()
                SettingsSectionTitle("Blocked numbers")
            }
            if (blockedNumbers.isEmpty()) {
                item {
                    Text(
                        "No blocked numbers",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(blockedNumbers) { number ->
                    SettingsRow(title = number, subtitle = "Tap to unblock", onClick = { onUnblock(number) })
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
