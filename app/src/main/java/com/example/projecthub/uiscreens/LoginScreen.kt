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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    var messageIsSuccess by remember { mutableStateOf(false) }
    var showPasswordResetDialog by remember { mutableStateOf(false) }
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val isLoading = authState.isLoading
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isFormValid = isEmailValid && password.length >= 6
    val language = currentAppSettings().language
    val goToRegister = rememberSoundClick(onGoToRegister)
    val forgotPasswordClick = rememberSoundClick { showPasswordResetDialog = true }
    val loginClick = rememberSoundClick {
        if (email.isNotBlank() && !isEmailValid) {
            message = language.t("login.invalidEmail")
            messageIsSuccess = false
            return@rememberSoundClick
        }

        if (!isFormValid) {
            message = language.t("login.invalidForm")
            messageIsSuccess = false
            return@rememberSoundClick
        }

        authViewModel.login(email, password) { success, resultMessage ->
            message = resultMessage
            messageIsSuccess = success

            if (success) {
                onLoginSuccess()
            }
        }
    }

    AuthResponsiveLayout(subtitle = language.t("login.subtitle")) {
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

        TextButton(
            onClick = forgotPasswordClick,
            enabled = !isLoading,
            colors = ButtonDefaults.textButtonColors(contentColor = AuthAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(language.t("login.forgotPassword"))
        }

        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (messageIsSuccess) {
                    AuthAccentSoft
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }

    if (showPasswordResetDialog) {
        PasswordResetDialog(
            initialEmail = email,
            isLoading = isLoading,
            onDismiss = { showPasswordResetDialog = false },
            onSend = { resetEmail, onResult ->
                authViewModel.sendPasswordResetEmail(resetEmail) { success, resultMessage ->
                    onResult(success, resultMessage)
                    if (success) {
                        showPasswordResetDialog = false
                    }
                }
            }
        )
    }
}

@Composable
private fun PasswordResetDialog(
    initialEmail: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSend: (String, (Boolean, String) -> Unit) -> Unit
) {
    val language = currentAppSettings().language
    var resetEmail by remember(initialEmail) { mutableStateOf(initialEmail.trim()) }
    var localMessage by remember { mutableStateOf("") }
    var localMessageIsSuccess by remember { mutableStateOf(false) }
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(resetEmail.trim()).matches()

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        title = {
            Text(language.t("login.resetTitle"))
        },
        text = {
            Column {
                Text(
                    text = language.t("login.resetDescription"),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = resetEmail,
                    onValueChange = {
                        resetEmail = it
                        localMessage = ""
                        localMessageIsSuccess = false
                    },
                    label = { Text(language.t("login.email")) },
                    singleLine = true,
                    isError = localMessage.isNotBlank() && !localMessageIsSuccess,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    colors = authTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (localMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = localMessage,
                        color = if (localMessageIsSuccess) {
                            AuthAccentSoft
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isLoading,
                onClick = rememberSoundClick {
                    if (!isEmailValid) {
                        localMessage = language.t("login.invalidEmail")
                        localMessageIsSuccess = false
                    } else {
                        onSend(resetEmail.trim()) { success, resultMessage ->
                            localMessage = resultMessage
                            localMessageIsSuccess = success
                        }
                    }
                }
            ) {
                Text(language.t("login.resetSend"))
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isLoading,
                onClick = rememberSoundClick(onDismiss)
            ) {
                Text(language.t("common.cancel"))
            }
        }
    )
}
