package com.willykez.dialer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.willykez.dialer.data.model.SimAccount

@Composable
fun SimPickerDialog(
    accounts: List<SimAccount>,
    allowAskEachTime: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (SimAccount?, Boolean) -> Unit
) {
    var selectedKey by remember { mutableStateOf(accounts.firstOrNull()?.storageKey) }
    var rememberChoice by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Call with") },
        text = {
            Column {
                accounts.forEach { account ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedKey = account.storageKey }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedKey == account.storageKey,
                            onClick = { selectedKey = account.storageKey }
                        )
                        Icon(
                            Icons.Filled.SimCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = account.label,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
                if (allowAskEachTime) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedKey = null }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedKey == null, onClick = { selectedKey = null })
                        Text(text = "Ask every time", modifier = Modifier.padding(start = 12.dp))
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rememberChoice = !rememberChoice }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = rememberChoice,
                            onCheckedChange = { rememberChoice = it }
                        )
                        Text(text = "Remember my choice", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val chosen = accounts.find { it.storageKey == selectedKey }
                onConfirm(chosen, rememberChoice)
            }) {
                Text("Call")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
