package com.willykez.dialer.ui.calling

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.willykez.dialer.data.model.InCallStatus
import com.willykez.dialer.data.model.InCallUiState
import com.willykez.dialer.ui.components.ContactAvatar
import com.willykez.dialer.ui.dialpad.DialpadKeys
import com.willykez.dialer.ui.theme.AccentGreen
import com.willykez.dialer.ui.theme.AccentRed

@Composable
fun CallScreen(
    state: InCallUiState,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    onHangUp: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleHold: () -> Unit,
    onDialpadDigit: (Char) -> Unit
) {
    var showKeypad by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = statusLabel(state),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            ContactAvatar(
                photoUri = state.photoUri,
                initials = state.callerName.take(2).uppercase(),
                size = 128.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = state.callerName,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineLarge
            )

            if (state.callerName != state.number) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.number,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (state.isMultiSim && !state.simLabel.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = state.simLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AnimatedContent(targetState = state.status, label = "call_controls") { status ->
                when (status) {
                    InCallStatus.RINGING_INCOMING -> IncomingCallControls(onAnswer = onAnswer, onDecline = onDecline)
                    else -> ActiveCallControls(
                        state = state,
                        showKeypad = showKeypad,
                        onToggleKeypad = { showKeypad = !showKeypad },
                        onToggleMute = onToggleMute,
                        onToggleSpeaker = onToggleSpeaker,
                        onToggleHold = onToggleHold,
                        onHangUp = onHangUp,
                        onDialpadDigit = onDialpadDigit
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun statusLabel(state: InCallUiState): String = when (state.status) {
    InCallStatus.RINGING_INCOMING -> "Incoming call"
    InCallStatus.RINGING_OUTGOING, InCallStatus.DIALING -> "Calling..."
    InCallStatus.ACTIVE -> formatDuration(state.elapsedSeconds)
    InCallStatus.HOLDING -> "On hold"
    InCallStatus.DISCONNECTING, InCallStatus.DISCONNECTED -> "Call ended"
}

private fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
private fun IncomingCallControls(onAnswer: () -> Unit, onDecline: () -> Unit) {
    var dragOffset by remember { mutableStateOf(0f) }
    val threshold = 160f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CallCircleButton(
            icon = Icons.Filled.CallEnd,
            containerColor = AccentRed,
            label = "Decline",
            onClick = onDecline
        )
        CallCircleButton(
            icon = Icons.Filled.Call,
            containerColor = AccentGreen,
            label = "Answer",
            onClick = onAnswer,
            modifier = Modifier.pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (dragOffset < -threshold) onAnswer()
                        dragOffset = 0f
                    }
                ) { change, amount ->
                    change.consume()
                    dragOffset += amount.y
                }
            }
        )
    }
}

@Composable
private fun ActiveCallControls(
    state: InCallUiState,
    showKeypad: Boolean,
    onToggleKeypad: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleHold: () -> Unit,
    onHangUp: () -> Unit,
    onDialpadDigit: (Char) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (showKeypad) {
            DialpadKeys(
                onDigit = onDialpadDigit,
                showLetters = false,
                keySize = 64.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ToggleIconButton(
                icon = if (state.isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                selected = state.isMuted,
                onClick = onToggleMute
            )
            ToggleIconButton(
                icon = Icons.Filled.Dialpad,
                selected = showKeypad,
                onClick = onToggleKeypad
            )
            ToggleIconButton(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                selected = state.isSpeakerOn,
                onClick = onToggleSpeaker
            )
            ToggleIconButton(
                icon = if (state.isOnHold) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                selected = state.isOnHold,
                onClick = onToggleHold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        CallCircleButton(
            icon = Icons.Filled.CallEnd,
            containerColor = AccentRed,
            label = "End call",
            onClick = onHangUp
        )
    }
}

@Composable
private fun ToggleIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
    val tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
        }
    }
}

@Composable
private fun CallCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onClick) {
                Icon(imageVector = icon, contentDescription = label, tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
    }
}
