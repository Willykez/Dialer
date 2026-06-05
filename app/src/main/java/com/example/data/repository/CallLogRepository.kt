package com.example.data.repository

import com.example.data.dao.CallLogDao
import com.example.data.model.CallLog
import kotlinx.coroutines.flow.Flow

class CallLogRepository(private val callLogDao: CallLogDao) {
    val allCallLogs: Flow<List<CallLog>> = callLogDao.getAllCallLogs()

    suspend fun insertCallLog(callLog: CallLog) {
        callLogDao.insertCallLog(callLog)
    }

    suspend fun deleteCallLog(callLog: CallLog) {
        callLogDao.deleteCallLog(callLog)
    }

    suspend fun clearLog() {
        callLogDao.clearAllCallLogs()
    }
}
