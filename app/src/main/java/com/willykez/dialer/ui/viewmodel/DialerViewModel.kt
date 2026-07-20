package com.willykez.dialer.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.willykez.dialer.DialerApplication
import com.willykez.dialer.data.model.CallLogEntry
import com.willykez.dialer.data.model.Contact
import com.willykez.dialer.data.model.SimAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DialerViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DialerApplication

    enum class HomeTab { RECENTS, CONTACTS }

    private val _activeTab = MutableStateFlow(HomeTab.RECENTS)
    val activeTab: StateFlow<HomeTab> = _activeTab.asStateFlow()

    private val _isDialpadOpen = MutableStateFlow(false)
    val isDialpadOpen: StateFlow<Boolean> = _isDialpadOpen.asStateFlow()

    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _dialpadDigits = MutableStateFlow("")
    val dialpadDigits: StateFlow<String> = _dialpadDigits.asStateFlow()

    private val _hasContactsPermission = MutableStateFlow(false)
    val hasContactsPermission: StateFlow<Boolean> = _hasContactsPermission.asStateFlow()

    private val _hasCallLogPermission = MutableStateFlow(false)
    val hasCallLogPermission: StateFlow<Boolean> = _hasCallLogPermission.asStateFlow()

    private val _pendingCallNumber = MutableStateFlow<String?>(null)
    val pendingCallNumber: StateFlow<String?> = _pendingCallNumber.asStateFlow()

    private val _simAccounts = MutableStateFlow(app.phoneAccountsRepository.getCallCapableAccounts())
    val simAccounts: StateFlow<List<SimAccount>> = _simAccounts.asStateFlow()

    val isMultiSim: Boolean
        get() = simAccounts.value.size > 1

    fun refreshSimAccounts() {
        _simAccounts.value = app.phoneAccountsRepository.getCallCapableAccounts()
    }

    val preferredSimKey: StateFlow<String?> = app.settingsRepository.preferredSimKey

    fun setPreferredSimKey(key: String?) {
        app.settingsRepository.setPreferredSimKey(key)
    }

    val allContacts: StateFlow<List<Contact>> = app.contactsRepository.observeContactsAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentCalls: StateFlow<List<CallLogEntry>> = app.callLogRepository.observeCallLogAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteContacts: StateFlow<List<Contact>> = allContacts
        .map { contacts -> contacts.filter { it.isFavorite } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResults: StateFlow<List<Contact>> = combine(allContacts, _searchQuery) { contacts, query ->
        if (query.isBlank()) {
            emptyList()
        } else {
            contacts.filter { contact ->
                contact.displayName.contains(query, ignoreCase = true) ||
                    contact.numbers.any { it.number.contains(query) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dialpadMatches: StateFlow<List<Contact>> = combine(allContacts, _dialpadDigits) { contacts, digits ->
        if (digits.isBlank()) {
            emptyList()
        } else {
            contacts
                .mapNotNull { contact -> t9Rank(contact, digits)?.let { rank -> rank to contact } }
                .sortedWith(compareBy({ it.first }, { it.second.displayName }))
                .map { it.second }
                .take(20)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Google Phone-style "smart dial" ranking, lower is better:
     *   0 = a number starts with the typed digits
     *   1 = the T9 code of a name word (first/last/nickname) starts with the digits
     *   2 = the T9 code of the whole name starts with the digits
     *   3 = digits appear anywhere in a number
     *   4 = digits appear anywhere in the T9 code of the name
     *   null = no match at all
     */
    private fun t9Rank(contact: Contact, digits: String): Int? {
        val numberDigitsList = contact.numbers.map { it.number.filter { c -> c.isDigit() } }
        val fullT9 = contact.displayName.lowercase().map { letterToDigit(it) }.joinToString("")
        val wordT9s = contact.displayName.lowercase().split(" ", "-", ".").filter { it.isNotBlank() }
            .map { word -> word.map { letterToDigit(it) }.joinToString("") }

        return when {
            numberDigitsList.any { it.startsWith(digits) } -> 0
            wordT9s.any { it.startsWith(digits) } -> 1
            fullT9.startsWith(digits) -> 2
            numberDigitsList.any { it.contains(digits) } -> 3
            fullT9.contains(digits) -> 4
            else -> null
        }
    }

    private fun letterToDigit(c: Char): Char = when (c) {
        'a', 'b', 'c' -> '2'
        'd', 'e', 'f' -> '3'
        'g', 'h', 'i' -> '4'
        'j', 'k', 'l' -> '5'
        'm', 'n', 'o' -> '6'
        'p', 'q', 'r', 's' -> '7'
        't', 'u', 'v' -> '8'
        'w', 'x', 'y', 'z' -> '9'
        else -> c
    }

    fun setActiveTab(tab: HomeTab) {
        _activeTab.value = tab
    }

    fun setDialpadOpen(open: Boolean) {
        _isDialpadOpen.value = open
        if (open) _isSearchOpen.value = false
    }

    fun setSearchOpen(open: Boolean) {
        _isSearchOpen.value = open
        if (open) {
            _isDialpadOpen.value = false
            _searchQuery.value = ""
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun appendDigit(char: Char) {
        _dialpadDigits.value += char
    }

    fun backspaceDigit() {
        _dialpadDigits.value = _dialpadDigits.value.dropLast(1)
    }

    fun clearDigits() {
        _dialpadDigits.value = ""
    }

    fun setDigits(value: String) {
        _dialpadDigits.value = value
    }

    fun refreshPermissions() {
        val context = getApplication<Application>()
        _hasContactsPermission.value = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.READ_CONTACTS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        _hasCallLogPermission.value = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.READ_CALL_LOG
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun toggleFavorite(contact: Contact) {
        viewModelScope.launch {
            app.contactsRepository.setFavorite(contact.contactId, !contact.isFavorite)
        }
    }

    fun deleteCallLogEntry(id: Long) {
        viewModelScope.launch {
            app.callLogRepository.deleteEntry(id)
        }
    }

    fun placeCall(number: String) {
        if (number.isBlank()) return
        val context = getApplication<Application>()
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE)
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val accounts = app.phoneAccountsRepository.getCallCapableAccounts()
        if (accounts.size <= 1) {
            dial(number, null)
            return
        }

        val preferredKey = app.settingsRepository.preferredSimKey.value
        val preferredAccount = accounts.find { it.storageKey == preferredKey }
        if (preferredAccount != null) {
            dial(number, preferredAccount.handle)
        } else {
            _pendingCallNumber.value = number
        }
    }

    fun confirmPendingCall(account: SimAccount?, rememberChoice: Boolean) {
        val number = _pendingCallNumber.value ?: return
        _pendingCallNumber.value = null
        if (rememberChoice && account != null) {
            app.settingsRepository.setPreferredSimKey(account.storageKey)
        }
        dial(number, account?.handle)
    }

    fun cancelPendingCall() {
        _pendingCallNumber.value = null
    }

    private fun dial(number: String, accountHandle: PhoneAccountHandle?) {
        val context = getApplication<Application>()
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE)
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val extras = if (accountHandle != null) {
            Bundle().apply {
                putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, accountHandle)
            }
        } else {
            null
        }
        try {
            telecomManager.placeCall(Uri.fromParts("tel", number, null), extras)
        } catch (securityException: SecurityException) {
            return
        }
    }

    fun isDefaultDialer(): Boolean {
        val context = getApplication<Application>()
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
        return telecomManager?.defaultDialerPackage == context.packageName
    }
}
