@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.willykez.dialer.ui.dialpad

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
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
                    fontSize = (size.value / 7f).sp
                )
            }
        }
    }
}
