package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ActivePillState
import com.example.ui.theme.InteractivePillTrack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ConfirmAction
import com.example.ui.viewmodel.DialerViewModel

@Composable
fun NavigationDock(
    activeTab: DialerViewModel.Tab,
    onTabSelected: (DialerViewModel.Tab) -> Unit,
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
        // 1. Left Element: Independent standalone 56x56 T9 Keypad Toggle button
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isDialpadOpen) ConfirmAction else InteractivePillTrack)
                .clickable { onDialpadToggle() }
                .testTag("t9_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            // Draw a 3x3 layout matrix of structural vector dots representing the T9 Keypad toggle
            T9DotsGrid(color = if (isDialpadOpen) Color.Black else TextPrimary)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 2. Center Section (The Segmented Interlocking Dock)
        Row(
            modifier = Modifier
                .height(56.dp)
                .weight(1f)
                .clip(RoundedCornerShape(28.dp))
                .background(InteractivePillTrack)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Recents Tab Component
            val isRecentsActive = activeTab == DialerViewModel.Tab.RECENTS
            val recentsWeight = if (isRecentsActive) 1.5f else 0.8f
            val recentsBg by animateColorAsState(
                targetValue = if (isRecentsActive) ActivePillState else Color.Transparent,
                label = "recentsBg"
            )
            val recentsColor by animateColorAsState(
                targetValue = if (isRecentsActive) TextPrimary else TextSecondary,
                label = "recentsColor"
            )

            Row(
                modifier = Modifier
                    .weight(recentsWeight)
                    .fillMaxHeight()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(recentsBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // Custom ripple bypassed for interlocking feel
                    ) { onTabSelected(DialerViewModel.Tab.RECENTS) }
                    .testTag("tab_recents"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Clock/Recent vector placeholder
                ClockIcon(color = recentsColor, size = 20.dp)
                if (isRecentsActive) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recents",
                        color = recentsColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            // Contacts Tab Component
            val isContactsActive = activeTab == DialerViewModel.Tab.CONTACTS
            val contactsWeight = if (isContactsActive) 1.5f else 0.8f
            val contactsBg by animateColorAsState(
                targetValue = if (isContactsActive) ActivePillState else Color.Transparent,
                label = "contactsBg"
            )
            val contactsColor by animateColorAsState(
                targetValue = if (isContactsActive) TextPrimary else TextSecondary,
                label = "contactsColor"
            )

            Row(
                modifier = Modifier
                    .weight(contactsWeight)
                    .fillMaxHeight()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(contactsBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(DialerViewModel.Tab.CONTACTS) }
                    .testTag("tab_contacts"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Address-book profile card vector
                AddressBookIcon(color = contactsColor, size = 20.dp)
                if (isContactsActive) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Contacts",
                        color = contactsColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 3. Right Element: Independent standalone 56x56 Global Search button
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isSearchOpen) ConfirmAction else InteractivePillTrack)
                .clickable { onSearchToggle() }
                .testTag("search_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Contacts",
                tint = if (isSearchOpen) Color.Black else TextPrimary,
                modifier = Modifier.size(24.dp)
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
        repeat(3) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(3) { col ->
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
fun ClockIcon(color: Color, size: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(size)) {
        val radius = size.toPx() / 2f
        val center = androidx.compose.ui.geometry.Offset(radius, radius)
        drawCircle(
            color = color,
            radius = radius - 2.dp.toPx(),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
        // Hands
        drawLine(
            color = color,
            start = center,
            end = androidx.compose.ui.geometry.Offset(radius, radius * 0.5f),
            strokeWidth = 2.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = color,
            start = center,
            end = androidx.compose.ui.geometry.Offset(radius * 1.3f, radius),
            strokeWidth = 2.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun AddressBookIcon(color: Color, size: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(size)) {
        val width = size.toPx()
        val height = size.toPx()
        // Draw card bounds
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(2.dp.toPx(), 2.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(width - 4.dp.toPx(), height - 4.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
        // Little silhouettes inside
        val faceCenter = androidx.compose.ui.geometry.Offset(width / 2f, height * 0.42f)
        drawCircle(
            color = color,
            radius = width * 0.14f,
            center = faceCenter
        )
        val bodyPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(width * 0.25f, height * 0.85f)
            quadraticTo(width * 0.5f, height * 0.6f, width * 0.75f, height * 0.85f)
        }
        drawPath(
            path = bodyPath,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}
