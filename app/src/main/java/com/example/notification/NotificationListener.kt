package com.example.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.NotifBridgeApp
import com.example.data.model.QueueItem
import com.example.worker.NotificationForwardWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationListener : NotificationListenerService() {

    private val tag = "NotificationListener"
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var app: NotifBridgeApp

    override fun onCreate() {
        super.onCreate()
        app = application as NotifBridgeApp
        Log.d(tag, "Notification Listener Created")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        Log.d(tag, "Notification Listener Destroyed")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(tag, "Notification Listener Connected")
        app.settingsRepository.isServiceRunning = true
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(tag, "Notification Listener Disconnected")
        app.settingsRepository.isServiceRunning = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        if (packageName == applicationContext.packageName) {
            // Avoid intercepting our own notifications
            return
        }

        scope.launch {
            try {
                val settings = app.settingsRepository
                
                // Check if this package is enabled for monitoring
                val isEnabled = app.appConfigRepository.isAppEnabled(packageName)
                if (!isEnabled) {
                    return@launch
                }

                val notification = sbn.notification ?: return@launch
                val extras = notification.extras ?: return@launch

                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
                val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
                val ticker = notification.tickerText?.toString() ?: ""
                val category = notification.category ?: ""
                val tagStr = sbn.tag ?: ""
                val groupKey = sbn.groupKey ?: ""
                val channelId = notification.channelId ?: ""
                val postTime = sbn.postTime

                // Skip if both title and text are empty
                if (title.isBlank() && text.isBlank()) {
                    return@launch
                }

                // Check for duplicates if enabled
                if (settings.ignoreDuplicateNotification) {
                    val duplicate = app.queueRepository.findDuplicate(packageName, title, text, postTime)
                    if (duplicate != null) {
                        Log.d(tag, "Duplicate ignored: $packageName - $title")
                        return@launch
                    }
                }

                // Get app name from package manager
                val pm = packageManager
                val appName = try {
                    val ai = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(ai).toString()
                } catch (e: Exception) {
                    packageName
                }

                // Insert into Queue Database with Pending status
                val queueItem = QueueItem(
                    packageName = packageName,
                    appName = appName,
                    title = title,
                    text = text,
                    bigText = bigText,
                    subText = subText,
                    ticker = ticker,
                    category = category,
                    notificationId = sbn.id,
                    tag = tagStr,
                    groupKey = groupKey,
                    channelId = channelId,
                    postTime = postTime,
                    status = "Pending"
                )

                app.queueRepository.insertQueueItem(queueItem)
                Log.d(tag, "Queued notification: $packageName")

                // Trigger WorkManager immediately
                NotificationForwardWorker.enqueueOneTimeWork(applicationContext)

            } catch (e: Exception) {
                Log.e(tag, "Error processing notification: ${e.message}", e)
            }
        }
    }
}
