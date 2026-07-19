package com.willykez.dialer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.dialer.ui.viewmodel.DialerViewModel

@Composable
fun NavigationDock(
    activeTab: DialerViewModel.HomeTab,
    onTabSelected: (DialerViewModel.HomeTab) -> Unit,
    isDialpadOpen: Boolean,
    onDialpadToggle: () -> Unit,
    isSearchOpen: Boolean,
    onSearchToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isDialpadOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onDialpadToggle() },
            contentAlignment = Alignment.Center
        ) {
            T9DotsGrid(color = if (isDialpadOpen) Color.Black else MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .height(56.dp)
                .weight(1f)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DockSegment(
                label = "Recents",
                selected = activeTab == DialerViewModel.HomeTab.RECENTS,
                onClick = { onTabSelected(DialerViewModel.HomeTab.RECENTS) },
                icon = { color -> ClockIcon(color = color, size = 20.dp) }
            )
            DockSegment(
                label = "Contacts",
                selected = activeTab == DialerViewModel.HomeTab.CONTACTS,
                onClick = { onTabSelected(DialerViewModel.HomeTab.CONTACTS) },
                icon = { color -> AddressBookIcon(color = color, size = 20.dp) }
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isSearchOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onSearchToggle() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Contacts",
                tint = if (isSearchOpen) Color.Black else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun RowScope.DockSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit
) {
    val segmentWeight = if (selected) 1.5f else 0.8f
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent,
        label = "segment_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "segment_color"
    )

    Row(
        modifier = Modifier
            .weight(segmentWeight)
            .fillMaxHeight()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        icon(contentColor)
        if (selected) {
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
