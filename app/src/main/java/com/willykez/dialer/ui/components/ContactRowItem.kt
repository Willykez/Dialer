package com.willykez.dialer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class CallDirectionArrow { INCOMING, OUTGOING, NONE }

@Composable
fun ContactRowItem(
    primaryText: String,
    secondaryText: String,
    photoUri: String? = null,
    modifier: Modifier = Modifier,
    isMissed: Boolean = false,
    directionArrow: CallDirectionArrow = CallDirectionArrow.NONE,
    trailingBadge: (@Composable () -> Unit)? = null,
    onCallClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(
            photoUri = photoUri,
            initials = primaryText.take(2).uppercase(),
            size = 48.dp
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = primaryText,
                color = if (isMissed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (trailingBadge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingBadge()
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onCallClick() },
            contentAlignment = Alignment.Center
        ) {
            CallHandsetIcon(color = MaterialTheme.colorScheme.onBackground, iconSize = 18.dp)
        }
    }
}

@Composable
fun DirectionArrowVector(
    direction: CallDirectionArrow,
    isMissed: Boolean,
    modifier: Modifier = Modifier
) {
    val missedColor = MaterialTheme.colorScheme.error
    val outgoingColor = MaterialTheme.colorScheme.primary
    val defaultColor = MaterialTheme.colorScheme.onSurfaceVariant

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val color = if (isMissed) missedColor else if (direction == CallDirectionArrow.OUTGOING) outgoingColor else defaultColor

        when (direction) {
            CallDirectionArrow.OUTGOING -> {
                drawLine(
                    color = color,
                    start = Offset(0f, height),
                    end = Offset(width, 0f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(width * 0.4f, 0f),
                    end = Offset(width, 0f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(width, height * 0.6f),
                    end = Offset(width, 0f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            CallDirectionArrow.INCOMING -> {
                drawLine(
                    color = color,
                    start = Offset(width, 0f),
                    end = Offset(0f, height),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(width * 0.6f, height),
                    end = Offset(0f, height),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(0f, height * 0.4f),
                    end = Offset(0f, height),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            CallDirectionArrow.NONE -> {}
        }
    }
}

@Composable
fun CallHandsetIcon(color: androidx.compose.ui.graphics.Color, iconSize: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(iconSize)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
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
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
