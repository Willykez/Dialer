package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String?,
    val number: String,
    val duration: String, // e.g. "01:23"
    val timestamp: Long, // epoch millis
    val callType: CallType,
    val isRecorded: Boolean = false
) {
    enum class CallType {
        INCOMING, OUTGOING, MISSED
    }
}
