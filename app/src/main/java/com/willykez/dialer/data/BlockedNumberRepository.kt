package com.willykez.dialer.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BlockedNumberRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("dialer_blocked_numbers", Context.MODE_PRIVATE)

    private val _blockedNumbers = MutableStateFlow(loadFromPrefs())
    val blockedNumbers: StateFlow<Set<String>> = _blockedNumbers.asStateFlow()

    private fun loadFromPrefs(): Set<String> =
        prefs.getStringSet(KEY_NUMBERS, emptySet())?.toSet() ?: emptySet()

    fun isBlocked(number: String): Boolean {
        val normalized = PhoneNumberUtil.normalizeLoose(number)
        return _blockedNumbers.value.any { PhoneNumberUtil.normalizeLoose(it) == normalized }
    }

    fun block(number: String) {
        val updated = _blockedNumbers.value + number
        persist(updated)
    }

    fun unblock(number: String) {
        val updated = _blockedNumbers.value.filterNot {
            PhoneNumberUtil.normalizeLoose(it) == PhoneNumberUtil.normalizeLoose(number)
        }.toSet()
        persist(updated)
    }

    private fun persist(updated: Set<String>) {
        prefs.edit().putStringSet(KEY_NUMBERS, updated).apply()
        _blockedNumbers.value = updated
    }

    companion object {
        private const val KEY_NUMBERS = "blocked_numbers"
    }
}
