package com.example.projecthub.uiscreens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onGoToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val isLoading = authViewModel.isLoading
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isFormValid = isEmailValid && password.length >= 6
    val language = currentAppSettings().language
    val goToRegister = rememberSoundClick(onGoToRegister)
    val loginClick = rememberSoundClick {
        if (email.isNotBlank() && !isEmailValid) {
            message = language.t("login.invalidEmail")
            return@rememberSoundClick
        }

        if (!isFormValid) {
            message = language.t("login.invalidForm")
            return@rememberSoundClick
        }

        authViewModel.login(email, password) { success, resultMessage ->
            message = resultMessage

            if (success) {
                onLoginSuccess()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AuthHeader(subtitle = language.t("login.subtitle"))

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(language.t("login.email")) },
            singleLine = true,
            isError = message.isNotEmpty() && !isEmailValid,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            colors = authTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(language.t("login.password")) },
            singleLine = true,
            isError = message.isNotEmpty() && password.length in 1..5,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            colors = authTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = loginClick,
            enabled = !isLoading,
            colors = authButtonColors(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(language.t("login.submit"))
            }
        }

        TextButton(
            onClick = goToRegister,
            enabled = !isLoading,
            colors = ButtonDefaults.textButtonColors(contentColor = AuthAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(language.t("login.createAccount"))
        }

        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (message.contains("sucesso", ignoreCase = true)) {
                    AuthAccentSoft
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}
