package com.example.data.dao

import androidx.room.*
import com.example.data.model.ContactGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactGroupDao {
    @Query("SELECT * FROM contact_groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<ContactGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: ContactGroup): Long

    @Update
    suspend fun updateGroup(group: ContactGroup)

    @Delete
    suspend fun deleteGroup(group: ContactGroup)

    @Query("SELECT * FROM contact_groups WHERE id = :id")
    suspend fun getGroupById(id: Long): ContactGroup?
}
