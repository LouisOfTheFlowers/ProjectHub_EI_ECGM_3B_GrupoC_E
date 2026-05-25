package com.example.projecthub.repository

import android.content.Intent
import com.example.projecthub.local.dao.SyncQueueDao
import com.example.projecthub.local.dao.UserDao
import com.example.projecthub.local.entities.UserEntity
import com.example.projecthub.remote.supabase.AuthRemoteDataSource
import com.example.projecthub.remote.supabase.UserRemoteDataSource
import com.example.projecthub.remote.supabase.models.UserDto
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException

data class AuthenticatedUser(
    val user: UserDto,
    val jwt: String
)

class UserRepository(
    private val userDao: UserDao,
    private val syncQueueDao: SyncQueueDao,
    private val authRemoteDataSource: AuthRemoteDataSource = AuthRemoteDataSource(),
    private val userRemoteDataSource: UserRemoteDataSource = UserRemoteDataSource()
) {

    suspend fun register(
        nome: String,
        username: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            try {
                authRemoteDataSource.register(
                    nome = nome,
                    username = username,
                    email = email,
                    password = password
                )
            } catch (e: AuthRestException) {
                if (!e.isExistingAuthUserError()) {
                    throw e
                }

                authRemoteDataSource.login(email, password)
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<AuthenticatedUser> {
        return try {
            val session = authRemoteDataSource.login(email, password)
            val user = userRemoteDataSource.getUserByEmail(email)

            if (user == null) {
                Result.failure(Exception("Utilizador não encontrado."))
            } else {
                userDao.insertUser(
                    UserEntity(
                        id = user.id ?: 0,
                        nome = user.nome,
                        username = user.username,
                        email = user.email,
                        password = "",
                        foto = user.foto,
                        role = user.role,
                        createdAt = user.createdAt,
                        status = user.status
                    )
                )

                Result.success(
                    AuthenticatedUser(
                        user = user,
                        jwt = session.accessToken
                    )
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            authRemoteDataSource.sendPasswordResetEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun handlePasswordRecoveryDeepLink(intent: Intent, onReady: () -> Unit) {
        authRemoteDataSource.handlePasswordRecoveryDeepLink(intent) {
            onReady()
        }
    }

    fun currentJwt(): String? {
        return authRemoteDataSource.currentJwt()
    }

    suspend fun updatePasswordAfterRecovery(newPassword: String): Result<Unit> {
        return try {
            authRemoteDataSource.updatePassword(newPassword)
            logout()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreSession(): Result<AuthenticatedUser?> {
        return try {
            val session = authRemoteDataSource.restoreSession()
                ?: return Result.success(null)

            val email = session.user?.email
                ?: userDao.getAllUsers().firstOrNull()?.email
                ?: return Result.success(null)

            val user = userRemoteDataSource.getUserByEmail(email)
                ?: userDao.getUserByEmail(email)?.toDto()
                ?: return Result.success(null)

            userDao.insertUser(user.toEntity())

            Result.success(
                AuthenticatedUser(
                    user = user,
                    jwt = session.accessToken
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        authRemoteDataSource.logout()
        userDao.deleteAllUsers()
    }

    private fun AuthRestException.isExistingAuthUserError(): Boolean {
        return errorCode == AuthErrorCode.EmailExists ||
            errorCode == AuthErrorCode.UserAlreadyExists ||
            error.contains("already", ignoreCase = true)
    }

    private fun UserDto.toEntity(): UserEntity {
        return UserEntity(
            id = id ?: 0,
            nome = nome,
            username = username,
            email = email,
            password = "",
            foto = foto,
            role = role,
            createdAt = createdAt,
            status = status
        )
    }

    private fun UserEntity.toDto(): UserDto {
        return UserDto(
            id = id,
            nome = nome,
            username = username,
            email = email,
            password = "",
            foto = foto,
            role = role,
            createdAt = createdAt,
            status = status
        )
    }
}
