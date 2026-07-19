package com.example.ui.calling

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ActiveCallScreen(
    name: String,
    number: String,
    statusText: String,
    isRecording: Boolean,
    onToggleRecording: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 6 States for the control matrix
    var isMuteActive by remember { mutableStateOf(false) }
    var isKeypadActive by remember { mutableStateOf(false) }
    var isSpeakerActive by remember { mutableStateOf(false) }
    var isAddCallActive by remember { mutableStateOf(false) }
    var isHoldActive by remember { mutableStateOf(false) }
    var isBluetoothActive by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("immersive_connection_ui")
    ) {
        // Soft, circular blurred vignette effect radiating gently from the center
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            val radialGradient = Brush.radialGradient(
                colors = listOf(
                    ConfirmAction.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = centerOffset,
                radius = size.width * 0.9f
            )
            drawRect(brush = radialGradient)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Status Column Panel (upper margin padding of 80dp)
            Column(
                modifier = Modifier
                    .padding(top = 80.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Massive 120dp circular high-contrast profile picture gradient monogram
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ContainerLevel2, InteractivePillTrack)
                            )
                        )
                        .border(1.5.dp, ConfirmAction.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = name.split(" ")
                        .mapNotNull { it.firstOrNull()?.uppercase() }
                        .joinToString("")
                        .take(2)
                    Text(
                        text = if (initials.isNotEmpty()) initials else "#",
                        color = TextPrimary,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Caller Name in striking, extra-large bold font style (28sp)
                Text(
                    text = name,
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .testTag("active_caller_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Duration call-timer ticker text row ("Calling...", "00:04", "14:23") tracking in #A5A5A5
                Text(
                    text = statusText,
                    color = TextSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("active_call_duration_ticker")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = number,
                    color = TextSecondary.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                if (isRecording) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Red.copy(alpha = 0.15f))
                            .border(1.dp, Color.Red.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REC",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }

            // 2. Central In-Call Interactive Matrix Grid: Balanced 3-row spaced grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Row 1 nodes: Audio Mic Mute, Keypad DTMF, Speakerphone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InCallControlButton(
                        label = "Mute",
                        isActive = isMuteActive,
                        iconContent = { color -> MicIcon(color = color) },
                        onClick = { isMuteActive = !isMuteActive },
                        modifier = Modifier.testTag("controller_mute")
                    )

                    InCallControlButton(
                        label = "Keypad",
                        isActive = isKeypadActive,
                        iconContent = { color -> KeypadGridIcon(color = color) },
                        onClick = { isKeypadActive = !isKeypadActive },
                        modifier = Modifier.testTag("controller_keypad")
                    )

                    InCallControlButton(
                        label = "Speaker",
                        isActive = isSpeakerActive,
                        iconContent = { color -> AudioSpeakerIcon(color = color) },
                        onClick = { isSpeakerActive = !isSpeakerActive },
                        modifier = Modifier.testTag("controller_speaker")
                    )
                }

                // Row 2 nodes: Add Line Mixer, Hold connection, Bluetooth pairing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InCallControlButton(
                        label = "Add call",
                        isActive = isAddCallActive,
                        iconContent = { color -> AddLineIcon(color = color) },
                        onClick = { isAddCallActive = !isAddCallActive },
                        modifier = Modifier.testTag("controller_add_call")
                    )

                    InCallControlButton(
                        label = "Hold",
                        isActive = isHoldActive,
                        iconContent = { color -> HoldConnectionIcon(color = color) },
                        onClick = { isHoldActive = !isHoldActive },
                        modifier = Modifier.testTag("controller_hold")
                    )

                    InCallControlButton(
                        label = "Bluetooth",
                        isActive = isBluetoothActive,
                        iconContent = { color -> BluetoothAudioIcon(color = color) },
                        onClick = { isBluetoothActive = !isBluetoothActive },
                        modifier = Modifier.testTag("controller_bluetooth")
                    )
                }

                // Row 3 node: Integrated Call Recording Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InCallControlButton(
                        label = if (isRecording) "Recording" else "Record",
                        isActive = isRecording,
                        iconContent = { color ->
                            Box(contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.size(24.dp)) {
                                    drawCircle(
                                        color = if (isRecording) Color.Red else color,
                                        radius = size.minDimension / 2.3f
                                    )
                                    if (isRecording) {
                                        drawCircle(
                                            color = Color.White,
                                            radius = size.minDimension / 5.5f
                                        )
                                    }
                                }
                            }
                        },
                        onClick = onToggleRecording,
                        modifier = Modifier.testTag("controller_record")
                    )
                }
            }

            // 3. Primary Termination Control Node: Disconnect Call
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(DestructiveAction)
                    .clickable { onDisconnect() }
                    .testTag("disconnect_call_button"),
                contentAlignment = Alignment.Center
            ) {
                // Downward-facing angled phone receiver graphic asset (#FFFFFF)
                DownwardReceiverHandsetIcon(color = TextPrimary, iconSize = 32.dp)
            }
        }
    }
}

@Composable
fun InCallControlButton(
    label: String,
    isActive: Boolean,
    iconContent: @Composable (Color) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(84.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Outline profile track that reverses background properties to full white with black icons when active
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (isActive) TextPrimary else Color.Transparent)
                .border(2.dp, if (isActive) TextPrimary else InteractivePillTrack, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            iconContent(if (isActive) Color.Black else TextPrimary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = if (isActive) TextPrimary else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

// Custom geometric high-fidelity icons for ActiveCall Matrix (avoiding missing system vector shapes)
@Composable
fun MicIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        // Mic capsule
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.15f),
            size = androidx.compose.ui.geometry.Size(w * 0.3f, h * 0.5f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
        // Mic stand stand/cup
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.35f),
            size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.4f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        // Base line
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.75f),
            end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.9f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun KeypadGridIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val sizeDot = 3.dp.toPx()
        val spacingX = w / 4f
        val spacingY = h / 4f
        for (row in 1..3) {
            for (col in 1..3) {
                drawCircle(
                    color = color,
                    radius = sizeDot / 2f,
                    center = androidx.compose.ui.geometry.Offset(col * spacingX, row * spacingY)
                )
            }
        }
    }
}

@Composable
fun AudioSpeakerIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.25f, h * 0.35f)
            lineTo(w * 0.45f, h * 0.35f)
            lineTo(w * 0.70f, h * 0.15f)
            lineTo(w * 0.70f, h * 0.85f)
            lineTo(w * 0.45f, h * 0.65f)
            lineTo(w * 0.25f, h * 0.65f)
            close()
        }
        drawPath(path = path, color = color)
        // Waves
        drawArc(
            color = color,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.55f, h * 0.25f),
            size = androidx.compose.ui.geometry.Size(w * 0.3f, h * 0.5f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

@Composable
fun AddLineIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.15f),
            end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.85f),
            strokeWidth = 3.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.5f),
            end = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.5f),
            strokeWidth = 3.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun HoldConnectionIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.2f),
            end = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.8f),
            strokeWidth = 3.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.2f),
            end = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.8f),
            strokeWidth = 3.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun BluetoothAudioIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.3f, h * 0.3f)
            lineTo(w * 0.7f, h * 0.7f)
            lineTo(w * 0.5f, h * 0.9f)
            lineTo(w * 0.5f, h * 0.1f)
            lineTo(w * 0.7f, h * 0.3f)
            lineTo(w * 0.3f, h * 0.7f)
        }
        drawPath(
            path = path,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx(), join = androidx.compose.ui.graphics.StrokeJoin.Round, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

@Composable
fun DownwardReceiverHandsetIcon(color: Color, iconSize: androidx.compose.ui.unit.Dp) {
    Canvas(modifier = Modifier.size(iconSize)) {
        val w = size.width
        val h = size.height
        // Rotate a receiver down by 135 degrees to represent call hang-up / drop
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.15f, h * 0.4f)
            quadraticTo(w * 0.5f, h * 0.75f, w * 0.85f, h * 0.4f)
            lineTo(w * 0.75f, h * 0.25f)
            quadraticTo(w * 0.5f, h * 0.45f, w * 0.25f, h * 0.25f)
            close()
        }
        drawPath(path = path, color = color)
    }
}
