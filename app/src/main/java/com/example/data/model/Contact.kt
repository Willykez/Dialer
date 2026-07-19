package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val isFavorite: Boolean = false,
    val sectionHeader: String = name.firstOrNull()?.uppercase() ?: "#",
    val groupId: Long? = null
)
