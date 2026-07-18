package com.willykez.dialer.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.willykez.dialer.data.model.CallLogEntry
import com.willykez.dialer.data.model.Contact
import com.willykez.dialer.ui.contacts.ContactsScreen
import com.willykez.dialer.ui.recents.RecentsScreen
import com.willykez.dialer.ui.viewmodel.DialerViewModel

private val tabOrder = listOf(
    DialerViewModel.HomeTab.FAVORITES,
    DialerViewModel.HomeTab.RECENTS,
    DialerViewModel.HomeTab.CONTACTS
)

@Composable
fun HomeScreen(
    activeTab: DialerViewModel.HomeTab,
    onTabChanged: (DialerViewModel.HomeTab) -> Unit,
    favorites: List<Contact>,
    recents: List<CallLogEntry>,
    contacts: List<Contact>,
    hasContactsPermission: Boolean,
    hasCallLogPermission: Boolean,
    simLabels: Map<String, String> = emptyMap(),
    onRequestContactsPermission: () -> Unit,
    onRequestCallLogPermission: () -> Unit,
    onCall: (String) -> Unit,
    onOpenContact: (Contact) -> Unit,
    onOpenCallDetail: (CallLogEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = tabOrder.indexOf(activeTab).coerceAtLeast(0)
    ) { tabOrder.size }

    LaunchedEffect(activeTab) {
        val targetPage = tabOrder.indexOf(activeTab)
        if (targetPage >= 0 && targetPage != pagerState.currentPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val tab = tabOrder.getOrNull(pagerState.currentPage)
        if (tab != null && tab != activeTab) {
            onTabChanged(tab)
        }
    }

    HorizontalPager(state = pagerState, modifier = modifier.fillMaxSize()) { page ->
        when (tabOrder[page]) {
            DialerViewModel.HomeTab.FAVORITES -> {
                if (favorites.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            "Star a contact to add it here",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                } else {
                    ContactsScreen(
                        contacts = favorites,
                        hasPermission = hasContactsPermission,
                        onRequestPermission = onRequestContactsPermission,
                        onOpenContact = onOpenContact
                    )
                }
            }
            DialerViewModel.HomeTab.RECENTS -> RecentsScreen(
                calls = recents,
                hasPermission = hasCallLogPermission,
                simLabels = simLabels,
                onRequestPermission = onRequestCallLogPermission,
                onCall = onCall,
                onOpenDetail = onOpenCallDetail
            )
            DialerViewModel.HomeTab.CONTACTS -> ContactsScreen(
                contacts = contacts,
                hasPermission = hasContactsPermission,
                onRequestPermission = onRequestContactsPermission,
                onOpenContact = onOpenContact
            )
        }
    }
}
