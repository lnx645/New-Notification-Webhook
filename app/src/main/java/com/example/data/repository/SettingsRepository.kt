package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("notif_bridge_settings", Context.MODE_PRIVATE)

    companion object {
        const val KEY_WEBHOOK_URL = "webhook_url"
        const val KEY_AUTH_HEADER = "auth_header"
        const val KEY_CUSTOM_HEADER_NAME = "custom_header_name"
        const val KEY_CUSTOM_HEADER_VALUE = "custom_header_value"
        const val KEY_TIMEOUT_REQUEST = "timeout_request"
        const val KEY_RETRY_MAX = "retry_max"
        const val KEY_RETRY_DELAY = "retry_delay"
        const val KEY_WORKER_INTERVAL = "worker_interval"
        const val KEY_ENABLE_LOGGING = "enable_logging"
        const val KEY_AUTO_START = "auto_start"
        const val KEY_RETRY_FAILED_QUEUE = "retry_failed_queue"
        const val KEY_IGNORE_DUPLICATE = "ignore_duplicate"
        const val KEY_SERVICE_RUNNING = "service_running"
    }

    var webhookUrl: String
        get() = prefs.getString(KEY_WEBHOOK_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEBHOOK_URL, value).apply()

    var authHeader: String
        get() = prefs.getString(KEY_AUTH_HEADER, "") ?: ""
        set(value) = prefs.edit().putString(KEY_AUTH_HEADER, value).apply()

    var customHeaderName: String
        get() = prefs.getString(KEY_CUSTOM_HEADER_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_HEADER_NAME, value).apply()

    var customHeaderValue: String
        get() = prefs.getString(KEY_CUSTOM_HEADER_VALUE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_HEADER_VALUE, value).apply()

    var timeoutRequest: Int
        get() = prefs.getInt(KEY_TIMEOUT_REQUEST, 15)
        set(value) = prefs.edit().putInt(KEY_TIMEOUT_REQUEST, value).apply()

    var retryMax: Int
        get() = prefs.getInt(KEY_RETRY_MAX, 5)
        set(value) = prefs.edit().putInt(KEY_RETRY_MAX, value).apply()

    var retryDelay: Int
        get() = prefs.getInt(KEY_RETRY_DELAY, 5)
        set(value) = prefs.edit().putInt(KEY_RETRY_DELAY, value).apply()

    var workerInterval: Int
        get() = prefs.getInt(KEY_WORKER_INTERVAL, 15)
        set(value) = prefs.edit().putInt(KEY_WORKER_INTERVAL, value).apply()

    var enableLogging: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_LOGGING, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_LOGGING, value).apply()

    var autoStart: Boolean
        get() = prefs.getBoolean(KEY_AUTO_START, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_START, value).apply()

    var retryFailedQueue: Boolean
        get() = prefs.getBoolean(KEY_RETRY_FAILED_QUEUE, true)
        set(value) = prefs.edit().putBoolean(KEY_RETRY_FAILED_QUEUE, value).apply()

    var ignoreDuplicateNotification: Boolean
        get() = prefs.getBoolean(KEY_IGNORE_DUPLICATE, true)
        set(value) = prefs.edit().putBoolean(KEY_IGNORE_DUPLICATE, value).apply()

    var isServiceRunning: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_RUNNING, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_RUNNING, value).apply()

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
