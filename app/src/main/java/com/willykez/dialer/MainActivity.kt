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
import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import com.willykez.dialer.data.model.CallLogEntry
import com.willykez.dialer.data.model.Contact
import com.willykez.dialer.telecom.CallManager
import com.willykez.dialer.ui.calling.CallScreen
import com.willykez.dialer.ui.components.NavigationDock
import com.willykez.dialer.ui.components.SimPickerDialog
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
                var backGestureProgress by remember { mutableFloatStateOf(0f) }

                val activeTab by viewModel.activeTab.collectAsState()
                val isDialpadOpen by viewModel.isDialpadOpen.collectAsState()
                val isSearchOpen by viewModel.isSearchOpen.collectAsState()
                val searchQuery by viewModel.searchQuery.collectAsState()
                val searchResults by viewModel.searchResults.collectAsState()
                val dialpadDigits by viewModel.dialpadDigits.collectAsState()
                val dialpadMatches by viewModel.dialpadMatches.collectAsState()
                val favorites by viewModel.favoriteContacts.collectAsState()
                val recents by viewModel.recentCalls.collectAsState()
                val missedCallBadgeCount by viewModel.missedCallBadgeCount.collectAsState()
                val contacts by viewModel.allContacts.collectAsState()
                val hasContactsPermission by viewModel.hasContactsPermission.collectAsState()
                val hasCallLogPermission by viewModel.hasCallLogPermission.collectAsState()

                val vibrateOnRing by app.settingsRepository.vibrateOnRing.collectAsState()
                val ringtoneUri by app.settingsRepository.ringtoneUri.collectAsState()
                val blockedNumbers by app.blockedNumberRepository.blockedNumbers.collectAsState()

                val ringtoneLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    val uri = result.data?.let {
                        androidx.core.content.IntentCompat.getParcelableExtra(
                            it, RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java
                        )
                    }
                    app.settingsRepository.setRingtoneUri(uri)
                }

                val callState by CallManager.uiState.collectAsState()
                val pendingCallNumber by viewModel.pendingCallNumber.collectAsState()
                val simAccounts by viewModel.simAccounts.collectAsState()
                val preferredSimKey by viewModel.preferredSimKey.collectAsState()
                val isDefaultDialer = viewModel.isDefaultDialer()

                // Back-gesture priority, closest-to-front first: search/dialpad sheets close
                // before the underlying screen navigates; ContactDetail/Settings pop to Home
                // with a predictive-back scale+fade preview while the gesture is in progress.
                BackHandler(enabled = isSearchOpen) { viewModel.setSearchOpen(false) }
                BackHandler(enabled = isDialpadOpen && !isSearchOpen) { viewModel.setDialpadOpen(false) }

                val canPopDetail = screen !is Screen.Home && !isDialpadOpen && !isSearchOpen
                PredictiveBackHandler(enabled = canPopDetail) { progress ->
                    try {
                        progress.collect { event: BackEventCompat ->
                            backGestureProgress = event.progress
                        }
                        backGestureProgress = 0f
                        screen = Screen.Home
                    } catch (e: CancellationException) {
                        backGestureProgress = 0f
                    }
                }

                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    Scaffold(
                        containerColor = Color.Black,
                        bottomBar = {
                            if (screen is Screen.Home) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    AnimatedVisibility(
                                        visible = !isDialpadOpen && !isSearchOpen,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        NavigationDock(
                                            activeTab = activeTab,
                                            onTabSelected = { viewModel.setActiveTab(it) },
                                            isDialpadOpen = isDialpadOpen,
                                            onDialpadToggle = { viewModel.setDialpadOpen(!isDialpadOpen) },
                                            isSearchOpen = isSearchOpen,
                                            onSearchToggle = { viewModel.setSearchOpen(!isSearchOpen) },
                                            missedCallBadgeCount = missedCallBadgeCount
                                        )
                                    }
                                }
                            }
                        }
                    ) { padding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                                .then(
                                    if (screen is Screen.Home) {
                                        Modifier.padding(
                                            top = padding.calculateTopPadding(),
                                            bottom = if (isDialpadOpen) 0.dp else padding.calculateBottomPadding()
                                        )
                                    } else {
                                        // Predictive back preview: content eases back and fades
                                        // slightly as the user drags the system back gesture,
                                        // reverting instantly if the gesture is cancelled.
                                        Modifier
                                            .scale(1f - (backGestureProgress * 0.06f))
                                            .alpha(1f - (backGestureProgress * 0.35f))
                                    }
                                )
                        ) {
                            when (val current = screen) {
                                is Screen.Home -> {
                                    if (isSearchOpen) {
                                        SearchScreen(
                                            query = searchQuery,
                                            results = searchResults,
                                            onQueryChange = viewModel::updateSearchQuery,
                                            onClose = { viewModel.setSearchOpen(false) },
                                            onContactSelected = {
                                                viewModel.setSearchOpen(false)
                                                screen = Screen.ContactDetail(it)
                                            }
                                        )
                                    } else {
                                        HomeScreen(
                                            activeTab = activeTab,
                                            favorites = favorites,
                                            recents = recents,
                                            contacts = contacts,
                                            hasContactsPermission = hasContactsPermission,
                                            hasCallLogPermission = hasCallLogPermission,
                                            isDefaultDialer = isDefaultDialer,
                                            simLabels = simAccounts.associate { it.handle.id to it.label },
                                            onRequestContactsPermission = { requestSpecificPermission(android.Manifest.permission.READ_CONTACTS) },
                                            onRequestCallLogPermission = { requestSpecificPermission(android.Manifest.permission.READ_CALL_LOG) },
                                            onRequestDefaultDialer = { requestDefaultDialerRole() },
                                            onOpenSettings = { screen = Screen.Settings },
                                            onCall = viewModel::placeCall,
                                            onOpenContact = { screen = Screen.ContactDetail(it) },
                                            onOpenCallDetail = { entry: CallLogEntry ->
                                                viewModel.placeCall(entry.number)
                                            },
                                            onDeleteCall = { entry: CallLogEntry ->
                                                viewModel.deleteCallLogEntry(entry.id)
                                            }
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = isDialpadOpen,
                                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    ) {
                                        DialpadScreen(
                                            digits = dialpadDigits,
                                            matchingContacts = dialpadMatches,
                                            onDigit = viewModel::appendDigit,
                                            onBackspace = viewModel::backspaceDigit,
                                            onClearAll = viewModel::clearDigits,
                                            onInsertPlus = { viewModel.appendDigit('+') },
                                            onLongPressOne = { viewModel.callVoicemail() },
                                            speedDialContacts = favorites,
                                            onSpeedDial = { contact ->
                                                viewModel.placeCall(contact.primaryNumber)
                                                viewModel.clearDigits()
                                                viewModel.setDialpadOpen(false)
                                            },
                                            onCall = {
                                                viewModel.placeCall(dialpadDigits)
                                                viewModel.clearDigits()
                                                viewModel.setDialpadOpen(false)
                                            },
                                            onContactPicked = {
                                                viewModel.setDialpadOpen(false)
                                                screen = Screen.ContactDetail(it)
                                            }
                                        )
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
                                    isDefaultDialer = isDefaultDialer,
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
                            onDeclineWithMessage = { message ->
                                CallManager.reject()
                                runCatching {
                                    val smsIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                        data = android.net.Uri.parse("smsto:${state.number}")
                                        putExtra("sms_body", message)
                                    }
                                    startActivity(smsIntent)
                                }
                            },
                            onHangUp = { CallManager.hangUp() },
                            onToggleMute = { CallManager.setMuted(!state.isMuted) },
                            onToggleSpeaker = { CallManager.setSpeakerOn(!state.isSpeakerOn) },
                            onToggleHold = { CallManager.toggleHold() },
                            onDialpadDigit = { CallManager.playDtmf(it) }
                        )
                    }

                    if (pendingCallNumber != null) {
                        SimPickerDialog(
                            accounts = simAccounts,
                            allowAskEachTime = false,
                            onDismiss = { viewModel.cancelPendingCall() },
                            onConfirm = { account, remember -> viewModel.confirmPendingCall(account, remember) }
                        )
                    }

                    if (showSimSettingsPicker) {
                        SimPickerDialog(
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
