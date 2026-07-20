package com.willykez.dialer.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.dialer.data.model.Contact
import com.willykez.dialer.ui.components.ContactRowItem
import com.willykez.dialer.ui.recents.EmptyState
import com.willykez.dialer.ui.recents.PermissionPrompt
import kotlinx.coroutines.launch

@Composable
fun ContactsScreen(
    contacts: List<Contact>,
    favorites: List<Contact>,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenContact: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Contacts", style = MaterialTheme.typography.headlineLarge)

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_INSERT).apply {
                        type = android.provider.ContactsContract.Contacts.CONTENT_TYPE
                    }
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Contact", tint = MaterialTheme.colorScheme.onBackground)
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                menuExpanded = false
                                onOpenSettings()
                            }
                        )
                    }
                }
            }
        }

        if (!hasPermission) {
            PermissionPrompt(message = "Allow access to contacts to see your address book.", onGrant = onRequestPermission)
            return
        }

        if (contacts.isEmpty()) {
            EmptyState(text = "No contacts found")
            return
        }

        val grouped = contacts
            .filterNot { it.isFavorite }
            .groupBy { it.displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
            .toSortedMap()

        // Flat list of (sectionHeader?, contact?) so we can map a letter -> the item index
        // it starts at, for the fast-scroll index on the right edge.
        data class Row(val header: String? = null, val contact: Contact? = null)

        val flatItems = buildList {
            if (favorites.isNotEmpty()) {
                add(Row(header = "Favorite Contacts"))
                favorites.forEach { add(Row(contact = it)) }
            }
            grouped.forEach { (header, itemsInGroup) ->
                add(Row(header = header))
                itemsInGroup.forEach { add(Row(contact = it)) }
            }
        }
        val letterIndexPositions = remember(grouped.keys) {
            var runningIndex = if (favorites.isNotEmpty()) 1 + favorites.size else 0
            val map = linkedMapOf<String, Int>()
            grouped.forEach { (header, itemsInGroup) ->
                map[header] = runningIndex
                runningIndex += 1 + itemsInGroup.size
            }
            map
        }

        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        val haptics = LocalHapticFeedback.current

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(end = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(flatItems.size) { index ->
                    val row = flatItems[index]
                    when {
                        row.header != null -> {
                            SectionHeader(row.header)
                        }
                        row.contact != null -> {
                            ContactRowItem(
                                primaryText = row.contact.displayName,
                                secondaryText = row.contact.primaryNumber,
                                photoUri = row.contact.photoUri,
                                onCallClick = { onOpenContact(row.contact) },
                                onClick = { onOpenContact(row.contact) }
                            )
                        }
                    }
                }
            }

            // Fast-scroll A-Z index, One UI / classic Contacts app style: drag down the
            // right edge to jump straight to a letter section, with a haptic tick per letter.
            if (letterIndexPositions.isNotEmpty()) {
                var activeLetter by remember { mutableStateOf<String?>(null) }
                val letters = letterIndexPositions.keys.toList()

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(24.dp)
                        .padding(vertical = 8.dp)
                        .pointerInput(letters) {
                            detectVerticalDragGestures(
                                onDragEnd = { activeLetter = null },
                                onDragCancel = { activeLetter = null }
                            ) { change, _ ->
                                change.consume()
                                val fraction = (change.position.y / size.height).coerceIn(0f, 0.999f)
                                val letter = letters[(fraction * letters.size).toInt()]
                                if (letter != activeLetter) {
                                    activeLetter = letter
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    letterIndexPositions[letter]?.let { targetIndex ->
                                        scope.launch { listState.scrollToItem(targetIndex) }
                                    }
                                }
                            }
                        },
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    letters.forEach { letter ->
                        Text(
                            text = letter,
                            fontSize = 10.sp,
                            fontWeight = if (letter == activeLetter) FontWeight.Bold else FontWeight.Normal,
                            color = if (letter == activeLetter) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    )
}
