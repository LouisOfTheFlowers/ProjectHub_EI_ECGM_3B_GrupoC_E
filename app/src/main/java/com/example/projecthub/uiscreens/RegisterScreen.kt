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
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onGoToLogin: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var messageIsSuccess by remember { mutableStateOf(false) }
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val isLoading = authState.isLoading
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isPasswordValid = password.length >= 8 &&
        password.any { it.isLetter() } &&
        password.any { it.isDigit() }
    val isFormValid = nome.isNotBlank() &&
        username.isNotBlank() &&
        isEmailValid &&
        isPasswordValid
    val language = currentAppSettings().language
    val goToLogin = rememberSoundClick(onGoToLogin)
    val registerClick = rememberSoundClick {
        if (email.isNotBlank() && !isEmailValid) {
            message = language.t("register.invalidEmail")
            messageIsSuccess = false
            return@rememberSoundClick
        }

        if (!isFormValid) {
            message = language.t("register.invalidForm")
            messageIsSuccess = false
            return@rememberSoundClick
        }

        authViewModel.register(nome, username, email, password) { success, resultMessage ->
            message = resultMessage
            messageIsSuccess = success

            if (success) {
                onGoToLogin()
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
        AuthHeader(subtitle = language.t("register.subtitle"))

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text(language.t("register.name")) },
            singleLine = true,
            isError = message.isNotEmpty() && nome.isBlank(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = authTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(language.t("register.username")) },
            singleLine = true,
            isError = message.isNotEmpty() && username.isBlank(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = authTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(language.t("register.email")) },
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
            label = { Text(language.t("register.password")) },
            singleLine = true,
            isError = message.isNotEmpty() && password.isNotEmpty() && !isPasswordValid,
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
            onClick = registerClick,
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
                Text(language.t("register.submit"))
            }
        }

        TextButton(
            onClick = goToLogin,
            enabled = !isLoading,
            colors = ButtonDefaults.textButtonColors(contentColor = AuthAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(language.t("register.haveAccount"))
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
}
