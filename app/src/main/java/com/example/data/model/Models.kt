package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queue_items")
data class QueueItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val bigText: String,
    val subText: String,
    val ticker: String,
    val category: String,
    val notificationId: Int,
    val tag: String,
    val groupKey: String,
    val channelId: String,
    val postTime: Long,
    val receivedAt: Long = System.currentTimeMillis(),
    val status: String, // "Pending", "Sending", "Success", "Retry", "Failed"
    val retryCount: Int = 0,
    val lastError: String? = null
)

@Entity(tableName = "app_configs")
data class AppConfig(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isEnabled: Boolean = true
)

@Entity(tableName = "log_items")
data class LogItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // "Success", "Failed", "Retry"
    val url: String,
    val responseCode: Int,
    val responseBody: String?,
    val error: String?,
    val retryCount: Int,
    val duration: Long,
    val appName: String = "",
    val packageName: String = ""
)
