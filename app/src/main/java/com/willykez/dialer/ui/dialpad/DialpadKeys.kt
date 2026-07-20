@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.willykez.dialer.ui.dialpad

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class DialKey(val digit: Char, val letters: String)

private val keys = listOf(
    DialKey('1', ""),
    DialKey('2', "ABC"),
    DialKey('3', "DEF"),
    DialKey('4', "GHI"),
    DialKey('5', "JKL"),
    DialKey('6', "MNO"),
    DialKey('7', "PQRS"),
    DialKey('8', "TUV"),
    DialKey('9', "WXYZ"),
    DialKey('*', ""),
    DialKey('0', "+"),
    DialKey('#', "")
)

/**
 * A Material 3 Expressive T9 keypad: keys "melt" from circles into rounder squircles under
 * finger pressure (a subtle morph, echoing the liquid-glass motion used in the nav pill),
 * with per-key press scale + haptic tick, matching the tactile feel of the modern
 * Google Phone / Pixel dialer keypad.
 */
@Composable
fun DialpadKeys(
    onDigit: (Char) -> Unit,
    onLongPressZero: () -> Unit = {},
    showLetters: Boolean = true,
    keySize: Dp = 76.dp,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                row.forEach { key ->
                    DialKeyButton(
                        key = key,
                        size = keySize,
                        showLetters = showLetters,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDigit(key.digit)
                        },
                        onLongPress = {
                            if (key.digit == '0') {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongPressZero()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DialKeyButton(
    key: DialKey,
    size: Dp,
    showLetters: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Expressive "liquid" morph: rounder squircle + slight shrink while pressed.
    val cornerRadius by animateFloatAsState(
        targetValue = if (isPressed) size.value * 0.30f else size.value * 0.5f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
        label = "key_corner"
    )
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f),
        label = "key_scale"
    )
    val containerColor by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 380f),
        label = "key_color"
    )

    Box(
        modifier = Modifier
            .size(size)
            .scale(pressScale)
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(
                androidx.compose.ui.graphics.lerp(
                    MaterialTheme.colorScheme.surfaceContainer,
                    MaterialTheme.colorScheme.primaryContainer,
                    containerColor
                )
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
                onLongClick = onLongPress
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = key.digit.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = (size.value / 2.4f).sp
            )
            if (showLetters && key.letters.isNotBlank()) {
                Text(
                    text = key.letters,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = (size.value / 7f).sp,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}
