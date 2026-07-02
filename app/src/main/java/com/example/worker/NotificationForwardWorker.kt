package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.NotifBridgeApp
import com.example.data.model.LogItem
import com.example.data.model.QueueItem
import java.util.concurrent.TimeUnit

class NotificationForwardWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "NotifForwardWorker"
        private const val UNIQUE_PERIODIC_WORK_NAME = "PeriodicNotificationForwardWork"
        private const val UNIQUE_ONETIME_WORK_NAME = "OneTimeNotificationForwardWork"

        fun enqueuePeriodicWork(context: Context, intervalMinutes: Int) {
            val workManager = WorkManager.getInstance(context)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicWork = PeriodicWorkRequestBuilder<NotificationForwardWorker>(
                intervalMinutes.toLong(), TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWork
            )
            Log.d(TAG, "Periodic Work Enqueued: $intervalMinutes mins")
        }

        fun enqueueOneTimeWork(context: Context) {
            val workManager = WorkManager.getInstance(context)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeWork = OneTimeWorkRequestBuilder<NotificationForwardWorker>()
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniqueWork(
                UNIQUE_ONETIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeWork
            )
            Log.d(TAG, "One-Time Work Enqueued")
        }

        fun cancelPeriodicWork(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(UNIQUE_PERIODIC_WORK_NAME)
            Log.d(TAG, "Periodic Work Cancelled")
        }
    }

    override suspend fun doWork(): Result {
        val app = applicationContext as NotifBridgeApp
        val queueRepo = app.queueRepository
        val logRepo = app.logRepository
        val settings = app.settingsRepository
        val sender = app.webhookSender

        Log.d(TAG, "Worker running, reading Queue...")

        try {
            val pendingItems = queueRepo.getPendingQueueItems()
            if (pendingItems.isEmpty()) {
                Log.d(TAG, "Queue is empty, nothing to send.")
                return Result.success()
            }

            for (item in pendingItems) {
                // If the item has Retry status, check if the backoff period has passed
                if (item.status == "Retry") {
                    val initialDelayMs = settings.retryDelay * 1000L
                    val multiplier = Math.pow(2.0, (item.retryCount - 1).toDouble()).toLong()
                    val backoffDelayMs = initialDelayMs * multiplier
                    val elapsedMs = System.currentTimeMillis() - item.receivedAt
                    
                    if (elapsedMs < backoffDelayMs) {
                        Log.d(TAG, "Skipping item ${item.id} (${item.packageName}) - Backoff delay not met yet. Needs ${backoffDelayMs / 1000}s, elapsed ${elapsedMs / 1000}s")
                        continue
                    }
                }

                // Update status to Sending
                val sendingItem = item.copy(status = "Sending")
                queueRepo.updateQueueItem(sendingItem)

                Log.d(TAG, "Sending item ${item.id} to Webhook...")
                val result = sender.sendNotification(sendingItem)

                if (result.isSuccess) {
                    val successItem = sendingItem.copy(
                        status = "Success",
                        lastError = null
                    )
                    queueRepo.updateQueueItem(successItem)
                    Log.d(TAG, "Successfully sent item ${item.id}")

                    if (settings.enableLogging) {
                        logRepo.insertLog(
                            LogItem(
                                status = "Success",
                                url = settings.webhookUrl,
                                responseCode = result.responseCode,
                                responseBody = result.responseBody,
                                error = null,
                                retryCount = item.retryCount,
                                duration = result.duration,
                                appName = item.appName,
                                packageName = item.packageName
                            )
                        )
                    }
                } else {
                    val currentRetryCount = item.retryCount + 1
                    val maxRetries = settings.retryMax

                    if (currentRetryCount > maxRetries) {
                        val failedItem = sendingItem.copy(
                            status = "Failed",
                            retryCount = item.retryCount,
                            lastError = result.error ?: "Max retries exceeded"
                        )
                        queueRepo.updateQueueItem(failedItem)
                        Log.w(TAG, "Failed to send item ${item.id} permanently")

                        if (settings.enableLogging) {
                            logRepo.insertLog(
                                LogItem(
                                    status = "Failed",
                                    url = settings.webhookUrl,
                                    responseCode = result.responseCode,
                                    responseBody = result.responseBody,
                                    error = result.error ?: "Max retries exceeded",
                                    retryCount = item.retryCount,
                                    duration = result.duration,
                                    appName = item.appName,
                                    packageName = item.packageName
                                )
                            )
                        }
                    } else {
                        val retryItem = sendingItem.copy(
                            status = "Retry",
                            retryCount = currentRetryCount,
                            lastError = result.error ?: "Temporary transmission failure"
                        )
                        queueRepo.updateQueueItem(retryItem)
                        Log.i(TAG, "Item ${item.id} temporary failure, scheduling retry")

                        if (settings.enableLogging) {
                            logRepo.insertLog(
                                LogItem(
                                    status = "Retry",
                                    url = settings.webhookUrl,
                                    responseCode = result.responseCode,
                                    responseBody = result.responseBody,
                                    error = result.error ?: "Retry scheduled",
                                    retryCount = currentRetryCount,
                                    duration = result.duration,
                                    appName = item.appName,
                                    packageName = item.packageName
                                )
                            )
                        }
                    }
                }
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker execution failed: ${e.message}", e)
            return Result.retry()
        }
    }
}
