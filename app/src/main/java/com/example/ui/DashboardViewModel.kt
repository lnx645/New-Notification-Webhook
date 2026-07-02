package com.example.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.NotifBridgeApp
import com.example.data.model.AppConfig
import com.example.data.model.LogItem
import com.example.data.repository.AppConfigInfo
import com.example.data.repository.SettingsRepository
import com.example.worker.NotificationForwardWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NotifBridgeApp
    private val queueRepo = app.queueRepository
    private val appConfigRepo = app.appConfigRepository
    private val logRepo = app.logRepository
    val settingsRepo = app.settingsRepository
    private val webhookSender = app.webhookSender

    // Queue Statistics
    val pendingCount = queueRepo.getCountByStatusFlow("Pending").asStateFlow(0)
    val sendingCount = queueRepo.getCountByStatusFlow("Sending").asStateFlow(0)
    val successCount = queueRepo.getCountByStatusFlow("Success").asStateFlow(0)
    val retryCount = queueRepo.getCountByStatusFlow("Retry").asStateFlow(0)
    val failedCount = queueRepo.getCountByStatusFlow("Failed").asStateFlow(0)
    val totalQueueCount = queueRepo.totalCount.asStateFlow(0)
    val queueItems = queueRepo.allQueueItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Logs & Last status
    val logs = logRepo.allLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _lastLog = MutableStateFlow<LogItem?>(null)
    val lastLog: StateFlow<LogItem?> = _lastLog.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    // Apps List
    private val _installedApps = MutableStateFlow<List<AppConfigInfo>>(emptyList())
    val installedApps: StateFlow<List<AppConfigInfo>> = _installedApps.asStateFlow()

    val monitoredApps = appConfigRepo.allConfigsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Webhook test status
    private val _testResult = MutableStateFlow<String?>(null)
    val testResult: StateFlow<String?> = _testResult.asStateFlow()

    private val _isTestingWebhook = MutableStateFlow(false)
    val isTestingWebhook: StateFlow<Boolean> = _isTestingWebhook.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadInstalledApps()
        
        // Auto-update last log and error in real-time when logs flow emits new items
        viewModelScope.launch {
            logRepo.allLogs.collect { logsList ->
                _lastLog.value = logsList.firstOrNull()
                
                val failedLog = logsList.find { it.status == "Failed" || it.status == "Retry" }
                _lastError.value = failedLog?.error
            }
        }
    }

    private fun <T> Flow<T>.asStateFlow(initialValue: T): StateFlow<T> {
        return this.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadInstalledApps()
            // We can also trigger a background run of the queue to check if any are pending
            NotificationForwardWorker.enqueueOneTimeWork(app)
            kotlinx.coroutines.delay(800)
            _isRefreshing.value = false
        }
    }

    fun updateLastLogAndError() {
        viewModelScope.launch(Dispatchers.IO) {
            val log = logRepo.getLastLog()
            _lastLog.value = log
            
            // Query a recent error from logs
            val logsList = logRepo.allLogs.firstOrNull()
            val failedLog = logsList?.find { it.status == "Failed" || it.status == "Retry" }
            _lastError.value = failedLog?.error
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = appConfigRepo.getInstalledApps()
            _installedApps.value = apps
        }
    }

    fun toggleAppMonitoring(packageName: String, appName: String, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            appConfigRepo.setAppEnabled(packageName, appName, enabled)
        }
    }

    fun clearLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            logRepo.clearLogs()
            _lastLog.value = null
        }
    }

    fun clearQueue() {
        viewModelScope.launch(Dispatchers.IO) {
            queueRepo.clearAllQueue()
        }
    }

    fun testWebhook(url: String) {
        if (url.isBlank()) {
            _testResult.value = "URL Webhook kosong!"
            return
        }
        _isTestingWebhook.value = true
        _testResult.value = "Mengirim payload uji coba..."
        
        webhookSender.testWebhook(url) { result ->
            _isTestingWebhook.value = false
            _testResult.value = if (result.isSuccess) {
                "Berhasil! HTTP ${result.responseCode} (${result.duration}ms)"
            } else {
                "Gagal: ${result.error} (${result.duration}ms)"
            }
            updateLastLogAndError()
        }
    }

    fun clearTestResult() {
        _testResult.value = null
    }

    fun startPeriodicWorker() {
        NotificationForwardWorker.enqueuePeriodicWork(app, settingsRepo.workerInterval)
    }

    fun stopPeriodicWorker() {
        NotificationForwardWorker.cancelPeriodicWork(app)
    }

    fun forceRunQueue() {
        viewModelScope.launch(Dispatchers.IO) {
            NotificationForwardWorker.enqueueOneTimeWork(app)
        }
    }
}
