package com.example.data.database

import androidx.room.*
import com.example.data.model.AppConfig
import com.example.data.model.LogItem
import com.example.data.model.QueueItem
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {
    @Query("SELECT * FROM queue_items ORDER BY receivedAt DESC")
    fun getAllQueueItemsFlow(): Flow<List<QueueItem>>

    @Query("SELECT * FROM queue_items WHERE status = 'Pending' OR status = 'Retry' ORDER BY receivedAt ASC")
    suspend fun getPendingQueueItems(): List<QueueItem>

    @Query("SELECT * FROM queue_items WHERE id = :id")
    suspend fun getQueueItemById(id: Int): QueueItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItem(item: QueueItem): Long

    @Update
    suspend fun updateQueueItem(item: QueueItem)

    @Query("DELETE FROM queue_items")
    suspend fun clearAllQueue()

    @Query("SELECT COUNT(*) FROM queue_items WHERE status = :status")
    fun getCountByStatusFlow(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM queue_items WHERE status = :status")
    suspend fun getCountByStatus(status: String): Int

    @Query("SELECT COUNT(*) FROM queue_items")
    fun getTotalCountFlow(): Flow<Int>

    @Query("SELECT * FROM queue_items WHERE packageName = :packageName AND title = :title AND text = :text AND ABS(postTime - :postTime) < 5000 LIMIT 1")
    suspend fun findDuplicate(packageName: String, title: String, text: String, postTime: Long): QueueItem?
}

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_configs ORDER BY appName ASC")
    fun getAllAppConfigsFlow(): Flow<List<AppConfig>>

    @Query("SELECT * FROM app_configs")
    suspend fun getAllAppConfigs(): List<AppConfig>

    @Query("SELECT * FROM app_configs WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppConfig(packageName: String): AppConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppConfig(appConfig: AppConfig)

    @Query("DELETE FROM app_configs WHERE packageName = :packageName")
    suspend fun deleteAppConfigByPackage(packageName: String)
}

@Dao
interface LogDao {
    @Query("SELECT * FROM log_items ORDER BY timestamp DESC LIMIT 500")
    fun getAllLogsFlow(): Flow<List<LogItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(logItem: LogItem)

    @Query("DELETE FROM log_items")
    suspend fun clearLogs()

    @Query("SELECT * FROM log_items ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastLog(): LogItem?
}
