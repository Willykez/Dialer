package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.calling.ActiveCallScreen
import com.example.ui.components.NavigationDock
import com.example.ui.contacts.ContactsScreen
import com.example.ui.dialpad.DialpadOverlay
import com.example.ui.recents.RecentsScreen
import com.example.ui.search.SearchScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceBackground
import com.example.ui.viewmodel.DialerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DialerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge support is mandatory for design system guidelines
        enableEdgeToEdge()

        val requestPermissionLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[android.Manifest.permission.READ_CONTACTS] == true) {
                viewModel.syncLocalContacts()
            }
            if (permissions[android.Manifest.permission.READ_CALL_LOG] == true) {
                viewModel.syncLocalCallLogs()
            }
        }

        // Trigger permissions request on start
        val permissionsToRequest = mutableListOf(
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.WRITE_CALL_LOG
        )
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())

        // Initial check for existing permissions
        viewModel.syncLocalContacts()
        viewModel.syncLocalCallLogs()

        setContent {
            MyApplicationTheme {
                // Read states from viewmodel
                val activeTab by viewModel.activeTab.collectAsState()
                val isDialpadOpen by viewModel.isDialpadOpen.collectAsState()
                val isSearchOpen by viewModel.isSearchOpen.collectAsState()
                val activeCall by viewModel.activeCall.collectAsState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceBackground)
                ) {
                    // Check active call status. If activeCall != null, overlay the entire screen
                    if (activeCall != null) {
                        ActiveCallScreen(
                            name = activeCall!!.name,
                            number = activeCall!!.number,
                            statusText = activeCall!!.statusText,
                            isRecording = activeCall!!.isRecording,
                            onToggleRecording = { viewModel.toggleRecording() },
                            onDisconnect = { viewModel.endCall() }
                        )
                    } else {
                        // Standard layout
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = SurfaceBackground,
                            bottomBar = {
                                // Floating navigation bar dock at absolute base (with automatic keyboard/insets handling)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Transparent),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    // Render only when dialpad is closed for clean standard phone UX
                                    AnimatedVisibility(
                                        visible = !isDialpadOpen,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        NavigationDock(
                                            activeTab = activeTab,
                                            onTabSelected = { viewModel.setActiveTab(it) },
                                            isDialpadOpen = isDialpadOpen,
                                            onDialpadToggle = { viewModel.setDialpadOpen(!isDialpadOpen) },
                                            isSearchOpen = isSearchOpen,
                                            onSearchToggle = { viewModel.setSearchOpen(!isSearchOpen) }
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        top = innerPadding.calculateTopPadding(),
                                        bottom = if (isDialpadOpen) 0.dp else innerPadding.calculateBottomPadding()
                                    )
                            ) {
                                // Determine current view based on selection search triggers
                                when {
                                    isSearchOpen -> {
                                        SearchScreen(
                                            viewModel = viewModel,
                                            onInitiateCall = { name, number ->
                                                viewModel.startCall(name, number)
                                            }
                                        )
                                    }
                                    activeTab == DialerViewModel.Tab.RECENTS -> {
                                        RecentsScreen(
                                            viewModel = viewModel,
                                            onInitiateCall = { name, number ->
                                                viewModel.startCall(name, number)
                                            }
                                        )
                                    }
                                    activeTab == DialerViewModel.Tab.CONTACTS -> {
                                        ContactsScreen(
                                            viewModel = viewModel,
                                            onInitiateCall = { name, number ->
                                                viewModel.startCall(name, number)
                                            }
                                        )
                                    }
                                }

                                // Match T9 keypad sliding overlay (slide up smoothly above layout viewport)
                                AnimatedVisibility(
                                    visible = isDialpadOpen,
                                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                ) {
                                    DialpadOverlay(
                                        viewModel = viewModel,
                                        onCallClick = { name, number ->
                                            viewModel.startCall(name, number)
                                        },
                                        onCloseDialpad = { viewModel.setDialpadOpen(false) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
