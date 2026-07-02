package com.example.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.data.database.AppConfigDao
import com.example.data.model.AppConfig
import kotlinx.coroutines.flow.Flow

class AppConfigRepository(
    private val context: Context,
    private val appConfigDao: AppConfigDao
) {
    val allConfigsFlow: Flow<List<AppConfig>> = appConfigDao.getAllAppConfigsFlow()

    suspend fun getAllAppConfigs(): List<AppConfig> = appConfigDao.getAllAppConfigs()

    suspend fun getAppConfig(packageName: String): AppConfig? = appConfigDao.getAppConfig(packageName)

    suspend fun setAppEnabled(packageName: String, appName: String, enabled: Boolean) {
        if (enabled) {
            appConfigDao.insertAppConfig(AppConfig(packageName, appName, true))
        } else {
            appConfigDao.deleteAppConfigByPackage(packageName)
        }
    }

    suspend fun isAppEnabled(packageName: String): Boolean {
        val config = appConfigDao.getAppConfig(packageName)
        return config?.isEnabled == true
    }

    fun getInstalledApps(): List<AppConfigInfo> {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val list = mutableListOf<AppConfigInfo>()

        for (appInfo in packages) {
            val appName = pm.getApplicationLabel(appInfo).toString()
            if (appInfo.packageName.isNotEmpty()) {
                val displayName = if (appName.isNotEmpty()) appName else appInfo.packageName
                list.add(
                    AppConfigInfo(
                        packageName = appInfo.packageName,
                        appName = displayName
                    )
                )
            }
        }
        // Deduplicate and sort
        return list.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() }
    }
}

data class AppConfigInfo(
    val packageName: String,
    val appName: String
)
