package com.example.data.repository

import com.example.data.database.LogDao
import com.example.data.model.LogItem
import kotlinx.coroutines.flow.Flow

class LogRepository(private val logDao: LogDao) {
    val allLogs: Flow<List<LogItem>> = logDao.getAllLogsFlow()

    suspend fun insertLog(logItem: LogItem) = logDao.insertLog(logItem)

    suspend fun clearLogs() = logDao.clearLogs()

    suspend fun getLastLog(): LogItem? = logDao.getLastLog()
}
