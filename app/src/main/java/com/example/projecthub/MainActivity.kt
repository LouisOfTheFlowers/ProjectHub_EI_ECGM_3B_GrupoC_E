package com.example.projecthub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.example.projecthub.ui.theme.ProjectHubTheme
import com.example.projecthub.uiscreens.LoginScreen
import com.example.projecthub.uiscreens.RegisterScreen
import com.example.projecthub.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProjectHubTheme {
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

                    "home" -> Text("Login feito com sucesso.")
                }
            }
        }
    }
}