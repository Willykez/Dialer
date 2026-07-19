package com.willykez.dialer.data.model

enum class CallDirection {
    INCOMING, OUTGOING, MISSED, REJECTED, BLOCKED, VOICEMAIL
}

data class CallLogEntry(
    val id: Long,
    val number: String,
    val cachedName: String?,
    val photoUri: String?,
    val direction: CallDirection,
    val timestamp: Long,
    val durationSeconds: Long,
    val contactId: Long?,
    val phoneAccountId: String? = null
) {
    val displayName: String
        get() = cachedName?.takeIf { it.isNotBlank() } ?: number.ifBlank { "Unknown" }
}
