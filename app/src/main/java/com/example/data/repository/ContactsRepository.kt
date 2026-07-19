package com.example.data.repository

import com.example.data.dao.ContactDao
import com.example.data.model.Contact
import kotlinx.coroutines.flow.Flow

class ContactsRepository(private val contactDao: ContactDao) {
    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()

    fun searchContacts(query: String): Flow<List<Contact>> {
        return contactDao.searchContacts("%$query%")
    }

    suspend fun insertContact(contact: Contact) {
        contactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: Contact) {
        contactDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: Contact) {
        contactDao.deleteContact(contact)
    }

    suspend fun clearAll() {
        contactDao.clearAllContacts()
    }

    suspend fun updateFavorite(id: Long, isFavorite: Boolean) {
        contactDao.updateFavorite(id, isFavorite)
    }

    suspend fun updateContactGroup(id: Long, groupId: Long?) {
        contactDao.updateContactGroup(id, groupId)
    }

    fun getContactsByGroupId(groupId: Long): Flow<List<Contact>> {
        return contactDao.getContactsByGroupId(groupId)
    }
}
