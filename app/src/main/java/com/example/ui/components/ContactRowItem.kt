package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ContactRowItem(
    primaryText: String,
    secondaryText: String,
    modifier: Modifier = Modifier,
    isMissed: Boolean = false,
    directionArrow: CallDirectionArrow = CallDirectionArrow.NONE,
    isRecorded: Boolean = false,
    onCallClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ContainerLevel1)
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("contact_item_card"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Avatar Node
        val initialChar = primaryText.firstOrNull()?.uppercase() ?: "#"
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(ContainerLevel2, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Highly stylized double-spaced matrix style dot font feel for the monogram initial
            Text(
                text = ". $initialChar .",
                color = if (isMissed) DestructiveAction else TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Center-Left Text Stack
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // primaryText (Name or number)
            Text(
                text = primaryText,
                color = if (isMissed) DestructiveAction else TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // direction vector arrow layout row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                if (directionArrow != CallDirectionArrow.NONE) {
                    DirectionArrowVector(
                        direction = directionArrow,
                        isMissed = isMissed,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Text(
                    text = secondaryText,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (isRecorded) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Red.copy(alpha = 0.15f))
                            .border(0.5.dp, Color.Red.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⏺", color = Color.Red, fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("REC", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Far Right Handle Action
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(InteractivePillTrack)
                .clickable { onCallClick() }
                .testTag("row_call_action"),
            contentAlignment = Alignment.Center
        ) {
            CallHandsetIcon(color = TextPrimary, iconSize = 18.dp)
        }
    }
}

enum class CallDirectionArrow {
    INCOMING, OUTGOING, NONE
}

@Composable
fun DirectionArrowVector(
    direction: CallDirectionArrow,
    isMissed: Boolean,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val color = if (isMissed) DestructiveAction else if (direction == CallDirectionArrow.OUTGOING) ConfirmAction else TextSecondary

        when (direction) {
            CallDirectionArrow.OUTGOING -> {
                // Outgoing outbound arrow: angled up-right
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(0f, height),
                    end = androidx.compose.ui.geometry.Offset(width, 0f),
                    strokeWidth = 2.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                // arrowhead points up-right
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(width * 0.4f, 0f),
                    end = androidx.compose.ui.geometry.Offset(width, 0f),
                    strokeWidth = 2.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(width, height * 0.6f),
                    end = androidx.compose.ui.geometry.Offset(width, 0f),
                    strokeWidth = 2.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
            CallDirectionArrow.INCOMING -> {
                // Inbound arrow: angled down-left
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(width, 0f),
                    end = androidx.compose.ui.geometry.Offset(0f, height),
                    strokeWidth = 2.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                // arrowhead points down-left
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(width * 0.6f, height),
                    end = androidx.compose.ui.geometry.Offset(0f, height),
                    strokeWidth = 2.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(0f, height * 0.4f),
                    end = androidx.compose.ui.geometry.Offset(0f, height),
                    strokeWidth = 2.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
            else -> {}
        }
    }
}

@Composable
fun CallHandsetIcon(color: Color, iconSize: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(iconSize)) {
        val w = size.width
        val h = size.height
        // Draw standard clean outline phone receiver path
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.25f, h * 0.20f)
            quadraticTo(w * 0.45f, h * 0.10f, w * 0.60f, h * 0.25f)
            lineTo(w * 0.75f, h * 0.40f)
            quadraticTo(w * 0.85f, h * 0.50f, w * 0.70f, h * 0.65f)
            lineTo(w * 0.55f, h * 0.80f)
            quadraticTo(w * 0.45f, h * 0.90f, w * 0.35f, h * 0.80f)
            lineTo(w * 0.20f, h * 0.65f)
            quadraticTo(w * 0.10f, h * 0.55f, w * 0.25f, h * 0.35f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}
