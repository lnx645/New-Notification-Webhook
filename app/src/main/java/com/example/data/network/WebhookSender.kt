package com.example.data.network

import android.os.Build
import com.example.data.model.QueueItem
import com.example.data.repository.SettingsRepository
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class WebhookSender(private val settingsRepository: SettingsRepository) {

    data class WebhookResult(
        val isSuccess: Boolean,
        val responseCode: Int,
        val responseBody: String?,
        val error: String?,
        val duration: Long
    )

    fun testWebhook(url: String, callback: (WebhookResult) -> Unit) {
        val client = OkHttpClient.Builder()
            .connectTimeout(settingsRepository.timeoutRequest.toLong(), TimeUnit.SECONDS)
            .readTimeout(settingsRepository.timeoutRequest.toLong(), TimeUnit.SECONDS)
            .build()

        val json = JSONObject().apply {
            put("package", "com.example.notifbridge")
            put("app_name", "Notif Bridge")
            put("title", "Test Title")
            put("text", "This is a test notification from Notif Bridge.")
            put("big_text", "This is a detailed test notification body to verify webhook configuration.")
            put("sub_text", "Test Subtext")
            put("ticker", "Test Ticker")
            put("category", "test")
            put("notification_id", 999)
            put("tag", "test_tag")
            put("group_key", "test_group")
            put("channel_id", "test_channel")
            put("post_time", System.currentTimeMillis())
            put("received_at", System.currentTimeMillis())
            put("device", "Android - " + Build.MODEL)
            put("android_version", Build.VERSION.RELEASE)
        }

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val requestBuilder = Request.Builder()
            .url(url)
            .post(requestBody)

        val auth = settingsRepository.authHeader
        if (auth.isNotBlank()) {
            requestBuilder.addHeader("Authorization", auth)
        }

        val customName = settingsRepository.customHeaderName
        val customValue = settingsRepository.customHeaderValue
        if (customName.isNotBlank() && customValue.isNotBlank()) {
            requestBuilder.addHeader(customName, customValue)
        }

        val startTime = System.currentTimeMillis()
        client.newCall(requestBuilder.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val duration = System.currentTimeMillis() - startTime
                callback(
                    WebhookResult(
                        isSuccess = false,
                        responseCode = 0,
                        responseBody = null,
                        error = e.localizedMessage ?: e.message ?: "Connection Failure",
                        duration = duration
                    )
                )
            }

            override fun onResponse(call: Call, response: Response) {
                val duration = System.currentTimeMillis() - startTime
                val code = response.code
                val body = try {
                    response.body?.string()
                } catch (e: Exception) {
                    null
                }
                callback(
                    WebhookResult(
                        isSuccess = response.isSuccessful,
                        responseCode = code,
                        responseBody = body,
                        error = if (response.isSuccessful) null else "HTTP Status $code",
                        duration = duration
                    )
                )
            }
        })
    }

    suspend fun sendNotification(item: QueueItem): WebhookResult {
        val url = settingsRepository.webhookUrl
        if (url.isBlank()) {
            return WebhookResult(false, 0, null, "Webhook URL is empty", 0)
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(settingsRepository.timeoutRequest.toLong(), TimeUnit.SECONDS)
            .readTimeout(settingsRepository.timeoutRequest.toLong(), TimeUnit.SECONDS)
            .build()

        val json = JSONObject().apply {
            put("package", item.packageName)
            put("app_name", item.appName)
            put("title", item.title)
            put("text", item.text)
            put("big_text", item.bigText)
            put("sub_text", item.subText)
            put("ticker", item.ticker)
            put("category", item.category)
            put("notification_id", item.notificationId)
            put("tag", item.tag)
            put("group_key", item.groupKey)
            put("channel_id", item.channelId)
            put("post_time", item.postTime)
            put("received_at", item.receivedAt)
            put("device", "Android - " + Build.MODEL)
            put("android_version", Build.VERSION.RELEASE)
        }

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val requestBuilder = Request.Builder()
            .url(url)
            .post(requestBody)

        val auth = settingsRepository.authHeader
        if (auth.isNotBlank()) {
            requestBuilder.addHeader("Authorization", auth)
        }

        val customName = settingsRepository.customHeaderName
        val customValue = settingsRepository.customHeaderValue
        if (customName.isNotBlank() && customValue.isNotBlank()) {
            requestBuilder.addHeader(customName, customValue)
        }

        val startTime = System.currentTimeMillis()
        return try {
            val response = client.newCall(requestBuilder.build()).execute()
            val duration = System.currentTimeMillis() - startTime
            val code = response.code
            val body = try {
                response.body?.string()
            } catch (e: Exception) {
                null
            }
            WebhookResult(
                isSuccess = response.isSuccessful,
                responseCode = code,
                responseBody = body,
                error = if (response.isSuccessful) null else "Server returned code $code",
                duration = duration
            )
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            WebhookResult(
                isSuccess = false,
                responseCode = 0,
                responseBody = null,
                error = e.localizedMessage ?: e.message ?: "Network Exception",
                duration = duration
            )
        }
    }
}
