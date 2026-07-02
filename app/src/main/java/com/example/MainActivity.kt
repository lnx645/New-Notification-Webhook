package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppListScreen
import com.example.ui.DashboardScreen
import com.example.ui.DashboardViewModel
import com.example.ui.LogsScreen
import com.example.ui.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: DashboardViewModel = viewModel()
                var currentTab by remember { mutableIntStateOf(0) }

                val tabTitles = listOf("Dashboard", "Log Webhook", "Daftar Aplikasi", "Pengaturan")

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = tabTitles[currentTab],
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 0) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                        contentDescription = "Dashboard"
                                    )
                                },
                                label = { Text("Dashboard") },
                                modifier = Modifier.testTag("nav_dashboard")
                            )

                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 1) Icons.AutoMirrored.Filled.List else Icons.AutoMirrored.Outlined.List,
                                        contentDescription = "Log"
                                    )
                                },
                                label = { Text("Log") },
                                modifier = Modifier.testTag("nav_logs")
                            )

                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 2) Icons.Filled.Apps else Icons.Outlined.Apps,
                                        contentDescription = "Aplikasi"
                                    )
                                },
                                label = { Text("Aplikasi") },
                                modifier = Modifier.testTag("nav_apps")
                            )

                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 3) Icons.Filled.Settings else Icons.Outlined.Settings,
                                        contentDescription = "Pengaturan"
                                    )
                                },
                                label = { Text("Pengaturan") },
                                modifier = Modifier.testTag("nav_settings")
                            )
                        }
                    }
                ) { innerPadding ->
                    when (currentTab) {
                        0 -> DashboardScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding),
                            onNavigateToLogs = { currentTab = 1 }
                        )
                        1 -> LogsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        2 -> AppListScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        3 -> SettingsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
