package com.willykez.dialer.data.model

data class PhoneNumber(
    val number: String,
    val type: String
)

data class Contact(
    val contactId: Long,
    val lookupKey: String,
    val displayName: String,
    val numbers: List<PhoneNumber>,
    val photoUri: String?,
    val isFavorite: Boolean,
    val hasCustomRingtone: Boolean
) {
    val primaryNumber: String
        get() = numbers.firstOrNull()?.number.orEmpty()

    val initials: String
        get() = displayName
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "#" }
}
