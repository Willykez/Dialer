package com.willykez.dialer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.dialer.ui.theme.EmberOrange
import com.willykez.dialer.ui.theme.EmberPink

enum class CallDirectionArrow { INCOMING, OUTGOING, NONE }

/**
 * The recurring "Ember row card": a gently elevated rounded card (not a flat divided list
 * row) with a ring-avatar leading edge, a colored direction chip instead of a bare tiny
 * arrow, and a gradient Ember call button trailing edge.
 */
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
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(
            photoUri = photoUri,
            initials = primaryText.take(2).uppercase(),
            ringSeed = primaryText,
            size = 50.dp
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

            Spacer(modifier = Modifier.height(5.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (directionArrow != CallDirectionArrow.NONE) {
                    DirectionChip(direction = directionArrow, isMissed = isMissed)
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = secondaryText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
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
                .size(42.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(EmberOrange, EmberPink)))
                .clickable { onCallClick() },
            contentAlignment = Alignment.Center
        ) {
            CallHandsetIcon(color = Color.White, iconSize = 18.dp)
        }
    }
}

/** Small colored pill (not a bare tiny arrow) naming the call direction at a glance. */
@Composable
private fun DirectionChip(direction: CallDirectionArrow, isMissed: Boolean) {
    val (bg, label) = when {
        isMissed -> MaterialTheme.colorScheme.error.copy(alpha = 0.18f) to "Missed"
        direction == CallDirectionArrow.OUTGOING -> EmberOrange.copy(alpha = 0.18f) to "Out"
        else -> androidx.compose.ui.graphics.Color(0xFF2DD4BF).copy(alpha = 0.18f) to "In"
    }
    val tint = when {
        isMissed -> MaterialTheme.colorScheme.error
        direction == CallDirectionArrow.OUTGOING -> EmberOrange
        else -> androidx.compose.ui.graphics.Color(0xFF2DD4BF)
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DirectionArrowVector(direction = direction, isMissed = isMissed, tintOverride = tint, modifier = Modifier.size(9.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = label, color = tint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DirectionArrowVector(
    direction: CallDirectionArrow,
    isMissed: Boolean,
    modifier: Modifier = Modifier,
    tintOverride: Color? = null
) {
    val missedColor = MaterialTheme.colorScheme.error
    val outgoingColor = EmberOrange
    val defaultColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val color = tintOverride
            ?: if (isMissed) missedColor else if (direction == CallDirectionArrow.OUTGOING) outgoingColor else defaultColor

        when (direction) {
            CallDirectionArrow.OUTGOING -> {
                drawLine(color, Offset(0f, height), Offset(width, 0f), 2.dp.toPx(), StrokeCap.Round)
                drawLine(color, Offset(width * 0.4f, 0f), Offset(width, 0f), 2.dp.toPx(), StrokeCap.Round)
                drawLine(color, Offset(width, height * 0.6f), Offset(width, 0f), 2.dp.toPx(), StrokeCap.Round)
            }
            CallDirectionArrow.INCOMING -> {
                drawLine(color, Offset(width, 0f), Offset(0f, height), 2.dp.toPx(), StrokeCap.Round)
                drawLine(color, Offset(width * 0.6f, height), Offset(0f, height), 2.dp.toPx(), StrokeCap.Round)
                drawLine(color, Offset(0f, height * 0.4f), Offset(0f, height), 2.dp.toPx(), StrokeCap.Round)
            }
            CallDirectionArrow.NONE -> {}
        }
    }
}

@Composable
fun CallHandsetIcon(color: Color, iconSize: Dp) {
    Canvas(modifier = Modifier.size(iconSize)) {
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
        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}
