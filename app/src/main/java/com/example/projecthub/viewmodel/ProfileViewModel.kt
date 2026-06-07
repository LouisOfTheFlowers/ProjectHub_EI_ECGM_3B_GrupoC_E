package com.example.projecthub.viewmodel

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.local.database.DatabaseProvider
import com.example.projecthub.local.entities.SyncQueueEntity
import com.example.projecthub.local.entities.UserEntity
import com.example.projecthub.remote.supabase.AuthRemoteDataSource
import com.example.projecthub.remote.supabase.UserRemoteDataSource
import com.example.projecthub.remote.supabase.models.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class ProfileState(
    val user: UserDto? = null,
    val isSaving: Boolean = false,
    val isSendingEmailCode: Boolean = false,
    val emailCodeSent: Boolean = false,
    val isDeleting: Boolean = false,
    val isOffline: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authRemoteDataSource = AuthRemoteDataSource()
    private val userRemoteDataSource = UserRemoteDataSource()
    private val database = DatabaseProvider.getDatabase(application)
    private val syncQueueDao = database.syncQueueDao()
    private val userDao = database.userDao()
    private val connectivityManager = application.getSystemService(ConnectivityManager::class.java)
    private val json = Json { ignoreUnknownKeys = true }
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        private fun refreshNetworkState() {
            val online = hasInternet()
            _state.update { it.copy(isOffline = !online) }
            if (online) {
                syncPendingProfileUpdates()
            }
        }

        override fun onAvailable(network: Network) {
            refreshNetworkState()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            refreshNetworkState()
        }

        override fun onLost(network: Network) {
            refreshNetworkState()
        }
    }

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    init {
        _state.update { it.copy(isOffline = !hasInternet()) }
        runCatching { connectivityManager.registerDefaultNetworkCallback(networkCallback) }
        syncPendingProfileUpdates()
    }

    override fun onCleared() {
        runCatching { connectivityManager.unregisterNetworkCallback(networkCallback) }
        super.onCleared()
    }

    fun setUser(user: UserDto?) {
        if (user == null) {
            _state.update { it.copy(user = null, message = null, errorMessage = null) }
            return
        }

        _state.update { it.copy(user = user, message = null, errorMessage = null) }

        viewModelScope.launch {
            val userId = user.id
            val localUser = userId?.let { userDao.getUserById(it) }
            val hasPendingPhoto = userId?.let { hasPendingProfilePhotoUpdate(it) } == true
            val hasPendingDetails = userId?.let { hasPendingProfileDetailsUpdate(it) } == true
            val mergedUser = if (localUser != null && (hasPendingPhoto || hasPendingDetails)) {
                user.copy(
                    nome = if (hasPendingDetails) localUser.nome else user.nome,
                    username = if (hasPendingDetails) localUser.username else user.username,
                    foto = if (hasPendingPhoto) localUser.foto else user.foto
                )
            } else {
                user
            }

            saveUserLocally(mergedUser)
            _state.update { it.copy(user = mergedUser) }
            syncPendingProfileUpdates()
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    fun sendEmailChangeCode() {
        val currentUser = _state.value.user
        if (currentUser == null) {
            _state.update { it.copy(errorMessage = "Não foi possível identificar a tua conta.") }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSendingEmailCode = true,
                    emailCodeSent = false,
                    message = null,
                    errorMessage = null
                )
            }

            val result = runCatching {
                authRemoteDataSource.sendReauthenticationCode()
            }

            _state.update { state ->
                if (result.isSuccess) {
                    state.copy(
                        isSendingEmailCode = false,
                        emailCodeSent = true,
                        message = "Enviámos um código para ${currentUser.email}."
                    )
                } else {
                    state.copy(
                        isSendingEmailCode = false,
                        emailCodeSent = false,
                        errorMessage = accountErrorMessage(
                            result.exceptionOrNull(),
                            "Não foi possível enviar o código para o email atual."
                        )
                    )
                }
            }
        }
    }

    fun updatePhoto(
        photoUri: String?,
        onUserUpdated: (UserDto) -> Unit
    ) {
        val currentUser = _state.value.user
        val userId = currentUser?.id

        if (currentUser == null || userId == null) {
            _state.update { it.copy(errorMessage = "Não foi possível identificar a tua conta.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, message = null, errorMessage = null) }

            val updatedUser = currentUser.copy(foto = photoUri)

            if (!hasInternet()) {
                saveProfilePhotoOffline(updatedUser, userId, photoUri, onUserUpdated)
                return@launch
            }

            val result = runCatching {
                userRemoteDataSource.updateUserPhoto(userId, photoUri)
            }

            if (result.isSuccess) {
                saveUserLocally(updatedUser)
                _state.update {
                    it.copy(
                        user = updatedUser,
                        isSaving = false,
                        message = if (photoUri.isNullOrBlank()) {
                            "Foto removida."
                        } else {
                            "Foto de perfil atualizada."
                        }
                    )
                }
                onUserUpdated(updatedUser)
                syncPendingProfileUpdates()
            } else if (isNetworkError(result.exceptionOrNull())) {
                saveProfilePhotoOffline(updatedUser, userId, photoUri, onUserUpdated)
            } else {
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Não foi possível atualizar a foto de perfil."
                    )
                }
            }
        }
    }

    private suspend fun saveProfilePhotoOffline(
        updatedUser: UserDto,
        userId: Int,
        photoUri: String?,
        onUserUpdated: (UserDto) -> Unit
    ) {
        saveUserLocally(updatedUser)
        queueProfilePhotoUpdate(userId, photoUri)
        _state.update {
            it.copy(
                user = updatedUser,
                isSaving = false,
                message = if (photoUri.isNullOrBlank()) {
                    "Foto removida offline. Será sincronizada quando houver internet."
                } else {
                    "Foto guardada offline. Será sincronizada quando houver internet."
                },
                errorMessage = null
            )
        }
        onUserUpdated(updatedUser)
    }

    fun updateProfileDetails(
        nome: String,
        username: String,
        onUserUpdated: (UserDto) -> Unit
    ) {
        val currentUser = _state.value.user
        val userId = currentUser?.id

        if (currentUser == null || userId == null) {
            _state.update { it.copy(errorMessage = "Não foi possível identificar a tua conta.") }
            return
        }

        val trimmedName = nome.trim()
        val trimmedUsername = username.trim()

        if (trimmedName.isBlank()) {
            _state.update { it.copy(errorMessage = "Indica o teu nome.") }
            return
        }

        if (trimmedUsername.isBlank()) {
            _state.update { it.copy(errorMessage = "Indica o teu username.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, message = null, errorMessage = null) }

            val updatedUser = currentUser.copy(
                nome = trimmedName,
                username = trimmedUsername
            )

            if (!hasInternet()) {
                saveProfileDetailsOffline(updatedUser, userId, trimmedName, trimmedUsername, onUserUpdated)
                return@launch
            }

            val result = runCatching {
                userRemoteDataSource.updateUserProfile(userId, trimmedName, trimmedUsername)
            }

            if (result.isSuccess) {
                saveUserLocally(updatedUser)
                _state.update {
                    it.copy(
                        user = updatedUser,
                        isSaving = false,
                        message = "Dados do perfil atualizados.",
                        errorMessage = null
                    )
                }
                onUserUpdated(updatedUser)
                syncPendingProfileUpdates()
            } else if (isNetworkError(result.exceptionOrNull())) {
                saveProfileDetailsOffline(updatedUser, userId, trimmedName, trimmedUsername, onUserUpdated)
            } else {
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Não foi possível atualizar os dados do perfil."
                    )
                }
            }
        }
    }

    private suspend fun saveProfileDetailsOffline(
        updatedUser: UserDto,
        userId: Int,
        nome: String,
        username: String,
        onUserUpdated: (UserDto) -> Unit
    ) {
        saveUserLocally(updatedUser)
        queueProfileDetailsUpdate(userId, nome, username)
        _state.update {
            it.copy(
                user = updatedUser,
                isSaving = false,
                message = "Dados do perfil guardados offline. Serão sincronizados quando houver internet.",
                errorMessage = null
            )
        }
        onUserUpdated(updatedUser)
    }

    fun updateEmail(
        newEmail: String,
        verificationCode: String,
        onUserUpdated: (UserDto) -> Unit
    ) {
        val currentUser = _state.value.user
        if (currentUser == null) {
            _state.update { it.copy(errorMessage = "Não foi possível identificar a tua conta.") }
            return
        }

        val trimmedEmail = newEmail.trim()
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            _state.update { it.copy(errorMessage = "Insere um email válido.") }
            return
        }

        if (trimmedEmail.equals(currentUser.email, ignoreCase = true)) {
            _state.update { it.copy(errorMessage = "O novo email tem de ser diferente do atual.") }
            return
        }

        val trimmedCode = verificationCode.trim()
        if (trimmedCode.isBlank()) {
            _state.update { it.copy(errorMessage = "Insere o código enviado para o email atual.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, message = null, errorMessage = null) }

            val result = runCatching {
                authRemoteDataSource.updateEmail(trimmedEmail, trimmedCode)
                userRemoteDataSource.updateOwnEmail(trimmedEmail)
            }

            if (result.isSuccess) {
                val updatedUser = currentUser.copy(email = trimmedEmail)
                _state.update {
                    it.copy(
                        user = updatedUser,
                        isSaving = false,
                        emailCodeSent = false,
                        message = "Email atualizado. Se o Supabase pedir confirmação, confirma no teu email."
                    )
                }
                onUserUpdated(updatedUser)
            } else {
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = accountErrorMessage(
                            result.exceptionOrNull(),
                            "Não foi possível alterar o email."
                        )
                    )
                }
            }
        }
    }

    fun updatePassword(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        val currentUser = _state.value.user
        if (currentUser == null) {
            _state.update { it.copy(errorMessage = "Não foi possível identificar a tua conta.") }
            return
        }

        if (oldPassword.isBlank()) {
            _state.update { it.copy(errorMessage = "Insere a palavra-passe antiga.") }
            return
        }

        if (newPassword.length < 8) {
            _state.update { it.copy(errorMessage = "A nova palavra-passe deve ter pelo menos 8 caracteres.") }
            return
        }

        if (newPassword != confirmPassword) {
            _state.update { it.copy(errorMessage = "A confirmação da palavra-passe não coincide.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, message = null, errorMessage = null) }

            val result = runCatching {
                authRemoteDataSource.login(currentUser.email, oldPassword)
                authRemoteDataSource.updatePassword(newPassword)
            }

            if (result.isSuccess) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        message = "Palavra-passe alterada com sucesso."
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = accountErrorMessage(
                            result.exceptionOrNull(),
                            "Não foi possível alterar a palavra-passe."
                        )
                    )
                }
            }
        }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        val currentUser = _state.value.user
        if (currentUser == null) {
            _state.update { it.copy(errorMessage = "Não foi possível identificar a tua conta.") }
            return
        }

        if (currentUser.role.equals("ADMIN", ignoreCase = true)) {
            _state.update { it.copy(errorMessage = "A conta de administrador não pode ser eliminada.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true, message = null, errorMessage = null) }

            val result = runCatching {
                userRemoteDataSource.deleteOwnAccount()
            }

            if (result.isSuccess) {
                runCatching { authRemoteDataSource.logout() }
                onDeleted()
            } else {
                _state.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = accountErrorMessage(
                            result.exceptionOrNull(),
                            "Não foi possível eliminar a conta."
                        )
                    )
                }
            }
        }
    }

    private fun accountErrorMessage(error: Throwable?, fallback: String): String {
        val rawMessage = error?.message.orEmpty()

        return when {
            isNetworkError(error) ->
                "Esta ação precisa de ligação à internet por motivos de segurança."

            rawMessage.contains("invalid login", ignoreCase = true) ||
                rawMessage.contains("invalid credentials", ignoreCase = true) ->
                "A palavra-passe antiga não está correta."

            rawMessage.contains("nonce", ignoreCase = true) ||
                rawMessage.contains("token", ignoreCase = true) ||
                rawMessage.contains("otp", ignoreCase = true) ->
                "O código inserido não é válido ou expirou."

            rawMessage.contains("email", ignoreCase = true) &&
                rawMessage.contains("exists", ignoreCase = true) ->
                "Já existe uma conta com esse email."

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

    private fun syncPendingProfileUpdates() {
        viewModelScope.launch {
            if (!hasInternet()) return@launch

            val pendingActions = syncQueueDao.getPendingSyncActions()
                .filter { it.action == PROFILE_PHOTO_UPDATE || it.action == PROFILE_DETAILS_UPDATE }

            pendingActions.forEach { action ->
                when (action.action) {
                    PROFILE_PHOTO_UPDATE -> syncProfilePhotoAction(action)
                    PROFILE_DETAILS_UPDATE -> syncProfileDetailsAction(action)
                }
            }

            syncQueueDao.deleteSyncedActions()
        }
    }

    private suspend fun syncProfilePhotoAction(action: SyncQueueEntity) {
        val payload = action.decodeProfilePhotoPayload() ?: return
        val result = runCatching {
            userRemoteDataSource.updateUserPhoto(payload.userId, payload.photoUri)
        }

        if (result.isSuccess) {
            syncQueueDao.markAsSynced(action.id)
            val currentUser = _state.value.user
            if (currentUser?.id == payload.userId) {
                val syncedUser = currentUser.copy(foto = payload.photoUri)
                saveUserLocally(syncedUser)
                _state.update { it.copy(user = syncedUser) }
            }
        }
    }

    private suspend fun syncProfileDetailsAction(action: SyncQueueEntity) {
        val payload = action.decodeProfileDetailsPayload() ?: return
        val result = runCatching {
            userRemoteDataSource.updateUserProfile(payload.userId, payload.nome, payload.username)
        }

        if (result.isSuccess) {
            syncQueueDao.markAsSynced(action.id)
            val currentUser = _state.value.user
            if (currentUser?.id == payload.userId) {
                val syncedUser = currentUser.copy(
                    nome = payload.nome,
                    username = payload.username
                )
                saveUserLocally(syncedUser)
                _state.update { it.copy(user = syncedUser) }
            }
        }
    }

    private suspend fun queueProfilePhotoUpdate(userId: Int, photoUri: String?) {
        syncQueueDao.insertSyncAction(
            SyncQueueEntity(
                action = PROFILE_PHOTO_UPDATE,
                payload = json.encodeToString(ProfilePhotoSyncPayload(userId, photoUri))
            )
        )
    }

    private suspend fun queueProfileDetailsUpdate(userId: Int, nome: String, username: String) {
        syncQueueDao.insertSyncAction(
            SyncQueueEntity(
                action = PROFILE_DETAILS_UPDATE,
                payload = json.encodeToString(ProfileDetailsSyncPayload(userId, nome, username))
            )
        )
    }

    private suspend fun hasPendingProfilePhotoUpdate(userId: Int): Boolean {
        return syncQueueDao.getPendingSyncActions()
            .filter { it.action == PROFILE_PHOTO_UPDATE }
            .any { action ->
                action.decodeProfilePhotoPayload()?.userId == userId
            }
    }

    private suspend fun hasPendingProfileDetailsUpdate(userId: Int): Boolean {
        return syncQueueDao.getPendingSyncActions()
            .filter { it.action == PROFILE_DETAILS_UPDATE }
            .any { action ->
                action.decodeProfileDetailsPayload()?.userId == userId
            }
    }

    private suspend fun saveUserLocally(user: UserDto) {
        val userId = user.id ?: return
        userDao.insertUser(
            UserEntity(
                id = userId,
                nome = user.nome,
                username = user.username,
                email = user.email,
                password = user.password,
                foto = user.foto,
                role = user.role,
                createdAt = user.createdAt,
                status = user.status
            )
        )
    }

    private fun SyncQueueEntity.decodeProfilePhotoPayload(): ProfilePhotoSyncPayload? {
        return runCatching {
            json.decodeFromString<ProfilePhotoSyncPayload>(payload)
        }.getOrNull()
    }

    private fun SyncQueueEntity.decodeProfileDetailsPayload(): ProfileDetailsSyncPayload? {
        return runCatching {
            json.decodeFromString<ProfileDetailsSyncPayload>(payload)
        }.getOrNull()
    }

    private fun hasInternet(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun isNetworkError(error: Throwable?): Boolean {
        val message = error?.message.orEmpty()
        return message.contains("network", ignoreCase = true) ||
            message.contains("timeout", ignoreCase = true) ||
            message.contains("unable to resolve host", ignoreCase = true) ||
            message.contains("failed to connect", ignoreCase = true) ||
            message.contains("connection", ignoreCase = true)
    }

    @Serializable
    private data class ProfilePhotoSyncPayload(
        val userId: Int,
        val photoUri: String?
    )

    @Serializable
    private data class ProfileDetailsSyncPayload(
        val userId: Int,
        val nome: String,
        val username: String
    )

    private companion object {
        const val PROFILE_PHOTO_UPDATE = "PROFILE_PHOTO_UPDATE"
        const val PROFILE_DETAILS_UPDATE = "PROFILE_DETAILS_UPDATE"
    }
}
