package com.example.projecthub

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.settings.AppNotificationHelper
import com.example.projecthub.settings.AppSettingsProvider
import com.example.projecthub.settings.AppThemeMode
import com.example.projecthub.ui.theme.ProjectHubTheme
import com.example.projecthub.uiscreens.AdminDashboardScreen
import com.example.projecthub.uiscreens.LoginScreen
import com.example.projecthub.uiscreens.RegisterScreen
import com.example.projecthub.viewmodel.AdminSettingsViewModel
import com.example.projecthub.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: AdminSettingsViewModel = viewModel()
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (settings.themeMode) {
                AppThemeMode.Light -> false
                AppThemeMode.Dark -> true
                AppThemeMode.System -> systemDark
            }

            LaunchedEffect(settings.notificationsEnabled) {
                if (settings.notificationsEnabled) {
                    AppNotificationHelper.createChannels(this@MainActivity)
                    requestNotificationPermissionIfNeeded()
                }
            }

            ProjectHubTheme(
                darkTheme = darkTheme,
                dynamicColor = false
            ) {
                AppSettingsProvider(settings = settings) {
                    val authViewModel: AuthViewModel = viewModel()
                    var currentScreen by remember { mutableStateOf("login") }

                    when (currentScreen) {
                        "login" -> LoginScreen(
                            authViewModel = authViewModel,
                            onGoToRegister = { currentScreen = "register" },
                            onLoginSuccess = { currentScreen = "home" }
                        )

                        "register" -> RegisterScreen(
                            authViewModel = authViewModel,
                            onGoToLogin = { currentScreen = "login" }
                        )

                        "home" -> AdminDashboardScreen(
                            onLogout = {
                                authViewModel.logout {
                                    currentScreen = "login"
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (AppNotificationHelper.canPostNotifications(this)) return

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            42
        )
    }
}
