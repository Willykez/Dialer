package com.willykez.dialer.ui.recents

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
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.willykez.dialer.data.model.CallDirection
import com.willykez.dialer.data.model.CallLogEntry
import com.willykez.dialer.ui.components.ContactAvatar
import com.willykez.dialer.ui.theme.AccentRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentsScreen(
    calls: List<CallLogEntry>,
    hasPermission: Boolean,
    simLabels: Map<String, String> = emptyMap(),
    onRequestPermission: () -> Unit,
    onCall: (String) -> Unit,
    onOpenDetail: (CallLogEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!hasPermission) {
        PermissionPrompt(
            message = "Allow access to call history to see your recent calls.",
            onGrant = onRequestPermission
        )
        return
    }

    if (calls.isEmpty()) {
        EmptyState(text = "No recent calls yet")
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
    ) {
        items(calls, key = { it.id }) { entry ->
            RecentCallRow(
                entry = entry,
                simLabel = simLabels[entry.phoneAccountId],
                onCall = { onCall(entry.number) },
                onClick = { onOpenDetail(entry) }
            )
        }
    }
}

@Composable
private fun RecentCallRow(
    entry: CallLogEntry,
    simLabel: String?,
    onCall: () -> Unit,
    onClick: () -> Unit
) {
    val isMissed = entry.direction == CallDirection.MISSED || entry.direction == CallDirection.REJECTED
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(photoUri = entry.photoUri, initials = entry.displayName.take(2).uppercase(), size = 48.dp)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.displayName,
                color = if (isMissed) AccentRed else MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                DirectionIcon(entry.direction)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatTimestamp(entry.timestamp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                if (!simLabel.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "\u2022 $simLabel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onCall) {
                Icon(Icons.Filled.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun DirectionIcon(direction: CallDirection) {
    val (icon, tint) = when (direction) {
        CallDirection.INCOMING -> Icons.AutoMirrored.Filled.CallReceived to MaterialTheme.colorScheme.onSurfaceVariant
        CallDirection.OUTGOING -> Icons.AutoMirrored.Filled.CallMade to MaterialTheme.colorScheme.onSurfaceVariant
        else -> Icons.AutoMirrored.Filled.CallMissed to AccentRed
    }
    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
}

private fun formatTimestamp(timestamp: Long): String {
    val format = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    return format.format(Date(timestamp))
}

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PermissionPrompt(message: String, onGrant: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        androidx.compose.material3.Button(onClick = onGrant) {
            Text("Grant access")
        }
    }
}
