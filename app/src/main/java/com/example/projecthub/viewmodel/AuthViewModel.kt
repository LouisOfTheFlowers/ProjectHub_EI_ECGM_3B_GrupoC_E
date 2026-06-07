package com.example.projecthub.viewmodel

import android.app.Application
import android.content.Intent
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthState(
    val message: String = "",
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val isRestoringSession: Boolean = true,
    val isRecoverySessionReady: Boolean = false,
    val jwt: String? = null,
    val currentUser: UserDto? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DatabaseProvider.getDatabase(application)

    private val repository = UserRepository(
        userDao = database.userDao(),
        syncQueueDao = database.syncQueueDao()
    )
    private val onboardingRepository = OnboardingRepository(application)
    private val settingsRepository = SettingsRepository(application)
    private var currentLanguage: AppLanguage = AppLanguage.Portuguese

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    val message: String
        get() = _state.value.message

    val isLoggedIn: Boolean
        get() = _state.value.isLoggedIn

    val isLoading: Boolean
        get() = _state.value.isLoading

    val isRestoringSession: Boolean
        get() = _state.value.isRestoringSession

    val isRecoverySessionReady: Boolean
        get() = _state.value.isRecoverySessionReady

    val jwt: String?
        get() = _state.value.jwt

    val currentUser: UserDto?
        get() = _state.value.currentUser

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                currentLanguage = settings.language
            }
        }
    }

    fun restoreSession(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isRestoringSession = true, message = "") }

            val result = repository.restoreSession()

            if (result.isSuccess) {
                val authenticatedUser = result.getOrNull()
                _state.update {
                    it.copy(
                        currentUser = authenticatedUser?.user,
                        jwt = authenticatedUser?.jwt,
                        isLoggedIn = authenticatedUser != null,
                        isRestoringSession = false
                    )
                }
                onResult(authenticatedUser != null)
            } else {
                _state.update {
                    it.copy(
                        currentUser = null,
                        jwt = null,
                        isLoggedIn = false,
                        message = authErrorMessage(
                            result.exceptionOrNull(),
                            fallback = text("auth.sessionExpired")
                        ),
                        isRestoringSession = false
                    )
                }
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
            _state.update { it.copy(isLoading = true, message = "") }
            val result = repository.register(
                nome = nome.trim(),
                username = username.trim(),
                email = email.trim(),
                password = password
            )

            if (result.isSuccess) {
                val successMessage = text("auth.registerSuccess")
                _state.update { it.copy(isLoading = false, message = successMessage) }
                onResult(true, successMessage)
            } else {
                val errorMessage = authErrorMessage(
                    result.exceptionOrNull(),
                    fallback = text("auth.registerError")
                )
                _state.update { it.copy(isLoading = false, message = errorMessage) }
                onResult(false, errorMessage)
            }
        }
    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = "") }
            val result = repository.login(email.trim(), password)

            if (result.isSuccess) {
                val authenticatedUser = result.getOrNull()
                val successMessage = text("auth.loginSuccess")
                _state.update {
                    it.copy(
                        currentUser = authenticatedUser?.user,
                        isLoggedIn = true,
                        jwt = authenticatedUser?.jwt,
                        isLoading = false,
                        message = successMessage
                    )
                }
                onResult(true, successMessage)
            } else {
                val errorMessage = authErrorMessage(
                    result.exceptionOrNull(),
                    fallback = text("auth.loginError")
                )
                _state.update { it.copy(isLoading = false, message = errorMessage) }
                onResult(false, errorMessage)
            }
        }
    }

    fun logout(onResult: () -> Unit = {}) {
        viewModelScope.launch {
            repository.logout()
            _state.update {
                it.copy(
                    jwt = null,
                    currentUser = null,
                    isLoggedIn = false,
                    message = ""
                )
            }
            onResult()
        }
    }

    fun sendPasswordResetEmail(
        email: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = "") }
            val result = repository.sendPasswordResetEmail(email.trim())

            if (result.isSuccess) {
                val successMessage = text("auth.resetEmailSent")
                _state.update { it.copy(isLoading = false, message = successMessage) }
                onResult(true, successMessage)
            } else {
                val errorMessage = authErrorMessage(
                    result.exceptionOrNull(),
                    fallback = text("auth.resetEmailError")
                )
                _state.update { it.copy(isLoading = false, message = errorMessage) }
                onResult(false, errorMessage)
            }
        }
    }

    fun handlePasswordRecoveryDeepLink(intent: Intent, onReady: () -> Unit) {
        _state.update { it.copy(isRecoverySessionReady = false, message = "") }

        viewModelScope.launch {
            val result = repository.importPasswordRecoveryDeepLink(intent)

            if (result.isSuccess) {
                _state.update {
                    it.copy(
                        jwt = repository.currentJwt(),
                        isRecoverySessionReady = true,
                        message = ""
                    )
                }
                onReady()
            } else {
                _state.update {
                    it.copy(
                        jwt = null,
                        isRecoverySessionReady = false,
                        message = authErrorMessage(
                            result.exceptionOrNull(),
                            fallback = text("auth.recoveryInvalid")
                        )
                    )
                }
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
                _state.update { it.copy(message = error) }
                onResult(false, error)
                return@launch
            }

            if (newPassword != confirmPassword) {
                val error = text("auth.passwordMismatch")
                _state.update { it.copy(message = error) }
                onResult(false, error)
                return@launch
            }

            _state.update { it.copy(isLoading = true, message = "") }

            val result = repository.updatePasswordAfterRecovery(newPassword)

            if (result.isSuccess) {
                val successMessage = text("auth.passwordUpdated")
                _state.update {
                    it.copy(
                        currentUser = null,
                        jwt = null,
                        isLoggedIn = false,
                        isRecoverySessionReady = false,
                        isLoading = false,
                        message = successMessage
                    )
                }
                onResult(true, successMessage)
            } else {
                val errorMessage = authErrorMessage(
                    result.exceptionOrNull(),
                    fallback = text("auth.passwordUpdateError")
                )
                _state.update { it.copy(isLoading = false, message = errorMessage) }
                onResult(false, errorMessage)
            }
        }
    }

    fun updateCurrentUser(user: UserDto) {
        _state.update { it.copy(currentUser = user) }
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
