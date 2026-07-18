package com.willykez.dialer.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.willykez.dialer.data.model.Contact
import com.willykez.dialer.ui.components.ContactAvatar
import com.willykez.dialer.ui.recents.EmptyState
import com.willykez.dialer.ui.recents.PermissionPrompt

@Composable
fun ContactsScreen(
    contacts: List<Contact>,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onOpenContact: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!hasPermission) {
        PermissionPrompt(message = "Allow access to contacts to see your address book.", onGrant = onRequestPermission)
        return
    }

    if (contacts.isEmpty()) {
        EmptyState(text = "No contacts found")
        return
    }

    val grouped = contacts.groupBy { it.displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "#" }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        grouped.toSortedMap().forEach { (letter, entries) ->
            item(key = "header_$letter") {
                Text(
                    text = letter,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
            items(entries, key = { it.contactId }) { contact ->
                ContactRow(contact = contact, onClick = { onOpenContact(contact) })
            }
        }
    }
}

@Composable
private fun ContactRow(contact: Contact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(photoUri = contact.photoUri, initials = contact.initials, size = 44.dp)
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = contact.displayName,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
