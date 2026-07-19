package com.willykez.dialer.data

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import com.willykez.dialer.data.model.CallDirection
import com.willykez.dialer.data.model.CallLogEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CallLogRepository(private val context: Context) {

    fun observeCallLog(): Flow<Unit> = callbackFlow {
        trySend(Unit)
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        context.contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, observer)
        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }

    fun observeCallLogAsFlow(limit: Int = 200): Flow<List<CallLogEntry>> =
        observeCallLog().map { loadRecentCalls(limit) }

    suspend fun loadRecentCalls(limit: Int = 200): List<CallLogEntry> = withContext(Dispatchers.IO) {
        val entries = mutableListOf<CallLogEntry>()
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.CACHED_PHOTO_URI,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.CACHED_MATCHED_NUMBER,
            CallLog.Calls.PHONE_ACCOUNT_ID
        )

        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            "${CallLog.Calls.DATE} DESC"
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(CallLog.Calls._ID)
            val numberIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val nameIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
            val photoIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_PHOTO_URI)
            val typeIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)
            val dateIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
            val durationIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)
            val phoneAccountIdIdx = cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)

            while (cursor.moveToNext() && entries.size < limit) {
                val type = cursor.getInt(typeIdx)
                val direction = when (type) {
                    CallLog.Calls.INCOMING_TYPE -> CallDirection.INCOMING
                    CallLog.Calls.OUTGOING_TYPE -> CallDirection.OUTGOING
                    CallLog.Calls.MISSED_TYPE -> CallDirection.MISSED
                    CallLog.Calls.REJECTED_TYPE -> CallDirection.REJECTED
                    CallLog.Calls.BLOCKED_TYPE -> CallDirection.BLOCKED
                    CallLog.Calls.VOICEMAIL_TYPE -> CallDirection.VOICEMAIL
                    else -> CallDirection.OUTGOING
                }

                entries += CallLogEntry(
                    id = cursor.getLong(idIdx),
                    number = cursor.getString(numberIdx) ?: "",
                    cachedName = cursor.getString(nameIdx),
                    photoUri = cursor.getString(photoIdx),
                    direction = direction,
                    timestamp = cursor.getLong(dateIdx),
                    durationSeconds = cursor.getLong(durationIdx),
                    contactId = null,
                    phoneAccountId = if (phoneAccountIdIdx >= 0) cursor.getString(phoneAccountIdIdx) else null
                )
            }
        }
        entries
    }

    suspend fun deleteEntry(id: Long) = withContext(Dispatchers.IO) {
        context.contentResolver.delete(
            CallLog.Calls.CONTENT_URI,
            "${CallLog.Calls._ID} = ?",
            arrayOf(id.toString())
        )
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        context.contentResolver.delete(CallLog.Calls.CONTENT_URI, null, null)
    }
}
