package com.example.data.repository

import com.example.data.dao.ContactGroupDao
import com.example.data.model.ContactGroup
import kotlinx.coroutines.flow.Flow

class ContactGroupRepository(private val contactGroupDao: ContactGroupDao) {
    val allGroups: Flow<List<ContactGroup>> = contactGroupDao.getAllGroups()

    suspend fun insertGroup(group: ContactGroup): Long {
        return contactGroupDao.insertGroup(group)
    }

    suspend fun updateGroup(group: ContactGroup) {
        contactGroupDao.updateGroup(group)
    }

    suspend fun deleteGroup(group: ContactGroup) {
        contactGroupDao.deleteGroup(group)
    }

    suspend fun getGroupById(id: Long): ContactGroup? {
        return contactGroupDao.getGroupById(id)
    }
}
