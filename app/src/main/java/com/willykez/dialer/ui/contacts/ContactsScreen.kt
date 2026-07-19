package com.willykez.dialer.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.willykez.dialer.data.model.Contact
import com.willykez.dialer.ui.components.ContactRowItem
import com.willykez.dialer.ui.recents.EmptyState
import com.willykez.dialer.ui.recents.PermissionPrompt

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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            if (favorites.isNotEmpty()) {
                item {
                    Column {
                        SectionHeader("Favorite Contacts")
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            favorites.forEach { favorite ->
                                ContactRowItem(
                                    primaryText = favorite.displayName,
                                    secondaryText = favorite.primaryNumber,
                                    photoUri = favorite.photoUri,
                                    onCallClick = { onOpenContact(favorite) },
                                    onClick = { onOpenContact(favorite) }
                                )
                            }
                        }
                    }
                }
            }

            grouped.forEach { (header, itemsInGroup) ->
                item {
                    Column {
                        SectionHeader(header)
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsInGroup.forEach { contact ->
                                ContactRowItem(
                                    primaryText = contact.displayName,
                                    secondaryText = contact.primaryNumber,
                                    photoUri = contact.photoUri,
                                    onCallClick = { onOpenContact(contact) },
                                    onClick = { onOpenContact(contact) }
                                )
                            }
                        }
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
