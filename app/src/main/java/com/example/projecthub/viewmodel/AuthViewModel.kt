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
import com.example.projecthub.settings.OnboardingRepository
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DatabaseProvider.getDatabase(application)

    private val repository = UserRepository(
        userDao = database.userDao(),
        syncQueueDao = database.syncQueueDao()
    )
    private val onboardingRepository = OnboardingRepository(application)

    var message by mutableStateOf("")
        private set

    var isLoggedIn by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isRestoringSession by mutableStateOf(true)
        private set

    var isRecoverySessionReady by mutableStateOf(false)
        private set

    var jwt by mutableStateOf<String?>(null)
        private set

    var currentUser by mutableStateOf<UserDto?>(null)
        private set

    fun restoreSession(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            isRestoringSession = true
            message = ""

            val result = repository.restoreSession()

            if (result.isSuccess) {
                val authenticatedUser = result.getOrNull()
                currentUser = authenticatedUser?.user
                jwt = authenticatedUser?.jwt
                isLoggedIn = authenticatedUser != null
                isRestoringSession = false
                onResult(authenticatedUser != null)
            } else {
                currentUser = null
                jwt = null
                isLoggedIn = false
                message = authErrorMessage(
                    result.exceptionOrNull(),
                    fallback = "Sessao expirada. Inicia sessao novamente."
                )
                isRestoringSession = false
                onResult(false)
            }
        }
    }

    fun register(
        nome: String,
        username: String,
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            message = ""
            val result = repository.register(
                nome = nome.trim(),
                username = username.trim(),
                email = email.trim(),
                password = password
            )
            isLoading = false

            if (result.isSuccess) {
                message = "Conta criada com sucesso. Já podes iniciar sessão."
                onResult(true, message)
            } else {
                message = authErrorMessage(
                    result.exceptionOrNull(),
                    fallback = "Erro ao criar conta."
                )
                onResult(false, message)
            }
        }
    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            message = ""
            val result = repository.login(email.trim(), password)
            isLoading = false

            if (result.isSuccess) {
                currentUser = result.getOrNull()?.user
                isLoggedIn = true
                jwt = result.getOrNull()?.jwt
                message = "Login efetuado com sucesso."
                onResult(true, message)
            } else {
                message = authErrorMessage(
                    result.exceptionOrNull(),
                    fallback = "Erro ao iniciar sessão."
                )
                onResult(false, message)
            }
        }
    }

    fun logout(onResult: () -> Unit = {}) {
        viewModelScope.launch {
            repository.logout()
            jwt = null
            currentUser = null
            isLoggedIn = false
            message = ""
            onResult()
        }
    }

    fun sendPasswordResetEmail(
        email: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            message = ""
            val result = repository.sendPasswordResetEmail(email.trim())
            isLoading = false

            if (result.isSuccess) {
                message = "Email de recuperaÃ§Ã£o enviado. Verifica a tua caixa de entrada."
                onResult(true, message)
            } else {
                message = authErrorMessage(
                    result.exceptionOrNull(),
                    fallback = "NÃ£o foi possÃ­vel enviar o email de recuperaÃ§Ã£o."
                )
                onResult(false, message)
            }
        }
    }

    fun handlePasswordRecoveryDeepLink(intent: Intent, onReady: () -> Unit) {
        isRecoverySessionReady = false
        message = ""

        repository.handlePasswordRecoveryDeepLink(intent) {
            jwt = repository.currentJwt()
            isRecoverySessionReady = true
            onReady()
        }
    }

    fun updatePasswordAfterRecovery(
        newPassword: String,
        confirmPassword: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            if (newPassword.length < 8 || !newPassword.any(Char::isLetter) || !newPassword.any(Char::isDigit)) {
                val error = "A palavra-passe deve ter pelo menos 8 caracteres, letras e numeros."
                message = error
                onResult(false, error)
                return@launch
            }

            if (newPassword != confirmPassword) {
                val error = "As palavras-passe nao coincidem."
                message = error
                onResult(false, error)
                return@launch
            }

            isLoading = true
            message = ""

            val result = repository.updatePasswordAfterRecovery(newPassword)
            isLoading = false

            if (result.isSuccess) {
                currentUser = null
                jwt = null
                isLoggedIn = false
                isRecoverySessionReady = false
                message = "Palavra-passe alterada com sucesso. Ja podes iniciar sessao."
                onResult(true, message)
            } else {
                message = authErrorMessage(
                    result.exceptionOrNull(),
                    fallback = "Nao foi possivel alterar a palavra-passe."
                )
                onResult(false, message)
            }
        }
    }

    fun updateCurrentUser(user: UserDto) {
        currentUser = user
    }

    fun shouldShowIntroForCurrentUser(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = currentUser
            if (user == null) {
                onResult(false)
                return@launch
            }

            val userKey = user.id?.toString() ?: user.email
            val hasSeenIntro = onboardingRepository.hasSeenIntro(userKey, user.role)
            onResult(!hasSeenIntro)
        }
    }

    fun markIntroSeenForCurrentUser(onResult: () -> Unit = {}) {
        viewModelScope.launch {
            val user = currentUser
            if (user != null) {
                val userKey = user.id?.toString() ?: user.email
                onboardingRepository.markIntroSeen(userKey, user.role)
            }
            onResult()
        }
    }

    private fun authErrorMessage(error: Throwable?, fallback: String): String {
        if (error is AuthWeakPasswordException) {
            return "A palavra-passe é fraca. Usa pelo menos 8 caracteres com letras e números."
        }

        if (error is AuthRestException) {
            if (error.errorDescription.contains("sending confirmation email", ignoreCase = true)) {
                return "O Supabase não conseguiu enviar o email de confirmação. Configura o SMTP em Authentication > SMTP Settings."
            }

            return when (error.errorCode) {
                AuthErrorCode.EmailAddressInvalid ->
                    "Este email foi considerado inválido pelo Supabase. Usa um email real, sem domínio de teste."

                AuthErrorCode.EmailAddressNotAuthorized ->
                    "O Supabase não está autorizado a enviar emails para esse endereço. Configura SMTP próprio ou usa um email da organização Supabase."

                AuthErrorCode.EmailProviderDisabled,
                AuthErrorCode.ProviderDisabled ->
                    "O login por email está desativado no Supabase. Ativa o provider Email em Authentication."

                AuthErrorCode.SignupDisabled ->
                    "O registo está desativado no Supabase."

                AuthErrorCode.WeakPassword ->
                    "A palavra-passe é fraca. Usa pelo menos 8 caracteres com letras e números."

                AuthErrorCode.EmailExists,
                AuthErrorCode.UserAlreadyExists ->
                    "Já existe uma conta com este email."

                AuthErrorCode.InvalidCredentials ->
                    "Email ou palavra-passe incorretos."

                AuthErrorCode.OverEmailSendRateLimit,
                AuthErrorCode.OverRequestRateLimit ->
                    "Foram feitas demasiadas tentativas. Espera um pouco e tenta novamente."

                AuthErrorCode.UnexpectedFailure ->
                    "Erro interno no Supabase Auth: ${error.errorDescription}."

                else -> "Erro no Supabase Auth: ${error.error}."
            }
        }

        if (error is RestException) {
            return "Erro no Supabase: ${error.error}."
        }

        val rawMessage = error?.message.orEmpty()

        return when {
            rawMessage.contains("email_address_invalid", ignoreCase = true) ||
            rawMessage.contains("invalid format", ignoreCase = true) ||
                rawMessage.contains("validate email", ignoreCase = true) ->
                "Este email foi considerado inválido pelo Supabase. Usa um email real, sem domínio de teste."

            rawMessage.contains("email_address_not_authorized", ignoreCase = true) ->
                "O Supabase não está autorizado a enviar emails para esse endereço. Configura SMTP próprio ou usa um email da organização Supabase."

            rawMessage.contains("weak_password", ignoreCase = true) ->
                "A palavra-passe é fraca. Usa pelo menos 8 caracteres com letras e números."

            rawMessage.contains("email_provider_disabled", ignoreCase = true) ||
                rawMessage.contains("provider_disabled", ignoreCase = true) ->
                "O login por email está desativado no Supabase. Ativa o provider Email em Authentication."

            rawMessage.contains("already registered", ignoreCase = true) ||
                rawMessage.contains("already exists", ignoreCase = true) ||
                rawMessage.contains("Já existe", ignoreCase = true) ->
                "Já existe uma conta com este email."

            rawMessage.contains("invalid login", ignoreCase = true) ||
                rawMessage.contains("invalid credentials", ignoreCase = true) ->
                "Email ou palavra-passe incorretos."

            rawMessage.contains("network", ignoreCase = true) ||
                rawMessage.contains("timeout", ignoreCase = true) ->
                "Não foi possível ligar ao Supabase. Verifica a ligação à internet."

            rawMessage.isBlank() -> fallback

            else -> rawMessage
                .lineSequence()
                .firstOrNull()
                ?.take(120)
                ?: fallback
        }
    }
}
