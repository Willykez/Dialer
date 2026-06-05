package com.example.ui.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.CallLog
import com.example.data.model.Contact
import com.example.data.model.ContactGroup
import com.example.data.repository.CallLogRepository
import com.example.data.repository.ContactsRepository
import com.example.data.repository.ContactGroupRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DialerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val callLogRepository = CallLogRepository(db.callLogDao())
    private val contactsRepository = ContactsRepository(db.contactDao())
    private val contactGroupRepository = ContactGroupRepository(db.contactGroupDao())

    val allGroups: StateFlow<List<ContactGroup>> = contactGroupRepository.allGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tabs
    enum class Tab { RECENTS, CONTACTS }
    private val _activeTab = MutableStateFlow(Tab.RECENTS)
    val activeTab: StateFlow<Tab> = _activeTab.asStateFlow()

    // Navigation and overlays state
    private val _isDialpadOpen = MutableStateFlow(false)
    val isDialpadOpen: StateFlow<Boolean> = _isDialpadOpen.asStateFlow()

    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen.asStateFlow()

    // Search and inputs
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _dialpadDigits = MutableStateFlow("")
    val dialpadDigits: StateFlow<String> = _dialpadDigits.asStateFlow()

    // Call state data classes
    data class ActiveCall(
        val name: String,
        val number: String,
        val isIncoming: Boolean,
        val durationSeconds: Int = 0,
        val statusText: String = "Calling...",
        val isRecording: Boolean = false
    )

    private val _activeCall = MutableStateFlow<ActiveCall?>(null)
    val activeCall: StateFlow<ActiveCall?> = _activeCall.asStateFlow()

    private var timerJob: Job? = null

    // Base contacts & call logs flows
    val allCallLogs: StateFlow<List<CallLog>> = callLogRepository.allCallLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allContacts: StateFlow<List<Contact>> = contactsRepository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search results flow: dynamically filters if searchQuery changes or allContacts changes
    val searchResults: StateFlow<List<Contact>> = _searchQuery
        .debounce(100)
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                flowOf(emptyList())
            } else {
                contactsRepository.searchContacts(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // T9 dialpad auto-matched entries suggestions
    val dialpadSuggestions: StateFlow<List<Contact>> = _dialpadDigits
        .flatMapLatest { digits ->
            if (digits.isEmpty()) {
                flowOf(emptyList())
            } else {
                // T9-like filter: just simple substring contains matching for convenience & high fidelity
                contactsRepository.searchContacts(digits)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Pre-populate data if empty
        viewModelScope.launch {
            contactsRepository.allContacts.first().let { currentContacts ->
                if (currentContacts.isEmpty()) {
                    prepopulateMockData()
                }
            }
        }
    }

    private suspend fun prepopulateMockData() {
        // Pre-fill groups
        val friendsId = contactGroupRepository.insertGroup(ContactGroup(name = "Friends"))
        val familyId = contactGroupRepository.insertGroup(ContactGroup(name = "Family"))
        val workId = contactGroupRepository.insertGroup(ContactGroup(name = "Work"))

        // Pre-fill contacts
        val defaultContacts = listOf(
            Contact(name = "Alice Johnson", phone = "+1 555-0120", isFavorite = true, groupId = familyId),
            Contact(name = "Bob Smith", phone = "+1 555-0131", isFavorite = false, groupId = friendsId),
            Contact(name = "David Miller", phone = "+1 555-0144", isFavorite = false, groupId = workId),
            Contact(name = "Emma Watson", phone = "+1 555-0115", isFavorite = true, groupId = friendsId),
            Contact(name = "Frank Wright", phone = "+1 555-0199", isFavorite = false),
            Contact(name = "Grace Hopper", phone = "+1 555-0182", isFavorite = false, groupId = workId),
            Contact(name = "Henry Ford", phone = "+1 555-0171", isFavorite = false),
            Contact(name = "Isabella Ross", phone = "+1 555-0103", isFavorite = true),
            Contact(name = "Jack Reacher", phone = "+1 555-0222", isFavorite = false),
            Contact(name = "Liam Neeson", phone = "+1 555-0111", isFavorite = true, groupId = familyId),
            Contact(name = "Sophia Loren", phone = "+1 555-0300", isFavorite = true),
            // Corporate hotline (represented with a unique section like "Support" or sorted accordingly)
            Contact(name = "Carlcare Service Hotline", phone = "1-800-227-5227", isFavorite = false)
        )

        for (contact in defaultContacts) {
            contactsRepository.insertContact(contact)
        }

        // Pre-fill call logs
        val now = System.currentTimeMillis()
        val defaultLogs = listOf(
            CallLog(
                name = "Liam Neeson",
                number = "+1 555-0111",
                duration = "00:00",
                timestamp = now - 15 * 60 * 1000, // 15 mins ago
                callType = CallLog.CallType.MISSED
            ),
            CallLog(
                name = "Alice Johnson",
                number = "+1 555-0120",
                duration = "01:24",
                timestamp = now - 2 * 3600 * 1000, // 2 hours ago
                callType = CallLog.CallType.OUTGOING
            ),
            CallLog(
                name = null,
                number = "+1 555-4920",
                duration = "00:00",
                timestamp = now - 24 * 3600 * 1000, // Yesterday
                callType = CallLog.CallType.MISSED
            ),
            CallLog(
                name = "Emma Watson",
                number = "+1 555-0115",
                duration = "04:12",
                timestamp = now - 2 * 24 * 3600 * 1000, // 2 days ago
                callType = CallLog.CallType.INCOMING
            ),
            CallLog(
                name = "Bob Smith",
                number = "+1 555-0131",
                duration = "00:45",
                timestamp = now - 4 * 24 * 3600 * 1000, // 4 days ago
                callType = CallLog.CallType.OUTGOING
            )
        )

        for (log in defaultLogs) {
            callLogRepository.insertCallLog(log)
        }
    }

    // UI actions
    fun setActiveTab(tab: Tab) {
        _activeTab.value = tab
    }

    fun setDialpadOpen(isOpen: Boolean) {
        _isDialpadOpen.value = isOpen
        if (isOpen) {
            _isSearchOpen.value = false
        }
    }

    fun setSearchOpen(isOpen: Boolean) {
        _isSearchOpen.value = isOpen
        if (isOpen) {
            _isDialpadOpen.value = false
            _searchQuery.value = ""
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun appendDialpadDigit(char: Char) {
        _dialpadDigits.value += char
    }

    fun deleteDialpadDigit() {
        val current = _dialpadDigits.value
        if (current.isNotEmpty()) {
            _dialpadDigits.value = current.dropLast(1)
        }
    }

    fun clearDialpadDigits() {
        _dialpadDigits.value = ""
    }

    // Call management
    fun startCall(name: String, number: String, isIncoming: Boolean = false) {
        timerJob?.cancel()
        _activeCall.value = ActiveCall(
            name = name,
            number = number,
            isIncoming = isIncoming,
            durationSeconds = 0,
            statusText = if (isIncoming) "Incoming..." else "Calling...",
            isRecording = false
        )

        // Reset inputs & close overlays
        _isDialpadOpen.value = false

        timerJob = viewModelScope.launch {
            // Simulated ringing delay
            delay(2000)
            _activeCall.update { currentCall ->
                currentCall?.copy(statusText = "00:00")
            }
            _activeCall.value?.let { call ->
                postCallNotification(call.name, "00:00")
            }

            var elapsed = 0
            while (true) {
                delay(1000)
                elapsed++
                val minutes = elapsed / 60
                val seconds = elapsed % 60
                val durationStr = String.format("%02d:%02d", minutes, seconds)
                _activeCall.update { currentCall ->
                    currentCall?.copy(
                        durationSeconds = elapsed,
                        statusText = durationStr
                    )
                }
            }
        }
    }

    fun toggleRecording() {
        _activeCall.update { currentCall ->
            if (currentCall != null) {
                val nextRecording = !currentCall.isRecording
                postRecordingNotification(currentCall.name, nextRecording)
                currentCall.copy(isRecording = nextRecording)
            } else {
                null
            }
        }
    }

    fun endCall() {
        timerJob?.cancel()
        val currentCall = _activeCall.value
        if (currentCall != null) {
            // Write call to log
            viewModelScope.launch {
                val durationStr = if (currentCall.durationSeconds > 0) {
                    val minutes = currentCall.durationSeconds / 60
                    val seconds = currentCall.durationSeconds % 60
                    String.format("%02d:%02d", minutes, seconds)
                } else {
                    "00:00"
                }

                // If duration is 0 and isIncoming, it might be missed
                val finalCallType = when {
                    currentCall.isIncoming && currentCall.durationSeconds == 0 -> CallLog.CallType.MISSED
                    currentCall.isIncoming -> CallLog.CallType.INCOMING
                    else -> CallLog.CallType.OUTGOING
                }

                callLogRepository.insertCallLog(
                    CallLog(
                        name = if (currentCall.name != currentCall.number) currentCall.name else null,
                        number = currentCall.number,
                        duration = durationStr,
                        timestamp = System.currentTimeMillis(),
                        callType = finalCallType,
                        isRecorded = currentCall.isRecording
                    )
                )

                if (currentCall.isRecording) {
                    postNotification("Call Recording Saved", "Your call recording with ${currentCall.name} was saved successfully.")
                }
            }
        }
        _activeCall.value = null
    }

    // Creating contacts quickly from UI
    fun addContact(name: String, phone: String, groupId: Long? = null) {
        viewModelScope.launch {
            contactsRepository.insertContact(
                Contact(name = name, phone = phone, groupId = groupId)
            )
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            contactsRepository.deleteContact(contact)
        }
    }

    // Group Management
    fun createGroup(name: String) {
        viewModelScope.launch {
            contactGroupRepository.insertGroup(ContactGroup(name = name))
        }
    }

    fun updateGroup(group: ContactGroup) {
        viewModelScope.launch {
            contactGroupRepository.updateGroup(group)
        }
    }

    fun deleteGroup(group: ContactGroup) {
        viewModelScope.launch {
            // Unlink contacts of this group
            contactsRepository.allContacts.first().forEach { contact ->
                if (contact.groupId == group.id) {
                    contactsRepository.updateContactGroup(contact.id, null)
                }
            }
            contactGroupRepository.deleteGroup(group)
        }
    }

    fun assignContactToGroup(contactId: Long, groupId: Long?) {
        viewModelScope.launch {
            contactsRepository.updateContactGroup(contactId, groupId)
        }
    }

    fun toggleContactFavorite(contact: Contact) {
        viewModelScope.launch {
            contactsRepository.updateFavorite(contact.id, !contact.isFavorite)
        }
    }

    // Notifications channel config & triggers
    private fun createNotificationChannel() {
        val context = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Dialer Notifications"
            val descriptionText = "Notifications for calls and call recordings"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("dialer_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun postNotification(title: String, message: String) {
        try {
            createNotificationChannel()
            val context = getApplication<Application>()
            val builder = androidx.core.app.NotificationCompat.Builder(context, "dialer_channel")
                .setSmallIcon(android.R.drawable.stat_sys_phone_call)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            val notificationManager = androidx.core.app.NotificationManagerCompat.from(context)
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < 33) {
                notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun postCallNotification(callerName: String, statusText: String) {
        postNotification("Active Call: $callerName", "Status: $statusText")
    }

    fun postRecordingNotification(callerName: String, isRecording: Boolean) {
        if (isRecording) {
            postNotification("Call Recording Active 🔴", "Recording started with $callerName")
        } else {
            postNotification("Call Recording Saved", "Call recording with $callerName has completed and saved locally.")
        }
    }

    // Real content provider mapping
    fun syncLocalContacts() {
        val context = getApplication<Application>()
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val resolver = context.contentResolver
                    val cursor = resolver.query(
                        android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(
                            android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER
                        ),
                        null, null, null
                    )
                    cursor?.use { c ->
                        val nameIdx = c.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val numberIdx = c.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (nameIdx >= 0 && numberIdx >= 0) {
                            val currentContacts = contactsRepository.allContacts.first()
                            while (c.moveToNext()) {
                                val name = c.getString(nameIdx) ?: "Unknown"
                                val phone = c.getString(numberIdx) ?: ""
                                if (phone.isNotEmpty()) {
                                    val exists = currentContacts.any { it.phone == phone }
                                    if (!exists) {
                                        contactsRepository.insertContact(Contact(name = name, phone = phone))
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun syncLocalCallLogs() {
        val context = getApplication<Application>()
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val resolver = context.contentResolver
                    val cursor = resolver.query(
                        android.provider.CallLog.Calls.CONTENT_URI,
                        arrayOf(
                            android.provider.CallLog.Calls.CACHED_NAME,
                            android.provider.CallLog.Calls.NUMBER,
                            android.provider.CallLog.Calls.DURATION,
                            android.provider.CallLog.Calls.DATE,
                            android.provider.CallLog.Calls.TYPE
                        ),
                        null, null, "${android.provider.CallLog.Calls.DATE} DESC"
                    )
                    cursor?.use { c ->
                        val nameIdx = c.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME)
                        val numIdx = c.getColumnIndex(android.provider.CallLog.Calls.NUMBER)
                        val durIdx = c.getColumnIndex(android.provider.CallLog.Calls.DURATION)
                        val dateIdx = c.getColumnIndex(android.provider.CallLog.Calls.DATE)
                        val typeIdx = c.getColumnIndex(android.provider.CallLog.Calls.TYPE)
                        if (numIdx >= 0 && durIdx >= 0 && dateIdx >= 0 && typeIdx >= 0) {
                            val currentLogs = callLogRepository.allCallLogs.first()
                            while (c.moveToNext()) {
                                val name = if (nameIdx >= 0) c.getString(nameIdx) else null
                                val num = c.getString(numIdx) ?: ""
                                val dur = c.getLong(durIdx)
                                val date = c.getLong(dateIdx)
                                val type = c.getInt(typeIdx)

                                val minutes = dur / 60
                                val seconds = dur % 60
                                val durStr = String.format("%02d:%02d", minutes, seconds)

                                val callLogType = when (type) {
                                    android.provider.CallLog.Calls.MISSED_TYPE -> CallLog.CallType.MISSED
                                    android.provider.CallLog.Calls.INCOMING_TYPE -> CallLog.CallType.INCOMING
                                    else -> CallLog.CallType.OUTGOING
                                }

                                val exists = currentLogs.any { it.timestamp == date && it.number == num }
                                if (!exists && num.isNotEmpty()) {
                                    callLogRepository.insertCallLog(
                                        CallLog(
                                            name = name,
                                            number = num,
                                            duration = durStr,
                                            timestamp = date,
                                            callType = callLogType
                                        )
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Default dialer package verification
    fun isDefaultDialer(): Boolean {
        val context = getApplication<Application>()
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? android.telecom.TelecomManager
        return telecomManager?.defaultDialerPackage == context.packageName
    }
}
