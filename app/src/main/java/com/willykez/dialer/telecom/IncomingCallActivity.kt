package com.willykez.dialer.telecom

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.willykez.dialer.data.model.InCallStatus
import com.willykez.dialer.ui.calling.CallScreen
import com.willykez.dialer.ui.theme.DialerTheme

class IncomingCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        } else {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        setContent {
            DialerTheme {
                val state by CallManager.uiState.collectAsState()

                LaunchedEffect(state?.status) {
                    if (state == null || state?.status == InCallStatus.DISCONNECTED) {
                        finish()
                    }
                }

                state?.let { callState ->
                    CallScreen(
                        state = callState,
                        onAnswer = { CallManager.answer() },
                        onDecline = {
                            CallManager.reject()
                            finish()
                        },
                        onHangUp = {
                            CallManager.hangUp()
                        },
                        onToggleMute = { CallManager.setMuted(!callState.isMuted) },
                        onToggleSpeaker = { CallManager.setSpeakerOn(!callState.isSpeakerOn) },
                        onToggleHold = { CallManager.toggleHold() },
                        onDialpadDigit = { CallManager.playDtmf(it) }
                    )
                }
            }
        }
    }
}
