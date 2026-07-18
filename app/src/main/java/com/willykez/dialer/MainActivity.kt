package com.willykez.dialer

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.telecom.TelecomManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.willykez.dialer.data.model.CallLogEntry
import com.willykez.dialer.data.model.Contact
import com.willykez.dialer.telecom.CallManager
import com.willykez.dialer.ui.calling.CallScreen
import com.willykez.dialer.ui.components.DialerBottomBar
import com.willykez.dialer.ui.contacts.ContactDetailScreen
import com.willykez.dialer.ui.dialpad.DialpadScreen
import com.willykez.dialer.ui.home.HomeScreen
import com.willykez.dialer.ui.search.SearchScreen
import com.willykez.dialer.ui.settings.SettingsScreen
import com.willykez.dialer.ui.theme.DialerTheme
import com.willykez.dialer.ui.viewmodel.DialerViewModel

private sealed interface Screen {
    data object Home : Screen
    data class ContactDetail(val contact: Contact) : Screen
    data object Settings : Screen
}

class MainActivity : ComponentActivity() {

    private val viewModel: DialerViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshPermissions()
    }

    private val defaultDialerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.refreshPermissions()
        requestCorePermissions()

        setContent {
            DialerTheme {
                val app = application as DialerApplication
                var screen by remember { mutableStateOf<Screen>(Screen.Home) }
                var showSimSettingsPicker by remember { mutableStateOf(false) }

                val activeTab by viewModel.activeTab.collectAsState()
                val isDialpadOpen by viewModel.isDialpadOpen.collectAsState()
                val isSearchOpen by viewModel.isSearchOpen.collectAsState()
                val searchQuery by viewModel.searchQuery.collectAsState()
                val searchResults by viewModel.searchResults.collectAsState()
                val dialpadDigits by viewModel.dialpadDigits.collectAsState()
                val dialpadMatches by viewModel.dialpadMatches.collectAsState()
                val favorites by viewModel.favoriteContacts.collectAsState()
                val recents by viewModel.recentCalls.collectAsState()
                val contacts by viewModel.allContacts.collectAsState()
                val hasContactsPermission by viewModel.hasContactsPermission.collectAsState()
                val hasCallLogPermission by viewModel.hasCallLogPermission.collectAsState()

                val vibrateOnRing by app.settingsRepository.vibrateOnRing.collectAsState()
                val ringtoneUri by app.settingsRepository.ringtoneUri.collectAsState()
                val blockedNumbers by app.blockedNumberRepository.blockedNumbers.collectAsState()

                val ringtoneLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                    app.settingsRepository.setRingtoneUri(uri)
                }

                val callState by CallManager.uiState.collectAsState()
                val pendingCallNumber by viewModel.pendingCallNumber.collectAsState()
                val simAccounts by viewModel.simAccounts.collectAsState()
                val preferredSimKey by viewModel.preferredSimKey.collectAsState()

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        bottomBar = {
                            if (screen is Screen.Home && !isSearchOpen) {
                                DialerBottomBar(
                                    activeTab = activeTab,
                                    onTabSelected = {
                                        viewModel.setActiveTab(it)
                                        viewModel.setDialpadOpen(false)
                                    },
                                    isDialpadOpen = isDialpadOpen,
                                    onDialpadToggle = { viewModel.setDialpadOpen(!isDialpadOpen) },
                                    onSearchClick = { viewModel.setSearchOpen(true) }
                                )
                            }
                        }
                    ) { padding ->
                        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                            when (val current = screen) {
                                is Screen.Home -> {
                                    when {
                                        isSearchOpen -> SearchScreen(
                                            query = searchQuery,
                                            results = searchResults,
                                            onQueryChange = viewModel::updateSearchQuery,
                                            onClose = { viewModel.setSearchOpen(false) },
                                            onContactSelected = {
                                                viewModel.setSearchOpen(false)
                                                screen = Screen.ContactDetail(it)
                                            }
                                        )
                                        isDialpadOpen -> DialpadScreen(
                                            digits = dialpadDigits,
                                            matchingContacts = dialpadMatches,
                                            onDigit = viewModel::appendDigit,
                                            onBackspace = viewModel::backspaceDigit,
                                            onLongBackspace = viewModel::clearDigits,
                                            onCall = {
                                                viewModel.placeCall(dialpadDigits)
                                                viewModel.clearDigits()
                                                viewModel.setDialpadOpen(false)
                                            },
                                            onContactPicked = { screen = Screen.ContactDetail(it) }
                                        )
                                        else -> Column(modifier = Modifier.fillMaxSize()) {
                                            androidx.compose.foundation.layout.Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                            ) {
                                                androidx.compose.material3.Text(
                                                    text = "Phone",
                                                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                androidx.compose.material3.IconButton(onClick = { screen = Screen.Settings }) {
                                                    androidx.compose.material3.Icon(
                                                        Icons.Filled.Settings,
                                                        contentDescription = "Settings"
                                                    )
                                                }
                                            }
                                            HomeScreen(
                                                activeTab = activeTab,
                                                onTabChanged = viewModel::setActiveTab,
                                                favorites = favorites,
                                                recents = recents,
                                                contacts = contacts,
                                                hasContactsPermission = hasContactsPermission,
                                                hasCallLogPermission = hasCallLogPermission,
                                                simLabels = simAccounts.associate { it.handle.id to it.label },
                                                onRequestContactsPermission = { requestSpecificPermission(android.Manifest.permission.READ_CONTACTS) },
                                                onRequestCallLogPermission = { requestSpecificPermission(android.Manifest.permission.READ_CALL_LOG) },
                                                onCall = viewModel::placeCall,
                                                onOpenContact = { screen = Screen.ContactDetail(it) },
                                                onOpenCallDetail = { entry: CallLogEntry ->
                                                    viewModel.placeCall(entry.number)
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                                is Screen.ContactDetail -> ContactDetailScreen(
                                    contact = current.contact,
                                    isBlocked = app.blockedNumberRepository.isBlocked(current.contact.primaryNumber),
                                    onBack = { screen = Screen.Home },
                                    onCall = viewModel::placeCall,
                                    onMessage = { number ->
                                        startActivity(
                                            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number"))
                                        )
                                    },
                                    onEdit = {
                                        val uri = app.contactsRepository.contactEditIntentUri(
                                            current.contact.lookupKey,
                                            current.contact.contactId
                                        )
                                        startActivity(Intent(Intent.ACTION_EDIT).apply {
                                            setDataAndType(uri, ContactsContract.Contacts.CONTENT_ITEM_TYPE)
                                        })
                                    },
                                    onToggleFavorite = { viewModel.toggleFavorite(current.contact) },
                                    onPickRingtone = {
                                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                        }
                                        ringtoneLauncher.launch(intent)
                                    },
                                    onToggleBlock = {
                                        val number = current.contact.primaryNumber
                                        if (app.blockedNumberRepository.isBlocked(number)) {
                                            app.blockedNumberRepository.unblock(number)
                                        } else {
                                            app.blockedNumberRepository.block(number)
                                        }
                                    }
                                )
                                is Screen.Settings -> SettingsScreen(
                                    isDefaultDialer = viewModel.isDefaultDialer(),
                                    ringtoneLabel = ringtoneUri?.let {
                                        RingtoneManager.getRingtone(this@MainActivity, it)?.getTitle(this@MainActivity)
                                    } ?: "Default",
                                    vibrateOnRing = vibrateOnRing,
                                    blockedNumbers = blockedNumbers.toList(),
                                    showSimRow = simAccounts.size > 1,
                                    simPreferenceLabel = simAccounts.find { it.storageKey == preferredSimKey }?.label
                                        ?: "Ask every time",
                                    onBack = { screen = Screen.Home },
                                    onRequestDefaultDialer = { requestDefaultDialerRole() },
                                    onPickRingtone = {
                                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri)
                                        }
                                        ringtoneLauncher.launch(intent)
                                    },
                                    onToggleVibrate = { app.settingsRepository.setVibrateOnRing(it) },
                                    onUnblock = { app.blockedNumberRepository.unblock(it) },
                                    onPickDefaultSim = { showSimSettingsPicker = true }
                                )
                            }
                        }
                    }

                    callState?.let { state ->
                        CallScreen(
                            state = state,
                            onAnswer = { CallManager.answer() },
                            onDecline = { CallManager.reject() },
                            onHangUp = { CallManager.hangUp() },
                            onToggleMute = { CallManager.setMuted(!state.isMuted) },
                            onToggleSpeaker = { CallManager.setSpeakerOn(!state.isSpeakerOn) },
                            onToggleHold = { CallManager.toggleHold() },
                            onDialpadDigit = { CallManager.playDtmf(it) }
                        )
                    }

                    if (pendingCallNumber != null) {
                        com.willykez.dialer.ui.components.SimPickerDialog(
                            accounts = simAccounts,
                            allowAskEachTime = false,
                            onDismiss = { viewModel.cancelPendingCall() },
                            onConfirm = { account, remember -> viewModel.confirmPendingCall(account, remember) }
                        )
                    }

                    if (showSimSettingsPicker) {
                        com.willykez.dialer.ui.components.SimPickerDialog(
                            accounts = simAccounts,
                            allowAskEachTime = true,
                            onDismiss = { showSimSettingsPicker = false },
                            onConfirm = { account, _ ->
                                viewModel.setPreferredSimKey(account?.storageKey)
                                showSimSettingsPicker = false
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
        viewModel.refreshSimAccounts()
    }

    private fun requestCorePermissions() {
        val permissions = mutableListOf(
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.WRITE_CALL_LOG,
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.READ_PHONE_STATE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            permissions += android.Manifest.permission.ANSWER_PHONE_CALLS
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += android.Manifest.permission.POST_NOTIFICATIONS
        }
        val notGranted = permissions.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    private fun requestSpecificPermission(permission: String) {
        permissionLauncher.launch(arrayOf(permission))
    }

    private fun requestDefaultDialerRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
            ) {
                defaultDialerLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER))
            }
        } else {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            if (telecomManager.defaultDialerPackage != packageName) {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                    putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                }
                defaultDialerLauncher.launch(intent)
            }
        }
    }
}
