@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package com.willykez.dialer.ui.contacts

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.dialer.data.model.Contact
import com.willykez.dialer.ui.components.ContactAvatar
import com.willykez.dialer.ui.components.ContactRowItem
import com.willykez.dialer.ui.components.EditorialHeader
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
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        EditorialHeader(
            title = "Contacts",
            subtitle = if (contacts.isNotEmpty()) "${contacts.size} people" else null
        ) {
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
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    DropdownMenuItem(text = { Text("Settings") }, onClick = { menuExpanded = false; onOpenSettings() })
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

        data class Row(val header: String? = null, val contact: Contact? = null)

        val flatItems = buildList {
            grouped.forEach { (header, itemsInGroup) ->
                add(Row(header = header))
                itemsInGroup.forEach { add(Row(contact = it)) }
            }
        }
        val letterIndexPositions = remember(grouped.keys) {
            var runningIndex = 0
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
                modifier = Modifier.fillMaxSize().padding(end = 22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 110.dp, top = 4.dp)
            ) {
                if (favorites.isNotEmpty()) {
                    item {
                        QuickDialStrip(
                            favorites = favorites,
                            onOpenContact = onOpenContact,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                }

                items(flatItems.size) { index ->
                    val row = flatItems[index]
                    when {
                        row.header != null -> SectionHeader(row.header)
                        row.contact != null -> ContactRowNoCall(
                            contact = row.contact,
                            onClick = { onOpenContact(row.contact) },
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                }
            }

            if (letterIndexPositions.isNotEmpty()) {
                var activeLetter by remember { mutableStateOf<String?>(null) }
                val letters = letterIndexPositions.keys.toList()
                val favoritesOffset = if (favorites.isNotEmpty()) 1 else 0

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
                                        scope.launch { listState.scrollToItem(targetIndex + favoritesOffset) }
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
                            color = if (letter == activeLetter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Builds the shared-element modifier that makes a contact's avatar visually "fly" from
 * wherever it's tapped (Quick Dial strip or the alphabetical list) into the Contact Detail
 * hero position. Falls back to a no-op modifier when no transition scope is available
 * (e.g. this composable is previewed standalone).
 */
@Composable
private fun avatarSharedModifier(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    contactId: Long
): Modifier {
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return Modifier
    return with(sharedTransitionScope) {
        Modifier.sharedElement(
            rememberSharedContentState(key = "avatar-$contactId"),
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

/**
 * Horizontal "Quick Dial" strip: a graphic, One UI-style row of favorite avatars up top,
 * distinct from the alphabetical list below rather than folded into it.
 */
@Composable
private fun QuickDialStrip(
    favorites: List<Contact>,
    onOpenContact: (Contact) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = "QUICK DIAL",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 10.dp, start = 2.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(favorites, key = { it.contactId }) { contact ->
                Column(
                    modifier = Modifier.widthIn(max = 68.dp).clickable { onOpenContact(contact) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ContactAvatar(
                        photoUri = contact.photoUri,
                        initials = contact.initials,
                        ringSeed = contact.displayName,
                        size = 60.dp,
                        modifier = avatarSharedModifier(sharedTransitionScope, animatedVisibilityScope, contact.contactId)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = contact.displayName.substringBefore(" "),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** A lighter row (no call button) for the plain alphabetical list, tapping opens the profile. */
@Composable
private fun ContactRowNoCall(
    contact: Contact,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(
            photoUri = contact.photoUri,
            initials = contact.initials,
            ringSeed = contact.displayName,
            size = 46.dp,
            modifier = avatarSharedModifier(sharedTransitionScope, animatedVisibilityScope, contact.contactId)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(contact.displayName, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            Text(contact.primaryNumber, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = com.willykez.dialer.ui.theme.EmberOrange,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    )
}
