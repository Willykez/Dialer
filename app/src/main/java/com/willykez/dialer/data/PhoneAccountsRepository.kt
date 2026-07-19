package com.willykez.dialer.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat
import com.willykez.dialer.data.model.SimAccount

class PhoneAccountsRepository(private val context: Context) {

    fun getCallCapableAccounts(): List<SimAccount> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return emptyList()
        }
        return try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            val handles = telecomManager.callCapablePhoneAccounts
            handles.mapNotNull { handle -> resolveAccount(telecomManager, handle) }
        } catch (securityException: SecurityException) {
            emptyList()
        }
    }

    private fun resolveAccount(
        telecomManager: TelecomManager,
        handle: android.telecom.PhoneAccountHandle
    ): SimAccount? {
        return try {
            val account = telecomManager.getPhoneAccount(handle)
            val label = account?.label?.toString()?.takeIf { it.isNotBlank() } ?: handle.id
            SimAccount(handle = handle, label = label)
        } catch (securityException: SecurityException) {
            null
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
