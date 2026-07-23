@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.willykez.dialer.ui.dialpad

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.willykez.dialer.data.model.Contact
import com.willykez.dialer.ui.components.ContactAvatar
import com.willykez.dialer.ui.theme.EmberOrange
import com.willykez.dialer.ui.theme.EmberPink

@Composable
fun DialpadScreen(
    digits: String,
    matchingContacts: List<Contact>,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    onInsertPlus: () -> Unit = {},
    onCall: () -> Unit,
    onContactPicked: (Contact) -> Unit,
    onLongPressOne: () -> Unit = {},
    speedDialContacts: List<Contact> = emptyList(),
    onSpeedDial: (Contact) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 36.dp, height = 4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = digits,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {

            androidx.compose.animation.AnimatedVisibility(
                visible = digits.isNotEmpty() && matchingContacts.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    items(
                        matchingContacts,
                        key = { it.contactId }
                    ) { contact ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    onContactPicked(contact)
                                }
                                .padding(
                                    vertical = 8.dp,
                                    horizontal = 8.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            ContactAvatar(
                                photoUri = contact.photoUri,
                                initials = contact.initials,
                                ringSeed = contact.displayName,
                                size = 42.dp
                            )

                            Spacer(
                                modifier = Modifier.width(12.dp)
                            )

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    contact.displayName,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                Text(
                                    contact.primaryNumber,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                EmberOrange,
                                                EmberPink
                                            )
                                        )
                                    )
                                    .clickable {
                                        onContactPicked(contact)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Call,
                                    contentDescription = "Call ${contact.displayName}",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DialpadKeys(
            onDigit = onDigit,
            onLongPressZero = onInsertPlus,
            onLongPressOne = onLongPressOne,
            speedDialContacts = speedDialContacts,
            onSpeedDial = onSpeedDial,
            keySize = 72.dp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            Box(
                modifier = Modifier.size(76.dp)
            )

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                EmberOrange,
                                EmberPink
                            )
                        )
                    )
                    .clickable(enabled = digits.isNotEmpty()) {
                        onCall()
                    },
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    Icons.Filled.Call,
                    contentDescription = "Call",
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier.size(76.dp),
                contentAlignment = Alignment.Center
            ) {

                if (digits.isNotEmpty()) {

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .combinedClickable(
                                onClick = onBackspace,
                                onLongClick = onClearAll
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace (hold to clear)",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}