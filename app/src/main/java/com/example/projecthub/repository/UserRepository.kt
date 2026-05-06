package com.example.projecthub.repository

import com.example.projecthub.local.dao.SyncQueueDao
import com.example.projecthub.local.dao.UserDao
import com.example.projecthub.local.entities.UserEntity
import com.example.projecthub.remote.supabase.UserRemoteDataSource
import com.example.projecthub.remote.supabase.models.UserDto

class UserRepository(
    private val userDao: UserDao,
    private val syncQueueDao: SyncQueueDao,
    private val userRemoteDataSource: UserRemoteDataSource = UserRemoteDataSource()
) {

    suspend fun register(
        nome: String,
        username: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val existingUser = userRemoteDataSource.getUserByEmail(email)

            if (existingUser != null) {
                return Result.failure(Exception("Já existe uma conta com este email."))
            }

            val newUser = UserDto(
                id = null,
                nome = nome,
                username = username,
                email = email,
                password = password,
                foto = null,
                role = "UTILIZADOR",
                createdAt = null,
                status = "ATIVO"
            )

            userRemoteDataSource.registerUser(newUser)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<UserDto> {
        return try {
            val user = userRemoteDataSource.getUserByEmail(email)

            if (user == null) {
                Result.failure(Exception("Utilizador não encontrado."))

            } else if (user.password != password) {
                Result.failure(Exception("Password incorreta."))

            } else {
                userDao.insertUser(
                    UserEntity(
                        id = user.id ?: 0,
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

                Result.success(user)
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}