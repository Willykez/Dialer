package com.example.data.dao

import androidx.room.*
import com.example.data.model.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE name LIKE :query OR phone LIKE :query ORDER BY name ASC")
    fun searchContacts(query: String): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("DELETE FROM contacts")
    suspend fun clearAllContacts()

    @Query("UPDATE contacts SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE contacts SET groupId = :groupId WHERE id = :id")
    suspend fun updateContactGroup(id: Long, groupId: Long?)

    @Query("SELECT * FROM contacts WHERE groupId = :groupId ORDER BY name ASC")
    fun getContactsByGroupId(groupId: Long): Flow<List<Contact>>
}
