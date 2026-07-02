package com.example

import android.app.Application
import androidx.work.Configuration
import com.example.data.database.AppDatabase
import com.example.data.network.WebhookSender
import com.example.data.repository.AppConfigRepository
import com.example.data.repository.LogRepository
import com.example.data.repository.QueueRepository
import com.example.data.repository.SettingsRepository

class NotifBridgeApp : Application(), Configuration.Provider {

    lateinit var database: AppDatabase
    lateinit var settingsRepository: SettingsRepository
    lateinit var queueRepository: QueueRepository
    lateinit var appConfigRepository: AppConfigRepository
    lateinit var logRepository: LogRepository
    lateinit var webhookSender: WebhookSender

    override fun onCreate() {
        super.onCreate()
        
        database = AppDatabase.getDatabase(this)
        settingsRepository = SettingsRepository(this)
        queueRepository = QueueRepository(database.queueDao())
        appConfigRepository = AppConfigRepository(this, database.appConfigDao())
        logRepository = LogRepository(database.logDao())
        webhookSender = WebhookSender(settingsRepository)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
