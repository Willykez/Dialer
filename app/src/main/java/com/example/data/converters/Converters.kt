package com.example.data.converters

import androidx.room.TypeConverter
import com.example.data.model.CallLog

class Converters {
    @TypeConverter
    fun fromCallType(value: CallLog.CallType): String {
        return value.name
    }

    @TypeConverter
    fun toCallType(value: String): CallLog.CallType {
        return CallLog.CallType.valueOf(value)
    }
}
