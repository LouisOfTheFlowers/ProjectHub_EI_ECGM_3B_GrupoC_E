package com.example.projecthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.remote.supabase.AuthRemoteDataSource
import com.example.projecthub.remote.supabase.UserRemoteDataSource
import com.example.projecthub.remote.supabase.models.UserDto
import kotlinx.coroutines.launch

data class ProfileState(
    val user: UserDto? = null,
    val isSaving: Boolean = false,
    val isSendingEmailCode: Boolean = false,
    val emailCodeSent: Boolean = false,
    val isDeleting: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val authRemoteDataSource: AuthRemoteDataSource = AuthRemoteDataSource(),
    private val userRemoteDataSource: UserRemoteDataSource = UserRemoteDataSource()
) : ViewModel() {

    var state by mutableStateOf(ProfileState())
        private set

    fun setUser(user: UserDto?) {
        state = state.copy(user = user, message = null, errorMessage = null)
    }

    fun clearMessage() {
        state = state.copy(message = null)
    }

    fun sendEmailChangeCode() {
        val currentUser = state.user
        if (currentUser == null) {
            state = state.copy(errorMessage = "Nao foi possivel identificar a tua conta.")
            return
        }

        viewModelScope.launch {
            state = state.copy(
                isSendingEmailCode = true,
                emailCodeSent = false,
                message = null,
                errorMessage = null
            )

            val result = runCatching {
                authRemoteDataSource.sendReauthenticationCode()
            }

            state = if (result.isSuccess) {
                state.copy(
                    isSendingEmailCode = false,
                    emailCodeSent = true,
                    message = "Enviamos um codigo para ${currentUser.email}."
                )
            } else {
                state.copy(
                    isSendingEmailCode = false,
                    emailCodeSent = false,
                    errorMessage = accountErrorMessage(
                        result.exceptionOrNull(),
                        "Nao foi possivel enviar o codigo para o email atual."
                    )
                )
            }
        }
    }

    fun updatePhoto(
        photoUri: String?,
        onUserUpdated: (UserDto) -> Unit
    ) {
        val currentUser = state.user
        val userId = currentUser?.id

        if (currentUser == null || userId == null) {
            state = state.copy(errorMessage = "Nao foi possivel identificar a tua conta.")
            return
        }

        viewModelScope.launch {
            state = state.copy(isSaving = true, message = null, errorMessage = null)

            val updatedUser = currentUser.copy(foto = photoUri)
            val result = runCatching {
                userRemoteDataSource.updateUserPhoto(userId, photoUri)
            }

            if (result.isSuccess) {
                state = state.copy(
                    user = updatedUser,
                    isSaving = false,
                    message = if (photoUri.isNullOrBlank()) {
                        "Foto removida."
                    } else {
                        "Foto de perfil atualizada."
                    }
                )
                onUserUpdated(updatedUser)
            } else {
                state = state.copy(
                    isSaving = false,
                    errorMessage = result.exceptionOrNull()?.message
                        ?: "Nao foi possivel atualizar a foto de perfil."
                )
            }
        }
    }

    fun updateEmail(
        newEmail: String,
        verificationCode: String,
        onUserUpdated: (UserDto) -> Unit
    ) {
        val currentUser = state.user
        if (currentUser == null) {
            state = state.copy(errorMessage = "Nao foi possivel identificar a tua conta.")
            return
        }

        val trimmedEmail = newEmail.trim()
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            state = state.copy(errorMessage = "Insere um email valido.")
            return
        }

        if (trimmedEmail.equals(currentUser.email, ignoreCase = true)) {
            state = state.copy(errorMessage = "O novo email tem de ser diferente do atual.")
            return
        }

        val trimmedCode = verificationCode.trim()
        if (trimmedCode.isBlank()) {
            state = state.copy(errorMessage = "Insere o codigo enviado para o email atual.")
            return
        }

        viewModelScope.launch {
            state = state.copy(isSaving = true, message = null, errorMessage = null)

            val result = runCatching {
                authRemoteDataSource.updateEmail(trimmedEmail, trimmedCode)
                userRemoteDataSource.updateOwnEmail(trimmedEmail)
            }

            if (result.isSuccess) {
                val updatedUser = currentUser.copy(email = trimmedEmail)
                state = state.copy(
                    user = updatedUser,
                    isSaving = false,
                    emailCodeSent = false,
                    message = "Email atualizado. Se o Supabase pedir confirmacao, confirma no teu email."
                )
                onUserUpdated(updatedUser)
            } else {
                state = state.copy(
                    isSaving = false,
                    errorMessage = accountErrorMessage(
                        result.exceptionOrNull(),
                        "Nao foi possivel alterar o email."
                    )
                )
            }
        }
    }

    fun updatePassword(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        val currentUser = state.user
        if (currentUser == null) {
            state = state.copy(errorMessage = "Nao foi possivel identificar a tua conta.")
            return
        }

        if (oldPassword.isBlank()) {
            state = state.copy(errorMessage = "Insere a palavra-passe antiga.")
            return
        }

        if (newPassword.length < 8) {
            state = state.copy(errorMessage = "A nova palavra-passe deve ter pelo menos 8 caracteres.")
            return
        }

        if (newPassword != confirmPassword) {
            state = state.copy(errorMessage = "A confirmacao da palavra-passe nao coincide.")
            return
        }

        viewModelScope.launch {
            state = state.copy(isSaving = true, message = null, errorMessage = null)

            val result = runCatching {
                authRemoteDataSource.login(currentUser.email, oldPassword)
                authRemoteDataSource.updatePassword(newPassword)
            }

            if (result.isSuccess) {
                state = state.copy(
                    isSaving = false,
                    message = "Palavra-passe alterada com sucesso."
                )
            } else {
                state = state.copy(
                    isSaving = false,
                    errorMessage = accountErrorMessage(
                        result.exceptionOrNull(),
                        "Nao foi possivel alterar a palavra-passe."
                    )
                )
            }
        }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        val currentUser = state.user
        if (currentUser == null) {
            state = state.copy(errorMessage = "Nao foi possivel identificar a tua conta.")
            return
        }

        viewModelScope.launch {
            state = state.copy(isDeleting = true, message = null, errorMessage = null)

            val result = runCatching {
                userRemoteDataSource.deleteOwnAccount()
            }

            if (result.isSuccess) {
                runCatching { authRemoteDataSource.logout() }
                onDeleted()
            } else {
                state = state.copy(
                    isDeleting = false,
                    errorMessage = accountErrorMessage(
                        result.exceptionOrNull(),
                        "Nao foi possivel eliminar a conta."
                    )
                )
            }
        }
    }

    private fun accountErrorMessage(error: Throwable?, fallback: String): String {
        val rawMessage = error?.message.orEmpty()

        return when {
            rawMessage.contains("invalid login", ignoreCase = true) ||
                rawMessage.contains("invalid credentials", ignoreCase = true) ->
                "A palavra-passe antiga nao esta correta."

            rawMessage.contains("nonce", ignoreCase = true) ||
                rawMessage.contains("token", ignoreCase = true) ||
                rawMessage.contains("otp", ignoreCase = true) ->
                "O codigo inserido nao e valido ou expirou."

            rawMessage.contains("email", ignoreCase = true) &&
                rawMessage.contains("exists", ignoreCase = true) ->
                "Ja existe uma conta com esse email."

            rawMessage.contains("user_update_own_email", ignoreCase = true) ->
                "Aplica a migration de perfil no Supabase antes de alterar o email."

            rawMessage.contains("user_delete_own_account", ignoreCase = true) ->
                "Aplica a migration de perfil no Supabase antes de eliminar a conta."

            rawMessage.isBlank() -> fallback

            else -> rawMessage
                .lineSequence()
                .firstOrNull()
                ?.take(180)
                ?: fallback
        }
    }
}
