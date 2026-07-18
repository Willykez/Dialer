package com.willykez.dialer.data

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import com.willykez.dialer.data.model.Contact
import com.willykez.dialer.data.model.PhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ContactsRepository(private val context: Context) {

    fun observeContacts(): Flow<Unit> = callbackFlow {
        trySend(Unit)
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        context.contentResolver.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI,
            true,
            observer
        )
        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }

    suspend fun loadAllContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val results = LinkedHashMap<Long, MutableList<PhoneNumber>>()
        val meta = HashMap<Long, Contact>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.STARRED,
            ContactsContract.CommonDataKinds.Phone.CUSTOM_RINGTONE
        )

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} ASC"
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val lookupIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)
            val nameIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
            val numberIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val typeIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)
            val photoIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            val starredIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.STARRED)
            val ringtoneIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CUSTOM_RINGTONE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIdx)
                val number = cursor.getString(numberIdx) ?: continue
                if (number.isBlank()) continue

                val typeLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                    context.resources,
                    cursor.getInt(typeIdx),
                    ""
                ).toString()

                results.getOrPut(id) { mutableListOf() }.add(PhoneNumber(number, typeLabel))

                if (!meta.containsKey(id)) {
                    meta[id] = Contact(
                        contactId = id,
                        lookupKey = cursor.getString(lookupIdx).orEmpty(),
                        displayName = cursor.getString(nameIdx) ?: number,
                        numbers = emptyList(),
                        photoUri = cursor.getString(photoIdx),
                        isFavorite = cursor.getInt(starredIdx) == 1,
                        hasCustomRingtone = !cursor.getString(ringtoneIdx).isNullOrBlank()
                    )
                }
            }
        }

        meta.values
            .map { it.copy(numbers = results[it.contactId].orEmpty().distinctBy { n -> n.number }) }
            .sortedBy { it.displayName.lowercase() }
    }

    suspend fun findContactByNumber(number: String): Contact? {
        return loadAllContacts().find { contact ->
            contact.numbers.any { PhoneNumberUtil.normalizeLoose(it.number) == PhoneNumberUtil.normalizeLoose(number) }
        }
    }

    suspend fun setFavorite(contactId: Long, favorite: Boolean) = withContext(Dispatchers.IO) {
        val values = android.content.ContentValues().apply {
            put(ContactsContract.Contacts.STARRED, if (favorite) 1 else 0)
        }
        val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        context.contentResolver.update(uri, values, null, null)
    }

    suspend fun setCustomRingtone(contactId: Long, ringtoneUri: Uri?) = withContext(Dispatchers.IO) {
        val values = android.content.ContentValues().apply {
            put(ContactsContract.Contacts.CUSTOM_RINGTONE, ringtoneUri?.toString())
        }
        val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        context.contentResolver.update(uri, values, null, null)
    }

    fun contactEditIntentUri(lookupKey: String, contactId: Long): Uri =
        Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, "$lookupKey/$contactId")

    fun observeContactsAsFlow(): Flow<List<Contact>> = observeContacts().map { loadAllContacts() }
}

object PhoneNumberUtil {
    fun normalizeLoose(number: String): String {
        val digitsOnly = number.filter { it.isDigit() }
        return if (digitsOnly.length > 9) digitsOnly.takeLast(9) else digitsOnly
    }
}
