package com.willykez.dialer.data

import android.content.Context
import android.telecom.TelecomManager
import com.willykez.dialer.data.model.SimAccount

class PhoneAccountsRepository(private val context: Context) {

    fun getCallCapableAccounts(): List<SimAccount> {
        return try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            telecomManager.callCapablePhoneAccounts.mapNotNull { handle ->
                val account = telecomManager.getPhoneAccount(handle)
                val label = account?.label?.toString()?.takeIf { it.isNotBlank() } ?: handle.id
                SimAccount(handle = handle, label = label)
            }
        } catch (securityException: SecurityException) {
            emptyList()
        }
    }

    fun hasMultipleSims(): Boolean = getCallCapableAccounts().size > 1

    fun findByStorageKey(key: String?): SimAccount? {
        if (key.isNullOrBlank()) return null
        return getCallCapableAccounts().find { it.storageKey == key }
    }

    fun findById(accountId: String?): SimAccount? {
        if (accountId.isNullOrBlank()) return null
        return getCallCapableAccounts().find { it.handle.id == accountId }
    }
}
