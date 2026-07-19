package com.example.ui.dialpad

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contact
import com.example.ui.theme.*
import com.example.ui.viewmodel.DialerViewModel

@Composable
fun DialpadOverlay(
    viewModel: DialerViewModel,
    onCallClick: (String, String) -> Unit,
    onCloseDialpad: () -> Unit,
    modifier: Modifier = Modifier
) {
    val digits by viewModel.dialpadDigits.collectAsState()
    val suggestions by viewModel.dialpadSuggestions.collectAsState()

    var showCreateContactDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(SurfaceBackground)
            .padding(16.dp)
    ) {
        // Digits entry preview screen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = digits.ifEmpty { " " },
                    color = TextPrimary,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.testTag("dialpad_digits_preview")
                )
            }

            if (digits.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.deleteDialpadDigit() },
                    modifier = Modifier.testTag("dialpad_delete_button")
                ) {
                    Text(
                        text = "⌫",
                        color = TextSecondary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // T9 Auto-Matched Entry Suggestion Tray (anchored directly above numeric array)
        if (digits.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillModifierWithMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = "Suggested",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                if (suggestions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ContainerLevel1)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "No direct directory mock suggestions",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialpad_suggestions_row"),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(suggestions) { contact ->
                            SuggestionCard(
                                contact = contact,
                                onClick = { onCallClick(contact.name, contact.phone) }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // The Numeric 3-column, 4-row Entry Matrix Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val keys = listOf(
                KeyData('1', ""),
                KeyData('2', "A B C"),
                KeyData('3', "D E F"),
                KeyData('4', "G H I"),
                KeyData('5', "J K L"),
                KeyData('6', "M N O"),
                KeyData('7', "P Q R S"),
                KeyData('8', "T U V"),
                KeyData('9', "W X Y Z"),
                KeyData('*', ""),
                KeyData('0', "+"),
                KeyData('#', "")
            )

            // Chunk keys in matrices of 3
            val rows = keys.chunked(3)
            rows.forEach { keyRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    keyRow.forEach { key ->
                        KeypadButton(
                            key = key,
                            onDigitTyped = { viewModel.appendDialpadDigit(it) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Primary Action Base Row: 3 Control Elements
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Circular button showing multi-dots minimize control item
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(InteractivePillTrack)
                    .clickable { onCloseDialpad() }
                    .testTag("dialpad_minimize_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Hide Dialer Keypad",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Center: Wide dominant green primary connection capsule
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(ConfirmAction)
                    .clickable {
                        val numberToCall = digits.ifEmpty { "1-800-227-5227" }
                        onCallClick("Willy Dialer Service", numberToCall)
                    }
                    .testTag("dialpad_call_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📞  Call",
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Right: Circular button housing mathematician plus sign to convert digits directly into contact profile
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(InteractivePillTrack)
                    .clickable {
                        if (digits.isNotEmpty()) {
                            showCreateContactDialog = true
                        }
                    }
                    .testTag("dialpad_add_contact_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Convert digits to new contact",
                    tint = if (digits.isNotEmpty()) TextPrimary else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // Modal quick creation contact flow when '+' clicked
    if (showCreateContactDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateContactDialog = false },
            title = { Text("Save to Contacts", color = TextPrimary) },
            text = {
                Column {
                    Text(
                        text = "Associate phone number \"$digits\" with a new user profile:",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Contact Name") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = ContainerLevel1,
                            unfocusedContainerColor = ContainerLevel1
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotEmpty()) {
                            viewModel.addContact(name, digits)
                            viewModel.clearDialpadDigits()
                            showCreateContactDialog = false
                        }
                    }
                ) {
                    Text("Save Info", color = ConfirmAction)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateContactDialog = false }) {
                    Text("Discard", color = TextSecondary)
                }
            },
            containerColor = ContainerLevel2
        )
    }
}

data class KeyData(val num: Char, val letters: String)

@Composable
fun KeypadButton(
    key: KeyData,
    onDigitTyped: (Char) -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(ContainerLevel2)
            .clickable { onDigitTyped(key.num) }
            .testTag("key_node_${key.num}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant serif style for numeric labels
            Text(
                text = key.num.toString(),
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Serif
            )
            if (key.letters.isNotEmpty()) {
                Text(
                    text = key.letters,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SuggestionCard(
    contact: Contact,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ContainerLevel1)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(ContainerLevel2, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.firstOrNull()?.uppercase() ?: "#",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contact.name,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(4.dp))
                // HD capabilities indicator
                Box(
                    modifier = Modifier
                        .border(1.dp, ConfirmAction.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = "HD",
                        color = ConfirmAction,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = contact.phone,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Utility to ensure clean modifiers
fun Modifier.fillModifierWithMaxWidth(): Modifier = this.fillMaxWidth()
