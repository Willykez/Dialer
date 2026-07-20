package com.willykez.dialer.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.willykez.dialer.data.model.CallLogEntry
import com.willykez.dialer.data.model.Contact
import com.willykez.dialer.ui.contacts.ContactsScreen
import com.willykez.dialer.ui.recents.RecentsScreen
import com.willykez.dialer.ui.viewmodel.DialerViewModel

@Composable
fun HomeScreen(
    activeTab: DialerViewModel.HomeTab,
    favorites: List<Contact>,
    recents: List<CallLogEntry>,
    contacts: List<Contact>,
    hasContactsPermission: Boolean,
    hasCallLogPermission: Boolean,
    isDefaultDialer: Boolean,
    simLabels: Map<String, String> = emptyMap(),
    onRequestContactsPermission: () -> Unit,
    onRequestCallLogPermission: () -> Unit,
    onRequestDefaultDialer: () -> Unit,
    onOpenSettings: () -> Unit,
    onCall: (String) -> Unit,
    onOpenContact: (Contact) -> Unit,
    onOpenCallDetail: (CallLogEntry) -> Unit,
    onDeleteCall: (CallLogEntry) -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (activeTab) {
        DialerViewModel.HomeTab.RECENTS -> RecentsScreen(
            calls = recents,
            hasPermission = hasCallLogPermission,
            isDefaultDialer = isDefaultDialer,
            simLabels = simLabels,
            onRequestPermission = onRequestCallLogPermission,
            onRequestDefaultDialer = onRequestDefaultDialer,
            onOpenSettings = onOpenSettings,
            onCall = onCall,
            onOpenDetail = onOpenCallDetail,
            onDeleteCall = onDeleteCall,
            modifier = modifier.fillMaxSize()
        )
        DialerViewModel.HomeTab.CONTACTS -> ContactsScreen(
            contacts = contacts,
            favorites = favorites,
            hasPermission = hasContactsPermission,
            onRequestPermission = onRequestContactsPermission,
            onOpenSettings = onOpenSettings,
            onOpenContact = onOpenContact,
            modifier = modifier.fillMaxSize()
        )
    }
}
