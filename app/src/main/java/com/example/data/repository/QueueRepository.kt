package com.example.data.repository

import com.example.data.database.QueueDao
import com.example.data.model.QueueItem
import kotlinx.coroutines.flow.Flow

class QueueRepository(private val queueDao: QueueDao) {
    val allQueueItems: Flow<List<QueueItem>> = queueDao.getAllQueueItemsFlow()
    val totalCount: Flow<Int> = queueDao.getTotalCountFlow()

    fun getCountByStatusFlow(status: String): Flow<Int> = queueDao.getCountByStatusFlow(status)

    suspend fun getCountByStatus(status: String): Int = queueDao.getCountByStatus(status)

    suspend fun getPendingQueueItems(): List<QueueItem> = queueDao.getPendingQueueItems()

    suspend fun insertQueueItem(item: QueueItem): Long = queueDao.insertQueueItem(item)

    suspend fun updateQueueItem(item: QueueItem) = queueDao.updateQueueItem(item)

    suspend fun clearAllQueue() = queueDao.clearAllQueue()

    suspend fun findDuplicate(packageName: String, title: String, text: String, postTime: Long): QueueItem? {
        return queueDao.findDuplicate(packageName, title, text, postTime)
    }
}
