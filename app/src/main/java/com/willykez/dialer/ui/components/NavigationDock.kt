package com.willykez.dialer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.dialer.ui.theme.EmberOrange
import com.willykez.dialer.ui.theme.EmberPink
import com.willykez.dialer.ui.viewmodel.DialerViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * The primary bottom pill: [dialpad glass button] — [liquid-glass sliding segmented pill] — [search glass button].
 *
 * The center pill can be tapped OR dragged horizontally, iOS-Control-Center-style: the thumb
 * follows your finger with rubber-band resistance past the edges, "melts" between the two
 * segments as it slides (labels/icons scale + fade based on proximity to the thumb), and
 * snaps home with a bouncy spring + haptic tick on release.
 */
@Composable
fun NavigationDock(
    activeTab: DialerViewModel.HomeTab,
    onTabSelected: (DialerViewModel.HomeTab) -> Unit,
    isDialpadOpen: Boolean,
    onDialpadToggle: () -> Unit,
    isSearchOpen: Boolean,
    onSearchToggle: () -> Unit,
    missedCallBadgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // 0f == Recents, 1f == Contacts. Drives the sliding glass thumb.
    val progress = remember { Animatable(if (activeTab == DialerViewModel.HomeTab.CONTACTS) 1f else 0f) }
    var lastHapticStep by remember { mutableFloatStateOf(progress.value) }

    LaunchedEffect(activeTab) {
        val target = if (activeTab == DialerViewModel.HomeTab.CONTACTS) 1f else 0f
        if (kotlin.math.abs(progress.value - target) > 0.01f) {
            progress.animateTo(
                target,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassCircleButton(
            active = isDialpadOpen,
            onClick = onDialpadToggle,
            contentDescription = "Dialpad"
        ) { tint -> T9DotsGrid(color = tint) }

        Spacer(modifier = Modifier.width(8.dp))

        BoxWithConstraints(
            modifier = Modifier
                .height(56.dp)
                .weight(1f)
        ) {
            val pillWidthPx = constraints.maxWidth.toFloat()
            val segmentWidthPx = pillWidthPx / 2f

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        // Frosted glass base: translucent surface + faint top-light gradient.
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
                                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.18f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .pointerInput(pillWidthPx) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                val snapped = if (progress.value > 0.5f) 1f else 0f
                                scope.launch {
                                    progress.animateTo(
                                        snapped,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTabSelected(
                                    if (snapped == 1f) DialerViewModel.HomeTab.CONTACTS
                                    else DialerViewModel.HomeTab.RECENTS
                                )
                            },
                            onDragCancel = {},
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                val deltaFraction = dragAmount / pillWidthPx
                                val next = (progress.value + deltaFraction).coerceIn(-0.18f, 1.18f)
                                scope.launch { progress.snapTo(next) }
                                // Light "detent" tick as the thumb crosses the midpoint.
                                val step = if (next > 0.5f) 1f else 0f
                                if (step != lastHapticStep) {
                                    lastHapticStep = step
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        )
                    }
            ) {
                // Sliding liquid-glass thumb, clamped so it never fully escapes the pill visually.
                val clampedProgress = progress.value.coerceIn(0f, 1f)
                val overshoot = progress.value - clampedProgress // rubber-band feedback past 0/1
                val thumbWidthDp = with(androidx.compose.ui.platform.LocalDensity.current) {
                    (segmentWidthPx - 8f).toDp()
                }
                val thumbOffsetPx = (clampedProgress * segmentWidthPx) + 4f + (overshoot * segmentWidthPx * 0.3f)

                Box(
                    modifier = Modifier
                        .offset { androidx.compose.ui.unit.IntOffset(thumbOffsetPx.roundToInt(), 0) }
                        .fillMaxHeight()
                        .width(thumbWidthDp)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(EmberOrange.copy(alpha = 0.92f), EmberPink.copy(alpha = 0.92f))))
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    DockSegment(
                        label = "Recents",
                        thumbDistance = kotlin.math.abs(progress.value - 0f),
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTabSelected(DialerViewModel.HomeTab.RECENTS)
                        },
                        icon = { color ->
                            Box {
                                ClockIcon(color = color, size = 20.dp)
                                if (missedCallBadgeCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-2).dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error)
                                    )
                                }
                            }
                        }
                    )
                    DockSegment(
                        label = "Contacts",
                        thumbDistance = kotlin.math.abs(progress.value - 1f),
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTabSelected(DialerViewModel.HomeTab.CONTACTS)
                        },
                        icon = { color -> AddressBookIcon(color = color, size = 20.dp) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        GlassCircleButton(
            active = isSearchOpen,
            onClick = onSearchToggle,
            contentDescription = "Search Contacts"
        ) { tint ->
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun GlassCircleButton(
    active: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable (Color) -> Unit
) {
    val tint = if (active) Color.White else MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .then(
                if (active) Modifier.background(Brush.linearGradient(listOf(EmberOrange, EmberPink)))
                else Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f))
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.16f), Color.Transparent)),
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClickLabel = contentDescription,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content(tint)
    }
}

@Composable
private fun RowScope.DockSegment(
    label: String,
    thumbDistance: Float,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit
) {
    // "Liquid" proximity effect: content nearest the glass thumb is brightest & largest,
    // mimicking iOS liquid-glass magnification as the thumb slides underneath it.
    val closeness = (1f - thumbDistance.coerceIn(0f, 1f))
    val contentColor = androidx.compose.ui.graphics.lerp(
        MaterialTheme.colorScheme.onSurfaceVariant,
        Color.White,
        closeness
    )
    val scale = 0.94f + (closeness * 0.10f)

    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        icon(contentColor)
        if (closeness > 0.45f) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun T9DotsGrid(color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.size(18.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(color, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun ClockIcon(color: Color, size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val radius = size.toPx() / 2f
        val center = Offset(radius, radius)
        drawCircle(
            color = color,
            radius = radius - 2.dp.toPx(),
            style = Stroke(width = 2.dp.toPx())
        )
        drawLine(
            color = color,
            start = center,
            end = Offset(radius, radius * 0.5f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = center,
            end = Offset(radius * 1.3f, radius),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun AddressBookIcon(color: Color, size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val width = size.toPx()
        val height = size.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(width - 4.dp.toPx(), height - 4.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )
        val faceCenter = Offset(width / 2f, height * 0.42f)
        drawCircle(
            color = color,
            radius = width * 0.14f,
            center = faceCenter
        )
        val bodyPath = Path().apply {
            moveTo(width * 0.25f, height * 0.85f)
            quadraticTo(width * 0.5f, height * 0.6f, width * 0.75f, height * 0.85f)
        }
        drawPath(
            path = bodyPath,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
