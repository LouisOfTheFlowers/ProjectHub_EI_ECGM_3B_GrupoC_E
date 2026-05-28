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
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.settings.OnboardingRepository
import com.example.projecthub.settings.SettingsRepository
import com.example.projecthub.settings.t
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
    private val settingsRepository = SettingsRepository(application)
    private var currentLanguage: AppLanguage = AppLanguage.Portuguese

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

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                currentLanguage = settings.language
            }
        }
    }

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
                    fallback = text("auth.sessionExpired")
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
                message = text("auth.registerSuccess")
                onResult(true, message)
            } else {
                message = authErrorMessage(
                    result.exceptionOrNull(),
                    fallback = text("auth.registerError")
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
                message = text("auth.loginSuccess")
                onResult(true, message)
            } else {
                message = authErrorMessage(
                    result.exceptionOrNull(),
                    fallback = text("auth.loginError")
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
                message = text("auth.resetEmailSent")
                onResult(true, message)
            } else {
                message = authErrorMessage(
                    result.exceptionOrNull(),
                    fallback = text("auth.resetEmailError")
                )
                onResult(false, message)
            }
        }
    }

    fun handlePasswordRecoveryDeepLink(intent: Intent, onReady: () -> Unit) {
        isRecoverySessionReady = false
        message = ""

        viewModelScope.launch {
            val result = repository.importPasswordRecoveryDeepLink(intent)

            if (result.isSuccess) {
                jwt = repository.currentJwt()
                isRecoverySessionReady = true
                message = ""
                onReady()
            } else {
                jwt = null
                isRecoverySessionReady = false
                message = authErrorMessage(
                    result.exceptionOrNull(),
                    fallback = text("auth.recoveryInvalid")
                )
            }
        }
    }

    fun updatePasswordAfterRecovery(
        newPassword: String,
        confirmPassword: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            if (newPassword.length < 8 || !newPassword.any(Char::isLetter) || !newPassword.any(Char::isDigit)) {
                val error = text("auth.weakPassword")
                message = error
                onResult(false, error)
                return@launch
            }

            if (newPassword != confirmPassword) {
                val error = text("auth.passwordMismatch")
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
                message = text("auth.passwordUpdated")
                onResult(true, message)
            } else {
                message = authErrorMessage(
                    result.exceptionOrNull(),
                    fallback = text("auth.passwordUpdateError")
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
            return text("auth.weakPassword")
        }

        if (error is AuthRestException) {
            if (error.errorDescription.contains("sending confirmation email", ignoreCase = true)) {
                return text("auth.smtpConfirmationError")
            }

            if (
                error.error.contains("email_not_confirmed", ignoreCase = true) ||
                error.errorDescription.contains("email not confirmed", ignoreCase = true) ||
                error.message.orEmpty().contains("email_not_confirmed", ignoreCase = true)
            ) {
                return text("auth.emailNotConfirmed")
            }

            return when (error.errorCode) {
                AuthErrorCode.EmailAddressInvalid ->
                    text("auth.emailInvalidSupabase")

                AuthErrorCode.EmailAddressNotAuthorized ->
                    text("auth.emailNotAuthorized")

                AuthErrorCode.EmailProviderDisabled,
                AuthErrorCode.ProviderDisabled ->
                    text("auth.emailProviderDisabled")

                AuthErrorCode.SignupDisabled ->
                    text("auth.signupDisabled")

                AuthErrorCode.WeakPassword ->
                    text("auth.weakPassword")

                AuthErrorCode.EmailExists,
                AuthErrorCode.UserAlreadyExists ->
                    text("auth.emailExists")

                AuthErrorCode.InvalidCredentials ->
                    text("auth.invalidCredentials")

                AuthErrorCode.OverEmailSendRateLimit,
                AuthErrorCode.OverRequestRateLimit ->
                    text("auth.rateLimit")

                AuthErrorCode.UnexpectedFailure ->
                    text("auth.supabaseInternal").format(error.errorDescription)

                else -> text("auth.supabaseAuthError").format(error.error)
            }
        }

        if (error is RestException) {
            return text("auth.supabaseRestError").format(error.error)
        }

        val rawMessage = error?.message.orEmpty()

        return when {
            rawMessage.contains("email_address_invalid", ignoreCase = true) ||
            rawMessage.contains("invalid format", ignoreCase = true) ||
                rawMessage.contains("validate email", ignoreCase = true) ->
                text("auth.emailInvalidSupabase")

            rawMessage.contains("email_address_not_authorized", ignoreCase = true) ->
                text("auth.emailNotAuthorized")

            rawMessage.contains("EMAIL_NOT_REGISTERED", ignoreCase = true) ->
                text("auth.emailNotRegistered")

            rawMessage.contains("email_not_confirmed", ignoreCase = true) ||
                rawMessage.contains("email not confirmed", ignoreCase = true) ->
                text("auth.emailNotConfirmed")

            rawMessage.contains("weak_password", ignoreCase = true) ->
                text("auth.weakPassword")

            rawMessage.contains("email_provider_disabled", ignoreCase = true) ||
                rawMessage.contains("provider_disabled", ignoreCase = true) ->
                text("auth.emailProviderDisabled")

            rawMessage.contains("already registered", ignoreCase = true) ||
                rawMessage.contains("already exists", ignoreCase = true) ||
                rawMessage.contains("Já existe", ignoreCase = true) ->
                text("auth.emailExists")

            rawMessage.contains("invalid login", ignoreCase = true) ||
                rawMessage.contains("invalid credentials", ignoreCase = true) ->
                text("auth.invalidCredentials")

            rawMessage.contains("network", ignoreCase = true) ||
                rawMessage.contains("timeout", ignoreCase = true) ->
                text("auth.networkError")

            rawMessage.isBlank() -> fallback

            else -> rawMessage
                .lineSequence()
                .firstOrNull()
                ?.take(120)
                ?: fallback
        }
    }

    private fun text(key: String): String = currentLanguage.t(key)
}
