package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings = viewModel.settingsRepo

    var webhookUrl by remember { mutableStateOf(settings.webhookUrl) }
    var authHeader by remember { mutableStateOf(settings.authHeader) }
    var customHeaderName by remember { mutableStateOf(settings.customHeaderName) }
    var customHeaderValue by remember { mutableStateOf(settings.customHeaderValue) }
    var timeoutRequest by remember { mutableStateOf(settings.timeoutRequest.toString()) }
    var retryMax by remember { mutableStateOf(settings.retryMax.toString()) }
    var retryDelay by remember { mutableStateOf(settings.retryDelay.toString()) }
    var workerInterval by remember { mutableStateOf(settings.workerInterval.toString()) }

    var enableLogging by remember { mutableStateOf(settings.enableLogging) }
    var autoStart by remember { mutableStateOf(settings.autoStart) }
    var retryFailedQueue by remember { mutableStateOf(settings.retryFailedQueue) }
    var ignoreDuplicate by remember { mutableStateOf(settings.ignoreDuplicateNotification) }

    fun saveSettings() {
        val timeout = timeoutRequest.toIntOrNull() ?: 15
        val maxRetry = retryMax.toIntOrNull() ?: 5
        val delay = retryDelay.toIntOrNull() ?: 5
        val interval = workerInterval.toIntOrNull() ?: 15

        settings.webhookUrl = webhookUrl
        settings.authHeader = authHeader
        settings.customHeaderName = customHeaderName
        settings.customHeaderValue = customHeaderValue
        settings.timeoutRequest = timeout
        settings.retryMax = maxRetry
        settings.retryDelay = delay
        settings.workerInterval = interval
        settings.enableLogging = enableLogging
        settings.autoStart = autoStart
        settings.retryFailedQueue = retryFailedQueue
        settings.ignoreDuplicateNotification = ignoreDuplicate

        if (autoStart) {
            viewModel.startPeriodicWorker()
        } else {
            viewModel.stopPeriodicWorker()
        }

        Toast.makeText(context, "Pengaturan berhasil disimpan!", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Pengaturan Webhook",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = webhookUrl,
                        onValueChange = { webhookUrl = it },
                        label = { Text("URL Webhook (HTTP / HTTPS)") },
                        modifier = Modifier.fillMaxWidth().testTag("webhook_url_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = authHeader,
                        onValueChange = { authHeader = it },
                        label = { Text("Authorization Header (Opsional)") },
                        placeholder = { Text("Bearer eyJhbGciOi...") },
                        modifier = Modifier.fillMaxWidth().testTag("auth_header_field"),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customHeaderName,
                            onValueChange = { customHeaderName = it },
                            label = { Text("Header Kustom") },
                            placeholder = { Text("X-API-Key") },
                            modifier = Modifier.weight(1f).testTag("custom_header_name_field"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = customHeaderValue,
                            onValueChange = { customHeaderValue = it },
                            label = { Text("Nilai Header") },
                            placeholder = { Text("rahasia123") },
                            modifier = Modifier.weight(1f).testTag("custom_header_value_field"),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = timeoutRequest,
                        onValueChange = { timeoutRequest = it },
                        label = { Text("Timeout Request (Detik)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("timeout_field"),
                        singleLine = true
                    )
                }
            }
        }

        item {
            Text(
                text = "Pengaturan Retry & Worker",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = retryMax,
                        onValueChange = { retryMax = it },
                        label = { Text("Retry Maksimal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("retry_max_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = retryDelay,
                        onValueChange = { retryDelay = it },
                        label = { Text("Retry Delay (Detik)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("retry_delay_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = workerInterval,
                        onValueChange = { workerInterval = it },
                        label = { Text("Interval Worker (Menit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("worker_interval_field"),
                        singleLine = true
                    )
                }
            }
        }

        item {
            Text(
                text = "Pengaturan Fitur & General",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ToggleSettingItem(
                        title = "Aktifkan Logging",
                        description = "Menyimpan riwayat respons & status pengiriman.",
                        checked = enableLogging,
                        onCheckedChange = { enableLogging = it },
                        modifier = Modifier.testTag("switch_enable_logging")
                    )

                    ToggleSettingItem(
                        title = "Auto Start",
                        description = "Mulai jalankan worker di background secara berkala.",
                        checked = autoStart,
                        onCheckedChange = { autoStart = it },
                        modifier = Modifier.testTag("switch_auto_start")
                    )

                    ToggleSettingItem(
                        title = "Coba Lagi Queue Gagal",
                        description = "Secara otomatis mencoba mengirim ulang queue yang gagal.",
                        checked = retryFailedQueue,
                        onCheckedChange = { retryFailedQueue = it },
                        modifier = Modifier.testTag("switch_retry_failed")
                    )

                    ToggleSettingItem(
                        title = "Abaikan Notifikasi Duplikat",
                        description = "Jangan kirim notifikasi yang sama dalam selang 5 detik.",
                        checked = ignoreDuplicate,
                        onCheckedChange = { ignoreDuplicate = it },
                        modifier = Modifier.testTag("switch_ignore_duplicate")
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { saveSettings() },
                    modifier = Modifier.fillMaxWidth().testTag("save_settings_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Pengaturan")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.clearQueue()
                            Toast.makeText(context, "Antrean berhasil dibersihkan!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).testTag("clear_queue_button")
                    ) {
                        Text("Bersihkan Queue")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.clearLogs()
                            Toast.makeText(context, "Log berhasil dibersihkan!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).testTag("clear_logs_button")
                    ) {
                        Text("Bersihkan Log")
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleSettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier
        )
    }
}
