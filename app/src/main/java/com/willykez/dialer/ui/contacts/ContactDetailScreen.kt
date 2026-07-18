package com.willykez.dialer.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.willykez.dialer.data.model.Contact
import com.willykez.dialer.ui.components.ContactAvatar
import androidx.compose.foundation.layout.weight

@Composable
fun ContactDetailScreen(
    contact: Contact,
    isBlocked: Boolean,
    onBack: () -> Unit,
    onCall: (String) -> Unit,
    onMessage: (String) -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPickRingtone: () -> Unit,
    onToggleBlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (contact.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (contact.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ContactAvatar(photoUri = contact.photoUri, initials = contact.initials, size = 120.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = contact.displayName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                QuickAction(icon = Icons.Filled.Call, label = "Call") { onCall(contact.primaryNumber) }
                QuickAction(icon = Icons.Filled.Message, label = "Message") { onMessage(contact.primaryNumber) }
                QuickAction(icon = Icons.Filled.MusicNote, label = "Ringtone") { onPickRingtone() }
                QuickAction(
                    icon = Icons.Filled.Block,
                    label = if (isBlocked) "Unblock" else "Block",
                    tint = if (isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                ) { onToggleBlock() }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(contact.numbers) { number ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCall(number.number) }
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(number.number, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                        Text(
                            number.type.ifBlank { "Mobile" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Filled.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun QuickAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = tint)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
