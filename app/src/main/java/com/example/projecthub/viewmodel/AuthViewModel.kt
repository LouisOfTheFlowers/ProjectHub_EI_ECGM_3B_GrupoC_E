package com.example.projecthub.viewmodel

import android.app.Application
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.local.database.DatabaseProvider
import com.example.projecthub.remote.supabase.models.UserDto
import com.example.projecthub.repository.UserRepository
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DatabaseProvider.getDatabase(application)
    private val repository = UserRepository(
        userDao = database.userDao(),
        syncQueueDao = database.syncQueueDao(),
    )

    var currentUser by mutableStateOf<UserDto?>(null)
        private set

    var isLoading by mutableStateOf(value = false)
        private set

    var isLoggedIn by mutableStateOf(false)
        private set

    var isRestoringSession by mutableStateOf(true)
        private set

    var isRecoverySessionReady by mutableStateOf(false)
        private set

    var message by mutableStateOf("")
        private set

    fun restoreSession(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            isRestoringSession = true
            val result = repository.restoreSession()
            if (result.isSuccess) {
                val authUser = result.getOrNull()
                currentUser = authUser?.user
                isLoggedIn = authUser != null
                onResult(authUser != null)
            } else {
                onResult(false)
            }
            isRestoringSession = false
        }
    }

    fun register(
        fullName: String,
        username: String,
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        viewModelScope.launch {
            isLoading = true
            val result = repository.register(fullName, username, email, password)
            isLoading = false
            if (result.isSuccess) {
                onResult(true, "Registo concluído com sucesso! Verifica o teu e-mail para confirmares a conta.")
            } else {
                val errorMsg = authErrorMessage(result.exceptionOrNull(), "Erro ao registar.")
                onResult(false, errorMsg)
            }
        }
    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        viewModelScope.launch {
            isLoading = true
            val result = repository.login(email, password)
            isLoading = false
            if (result.isSuccess) {
                val authUser = result.getOrNull()
                currentUser = authUser?.user
                isLoggedIn = true
                onResult(true, "Login com sucesso!")
            } else {
                val errorMsg = authErrorMessage(result.exceptionOrNull(), "Erro ao iniciar sessão.")
                onResult(false, errorMsg)
            }
        }
    }

    fun logout(onResult: () -> Unit = {}) {
        viewModelScope.launch {
            repository.logout()
            currentUser = null
            isLoggedIn = false
            onResult()
        }
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            val result = repository.sendPasswordResetEmail(email)
            isLoading = false
            if (result.isSuccess) {
                onResult(true, "Email de recuperação enviado.")
            } else {
                onResult(false, "Erro ao enviar email de recuperação.")
            }
        }
    }

    fun handlePasswordRecoveryDeepLink(intent: Intent, onReady: () -> Unit) {
        isRecoverySessionReady = false
        repository.handlePasswordRecoveryDeepLink(intent) {
            isRecoverySessionReady = true
            onReady()
        }
    }

    fun updatePasswordAfterRecovery(
        newPassword: String,
        confirmPassword: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        viewModelScope.launch {
            if (newPassword != confirmPassword) {
                onResult(false, "As palavras-passe não coincidem.")
                return@launch
            }
            if (newPassword.length < 8) {
                onResult(false, "A palavra-passe deve ter pelo menos 8 caracteres.")
                return@launch
            }

            isLoading = true
            val result = repository.updatePasswordAfterRecovery(newPassword)
            isLoading = false
            if (result.isSuccess) {
                isRecoverySessionReady = false
                onResult(true, "Senha alterada com sucesso.")
            } else {
                onResult(false, "Erro ao alterar senha.")
            }
        }
    }

    fun shouldShowIntroForCurrentUser(onResult: (Boolean) -> Unit) {
        onResult(false)
    }

    fun markIntroSeenForCurrentUser(onResult: () -> Unit) {
        onResult()
    }

    fun updateCurrentUser(user: UserDto?) {
        currentUser = user
    }

    private fun authErrorMessage(error: Throwable?, fallback: String): String {
        if (error is AuthWeakPasswordException) return "Senha demasiado fraca."
        if (error is AuthRestException) {
            return when (error.errorCode) {
                AuthErrorCode.EmailExists, AuthErrorCode.UserAlreadyExists -> "Este email já está registado."
                AuthErrorCode.InvalidCredentials -> "Email ou senha incorretos."
                else -> error.errorDescription
            }
        }
        if (error is RestException) return "Erro no servidor: ${error.error}"
        return error?.message ?: fallback
    }
}
