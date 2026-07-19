package com.willykez.dialer.data.model

enum class InCallStatus {
    DIALING, RINGING_INCOMING, RINGING_OUTGOING, ACTIVE, HOLDING, DISCONNECTING, DISCONNECTED
}

data class InCallUiState(
    val callerName: String,
    val number: String,
    val photoUri: String?,
    val status: InCallStatus,
    val startTimestamp: Long,
    val elapsedSeconds: Long = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isOnHold: Boolean = false,
    val canAddCall: Boolean = false,
    val simLabel: String? = null,
    val isMultiSim: Boolean = false
)
