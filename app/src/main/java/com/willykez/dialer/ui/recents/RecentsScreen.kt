@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.willykez.dialer.ui.recents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.dialer.data.model.CallDirection
import com.willykez.dialer.data.model.CallLogEntry
import com.willykez.dialer.ui.components.CallDirectionArrow
import com.willykez.dialer.ui.components.ContactRowItem
import com.willykez.dialer.ui.components.EditorialHeader
import com.willykez.dialer.ui.theme.EmberOrange
import com.willykez.dialer.ui.theme.EmberPink
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun RecentsScreen(
    calls: List<CallLogEntry>,
    hasPermission: Boolean,
    isDefaultDialer: Boolean,
    simLabels: Map<String, String> = emptyMap(),
    onRequestPermission: () -> Unit,
    onRequestDefaultDialer: () -> Unit,
    onOpenSettings: () -> Unit,
    onCall: (String) -> Unit,
    onOpenDetail: (CallLogEntry) -> Unit,
    onDeleteCall: (CallLogEntry) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        EditorialHeader(
            title = "Calls",
            subtitle = if (calls.isNotEmpty()) "${calls.size} recent" else null
        ) {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    DropdownMenuItem(text = { Text("Settings") }, onClick = { menuExpanded = false; onOpenSettings() })
                }
            }
        }

        if (!hasPermission) {
            PermissionPrompt(message = "Allow access to call history to see your recent calls.", onGrant = onRequestPermission)
            return
        }

        if (calls.isEmpty() && isDefaultDialer) {
            EmptyState(text = "No recent calls yet")
            return
        }

        val grouped = calls.groupBy { dayBucket(it.timestamp) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 110.dp, top = 4.dp)
        ) {
            if (!isDefaultDialer) {
                item { DefaultDialerBanner(onActivate = onRequestDefaultDialer) }
            }

            grouped.forEach { (bucket, entries) ->
                stickyHeader {
                    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(vertical = 6.dp)) {
                        DateBucketChip(bucket)
                    }
                }

                items(entries, key = { it.id }) { entry ->
                    val isMissed = entry.direction == CallDirection.MISSED || entry.direction == CallDirection.REJECTED
                    val arrow = if (entry.direction == CallDirection.OUTGOING) CallDirectionArrow.OUTGOING else CallDirectionArrow.INCOMING
                    val simLabel = simLabels[entry.phoneAccountId]
                    val detailText = buildString {
                        append(formatLogTimestamp(entry.timestamp))
                        if (entry.durationSeconds > 0) { append(" \u2022 "); append(formatDuration(entry.durationSeconds)) }
                        if (!simLabel.isNullOrBlank()) { append(" \u2022 "); append(simLabel) }
                    }

                    SwipeableCallRow(onCall = { onCall(entry.number) }, onDelete = { onDeleteCall(entry) }) {
                        ContactRowItem(
                            primaryText = entry.displayName,
                            secondaryText = detailText,
                            photoUri = entry.photoUri,
                            isMissed = isMissed,
                            directionArrow = arrow,
                            onCallClick = { onCall(entry.number) },
                            onClick = { onOpenDetail(entry) }
                        )
                    }
                }
            }
        }
    }
}

/** A small floating pill instead of a full-width sticky bar — reads as a graphic tag, not chrome. */
@Composable
private fun DateBucketChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text.uppercase(), style = MaterialTheme.typography.labelMedium, color = EmberOrange, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SwipeableCallRow(onCall: () -> Unit, onDelete: () -> Unit, content: @Composable () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCall()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.Settled -> true
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val (brush, icon, alignment) = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Triple(
                    Brush.linearGradient(listOf(EmberOrange, EmberPink)), Icons.Filled.Call, Alignment.CenterStart
                )
                SwipeToDismissBoxValue.EndToStart -> Triple(
                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error)),
                    Icons.Filled.Delete, Alignment.CenterEnd
                )
                SwipeToDismissBoxValue.Settled -> Triple(Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)), Icons.Filled.Call, Alignment.Center)
            }
            Box(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp)).background(brush).padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
        }
    ) {
        content()
    }
}

private fun dayBucket(timestamp: Long): String {
    val now = Calendar.getInstance()
    val logTime = Calendar.getInstance().apply { timeInMillis = timestamp }
    return when {
        now.get(Calendar.YEAR) == logTime.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == logTime.get(Calendar.DAY_OF_YEAR) -> "Today"
        now.get(Calendar.YEAR) == logTime.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) - logTime.get(Calendar.DAY_OF_YEAR) == 1 -> "Yesterday"
        else -> SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
private fun DefaultDialerBanner(onActivate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable { onActivate() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("System dialer inactive", color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Set this app as your default phone app to receive calls, sync call history, and enable the full-screen incoming call UI.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(EmberOrange, EmberPink)))
                .clickable { onActivate() }
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text("Set as default dialer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun formatLogTimestamp(timestamp: Long): String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape).background(
                Brush.linearGradient(listOf(EmberOrange.copy(alpha = 0.25f), EmberPink.copy(alpha = 0.25f)))
            ),
            contentAlignment = Alignment.Center
        ) {
            Text("\u2205", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 32.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = text, color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun PermissionPrompt(message: String, onGrant: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(EmberOrange, EmberPink)))
                .clickable { onGrant() }
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text("Grant access", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
