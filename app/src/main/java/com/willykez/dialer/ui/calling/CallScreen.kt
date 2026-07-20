package com.willykez.dialer.ui.calling

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.willykez.dialer.data.model.InCallStatus
import com.willykez.dialer.data.model.InCallUiState
import com.willykez.dialer.ui.components.ContactAvatar
import com.willykez.dialer.ui.dialpad.DialpadKeys
import com.willykez.dialer.ui.theme.AccentGreen
import com.willykez.dialer.ui.theme.AccentRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Full-screen call UI, restyled around Samsung One UI's layered/blurred backdrop layout
 * for the caller identity area, and an iOS-style horizontal slide-to-answer control for
 * incoming calls (with a quick-decline button kept alongside for discoverability/reliability).
 */
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

    Box(modifier = Modifier.fillMaxSize()) {
        BlurredCallerBackdrop(photoUri = state.photoUri)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = statusLabel(state),
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            ContactAvatar(
                photoUri = state.photoUri,
                initials = state.callerName.take(2).uppercase(),
                size = 132.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = state.callerName,
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge
            )

            if (state.callerName != state.number) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.number,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (state.isMultiSim && !state.simLabel.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = state.simLabel, color = Color.White, style = MaterialTheme.typography.labelMedium)
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

/**
 * One UI-style layered backdrop: the caller's photo, scaled up and heavily blurred, sitting
 * behind a dark scrim so foreground text stays legible. Falls back to a plain gradient when
 * there's no photo, or on API levels below 31 where Modifier.blur is a no-op.
 */
@Composable
private fun BlurredCallerBackdrop(photoUri: String?) {
    val context = LocalContext.current
    var bitmap by remember(photoUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(photoUri) {
        bitmap = null
        if (!photoUri.isNullOrBlank()) {
            bitmap = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(android.net.Uri.parse(photoUri))
                        ?.use { BitmapFactory.decodeStream(it) }
                }.getOrNull()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val loaded = bitmap
        if (loaded != null) {
            Image(
                bitmap = loaded.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(60.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.75f),
                            Color.Black.copy(alpha = 0.92f)
                        )
                    )
                )
        )
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

/**
 * iOS-style "slide to answer": a horizontal glass track with a draggable puck. Dragging the
 * puck past ~80% of the track answers the call, with a satisfying spring-snap + haptic on
 * both threshold-cross and commit. A standalone decline button sits beside the track so the
 * call can always be rejected with a single, unambiguous tap.
 */
@Composable
private fun IncomingCallControls(onAnswer: () -> Unit, onDecline: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            val trackWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
            val puckSizeDp = 60.dp
            val puckSizePx = with(LocalDensity.current) { puckSizeDp.toPx() }
            val maxDragPx = trackWidthPx - puckSizePx - 8f

            val dragX = remember { Animatable(0f) }
            var crossedThreshold by remember { mutableStateOf(false) }
            var answered by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color.White.copy(alpha = 0.14f))
            ) {
                Text(
                    text = "Slide to answer  \u2192",
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.align(Alignment.Center)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(4.dp)
                        .offset { androidx.compose.ui.unit.IntOffset(dragX.value.roundToInt(), 0) }
                        .size(puckSizeDp)
                        .clip(CircleShape)
                        .background(AccentGreen)
                        .pointerInput(maxDragPx) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (dragX.value > maxDragPx * 0.8f && !answered) {
                                        answered = true
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onAnswer()
                                    } else {
                                        scope.launch {
                                            dragX.animateTo(
                                                0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            )
                                        }
                                        crossedThreshold = false
                                    }
                                },
                                onDragCancel = {
                                    scope.launch { dragX.animateTo(0f) }
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                val next = (dragX.value + dragAmount).coerceIn(0f, maxDragPx)
                                scope.launch { dragX.snapTo(next) }
                                val nowPast = next > maxDragPx * 0.8f
                                if (nowPast != crossedThreshold) {
                                    crossedThreshold = nowPast
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Call, contentDescription = "Answer", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        CallCircleButton(
            icon = Icons.Filled.CallEnd,
            containerColor = AccentRed,
            label = "Decline",
            onClick = onDecline
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
    // One UI-style control grid: rounded glass cards in a fixed grid, big end-call FAB below.
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (showKeypad) {
            DialpadKeys(
                onDigit = onDialpadDigit,
                showLetters = false,
                keySize = 64.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OneUiControlTile(
                        icon = if (state.isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                        label = "Mute",
                        selected = state.isMuted,
                        onClick = onToggleMute,
                        modifier = Modifier.weight(1f)
                    )
                    OneUiControlTile(
                        icon = Icons.Filled.Dialpad,
                        label = "Keypad",
                        selected = showKeypad,
                        onClick = onToggleKeypad,
                        modifier = Modifier.weight(1f)
                    )
                    OneUiControlTile(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        label = "Speaker",
                        selected = state.isSpeakerOn,
                        onClick = onToggleSpeaker,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OneUiControlTile(
                        icon = if (state.isOnHold) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        label = if (state.isOnHold) "Resume" else "Hold",
                        selected = state.isOnHold,
                        onClick = onToggleHold,
                        modifier = Modifier.weight(1f)
                    )
                    OneUiControlTile(
                        icon = Icons.Filled.PersonAdd,
                        label = "Add call",
                        selected = false,
                        enabled = state.canAddCall,
                        onClick = { /* hand off to add-call flow if/when implemented */ },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        CallCircleButton(
            icon = Icons.Filled.CallEnd,
            containerColor = AccentRed,
            label = "End call",
            onClick = onHangUp
        )
    }
}

@Composable
private fun OneUiControlTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val background = when {
        !enabled -> Color.White.copy(alpha = 0.05f)
        selected -> MaterialTheme.colorScheme.primary
        else -> Color.White.copy(alpha = 0.12f)
    }
    val tint = if (selected && enabled) MaterialTheme.colorScheme.onPrimary else Color.White

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.padding(top = 12.dp)) {
            Icon(imageVector = icon, contentDescription = label, tint = tint.copy(alpha = if (enabled) 1f else 0.4f))
        }
        Text(
            text = label,
            color = tint.copy(alpha = if (enabled) 0.9f else 0.4f),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 10.dp)
        )
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
        Text(text = label, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelLarge)
    }
}
