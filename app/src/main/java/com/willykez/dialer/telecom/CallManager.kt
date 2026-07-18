@file:Suppress("DEPRECATION")

package com.willykez.dialer.telecom

import android.content.Context
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.DisconnectCause
import android.telecom.InCallService
import android.telecom.TelecomManager
import com.willykez.dialer.data.model.InCallStatus
import com.willykez.dialer.data.model.InCallUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CallManager {

    private var primaryCall: Call? = null
    private var secondaryCall: Call? = null
    private var service: InCallService? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun attachService(inCallService: InCallService) {
        service = inCallService
    }

    fun detachService(inCallService: InCallService) {
        if (service == inCallService) {
            service = null
        }
    }

    private val _uiState = MutableStateFlow<InCallUiState?>(null)
    val uiState: StateFlow<InCallUiState?> = _uiState.asStateFlow()

    private val _lastDisconnectCause = MutableStateFlow<DisconnectCause?>(null)
    val lastDisconnectCause: StateFlow<DisconnectCause?> = _lastDisconnectCause.asStateFlow()

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            refreshState()
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) {
            refreshState()
        }
    }

    fun onCallAdded(call: Call) {
        if (primaryCall == null || primaryCall?.state == Call.STATE_DISCONNECTED) {
            primaryCall = call
        } else if (secondaryCall == null) {
            secondaryCall = call
        }
        call.registerCallback(callback)
        refreshState()
    }

    fun onCallRemoved(call: Call) {
        call.unregisterCallback(callback)
        if (call == primaryCall) {
            _lastDisconnectCause.value = call.details?.disconnectCause
            primaryCall = secondaryCall
            secondaryCall = null
        } else if (call == secondaryCall) {
            secondaryCall = null
        }
        refreshState()
    }

    private fun refreshState() {
        val call = primaryCall
        if (call == null) {
            _uiState.value = null
            return
        }

        val details = call.details
        val number = details?.handle?.schemeSpecificPart.orEmpty()
        val callerName = details?.callerDisplayName?.takeIf { it.isNotBlank() } ?: number

        val status = when (call.state) {
            Call.STATE_RINGING -> InCallStatus.RINGING_INCOMING
            Call.STATE_DIALING, Call.STATE_CONNECTING -> InCallStatus.RINGING_OUTGOING
            Call.STATE_ACTIVE -> InCallStatus.ACTIVE
            Call.STATE_HOLDING -> InCallStatus.HOLDING
            Call.STATE_DISCONNECTING -> InCallStatus.DISCONNECTING
            Call.STATE_DISCONNECTED -> InCallStatus.DISCONNECTED
            else -> InCallStatus.DIALING
        }

        val existing = _uiState.value
        val simLabel = resolveSimLabel(details?.accountHandle)
        _uiState.value = InCallUiState(
            callerName = callerName,
            number = number,
            photoUri = null,
            status = status,
            startTimestamp = existing?.takeIf { it.number == number }?.startTimestamp
                ?: System.currentTimeMillis(),
            isMuted = existing?.isMuted ?: false,
            isSpeakerOn = existing?.isSpeakerOn ?: false,
            isOnHold = call.state == Call.STATE_HOLDING,
            canAddCall = details?.can(Call.Details.CAPABILITY_HOLD) == true,
            simLabel = simLabel,
            isMultiSim = hasMultipleSims()
        )
    }

    private fun resolveSimLabel(handle: android.telecom.PhoneAccountHandle?): String? {
        if (handle == null) return null
        val context = appContext ?: return null
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_PHONE_STATE
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        return try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            telecomManager.getPhoneAccount(handle)?.label?.toString()
        } catch (securityException: SecurityException) {
            null
        }
    }

    private fun hasMultipleSims(): Boolean {
        val context = appContext ?: return false
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_PHONE_STATE
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            telecomManager.callCapablePhoneAccounts.size > 1
        } catch (securityException: SecurityException) {
            false
        }
    }

    fun answer() {
        primaryCall?.answer(0)
    }

    fun reject() {
        primaryCall?.reject(false, null)
    }

    fun hangUp() {
        primaryCall?.disconnect()
    }

    fun toggleHold() {
        val call = primaryCall ?: return
        if (call.state == Call.STATE_HOLDING) {
            call.unhold()
        } else {
            call.hold()
        }
    }

    fun setMuted(muted: Boolean) {
        service?.setMuted(muted)
        _uiState.value = _uiState.value?.copy(isMuted = muted)
    }

    fun setSpeakerOn(enabled: Boolean) {
        val route = if (enabled) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
        service?.setAudioRoute(route)
        _uiState.value = _uiState.value?.copy(isSpeakerOn = enabled)
    }

    fun playDtmf(digit: Char) {
        primaryCall?.playDtmfTone(digit)
        primaryCall?.stopDtmfTone()
    }

    fun hasActiveCall(): Boolean = primaryCall != null
}
