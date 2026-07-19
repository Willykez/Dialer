package com.example.ui.recents

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallLog
import com.example.ui.components.CallDirectionArrow
import com.example.ui.components.ContactRowItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.DialerViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecentsScreen(
    viewModel: DialerViewModel,
    onInitiateCall: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val callLogs by viewModel.allCallLogs.collectAsState()
    var dropdownExpanded by remember { mutableStateOf(false) }
    var activePlaybackLog by remember { mutableStateOf<CallLog?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Top Bar Area: Large dominant title string "Calls", left-aligned, right vertical three-dot menu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Calls",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.testTag("recents_screen_title")
            )

            Box {
                IconButton(
                    onClick = { dropdownExpanded = true },
                    modifier = Modifier.testTag("recents_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu Options",
                        tint = TextPrimary
                    )
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.background(ContainerLevel2)
                ) {
                    DropdownMenuItem(
                        text = { Text("Clear All Logs", color = DestructiveAction) },
                        onClick = {
                            viewModel.endCall() // Safely abort any call timer dependencies
                            // Clear logs trigger
                            dropdownExpanded = false
                        },
                        modifier = Modifier.testTag("menu_clear_logs")
                    )
                }
            }
        }

        // Search highlight info panel or Spacer
        Spacer(modifier = Modifier.height(8.dp))

        if (callLogs.isEmpty()) {
            EmptyRecentsState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("recents_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp) // Cushion above floating bottom navigation
            ) {
                // Default Dialer dynamic setup card
                item {
                    val context = LocalContext.current
                    val telecomManager = remember { context.getSystemService(Context.TELECOM_SERVICE) as? android.telecom.TelecomManager }
                    val isDefault = remember(callLogs) { telecomManager?.defaultDialerPackage == context.packageName }

                    if (isDefault == false) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    try {
                                        val intent = android.content.Intent(android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                                            putExtra(android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // log or ignore
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = ContainerLevel1),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ConfirmAction.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "System Dialer Inactive ⚠️",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "To enable standard features like local system device logs synchronization and live call routing, tap to authorize this app.",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        try {
                                            val intent = android.content.Intent(android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                                                putExtra(android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ConfirmAction)
                                ) {
                                    Text("Activate Default Dialer", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                items(callLogs, key = { it.id }) { log ->
                    val displayName = log.name ?: log.number
                    val isMissed = log.callType == CallLog.CallType.MISSED
                    val arrowType = if (log.callType == CallLog.CallType.OUTGOING) {
                        CallDirectionArrow.OUTGOING
                    } else {
                        CallDirectionArrow.INCOMING
                    }

                    // Timestamp parsing
                    val formattedTime = remember(log.timestamp) {
                        formatLogTimestamp(log.timestamp)
                    }

                    val detailText = "Mobile • $formattedTime${if (log.duration != "00:00") " • ${log.duration}" else ""}"

                    ContactRowItem(
                        primaryText = displayName,
                        secondaryText = detailText,
                        isMissed = isMissed,
                        directionArrow = arrowType,
                        isRecorded = log.isRecorded,
                        onCallClick = {
                            onInitiateCall(displayName, log.number)
                        },
                        onClick = {
                            if (log.isRecorded) {
                                activePlaybackLog = log
                            } else {
                                onInitiateCall(displayName, log.number)
                            }
                        }
                    )
                }
            }
        }
    }

    // Interactive Recorded Call Playback Overlay Panel
    if (activePlaybackLog != null) {
        val log = activePlaybackLog!!
        var isPlaying by remember { mutableStateOf(false) }
        var playbackProgress by remember { mutableStateOf(0.40f) }
        var mockTimerSeconds by remember { mutableStateOf(18) }

        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                while (isPlaying) {
                    kotlinx.coroutines.delay(1000)
                    playbackProgress = (playbackProgress + 0.04f).coerceAtMost(1.0f)
                    mockTimerSeconds++
                    if (playbackProgress >= 1.0f) {
                        isPlaying = false
                        playbackProgress = 0.0f
                        mockTimerSeconds = 0
                    }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { activePlaybackLog = null },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.Red.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, Color.Red, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⏺", color = Color.Red, fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Call Recording", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text(log.name ?: log.number, color = TextSecondary, fontSize = 14.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Playback console tray
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ContainerLevel1)
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isPlaying = !isPlaying }
                        ) {
                            Text(if (isPlaying) "⏸" else "▶", color = ConfirmAction, fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            LinearProgressIndicator(
                                progress = { playbackProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = Color.Red,
                                trackColor = InteractivePillTrack
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "0:${mockTimerSeconds.toString().padStart(2, '0')}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "0:45",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onInitiateCall(log.name ?: log.number, log.number)
                            activePlaybackLog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ConfirmAction),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Redial ${log.number}", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { activePlaybackLog = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Panel", color = TextSecondary)
                }
            },
            containerColor = ContainerLevel2
        )
    }
}

@Composable
fun EmptyRecentsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("recents_empty_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(ContainerLevel1, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("∅", color = TextSecondary, fontSize = 36.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No recent calls",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Your call logs history will appear here. Press the T9 Keypad button below to dial a number.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

private fun formatLogTimestamp(timestamp: Long): String {
    val now = Calendar.getInstance()
    val logTime = Calendar.getInstance().apply { timeInMillis = timestamp }

    return when {
        now.get(Calendar.DATE) == logTime.get(Calendar.DATE) -> {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        }
        now.get(Calendar.DATE) - logTime.get(Calendar.DATE) == 1 -> {
            "Yesterday, " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        }
        else -> {
            SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
