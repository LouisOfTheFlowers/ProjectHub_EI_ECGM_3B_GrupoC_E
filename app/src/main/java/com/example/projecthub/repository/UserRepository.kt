package com.example.projecthub.repository

import com.example.projecthub.local.dao.SyncQueueDao
import com.example.projecthub.local.dao.UserDao
import com.example.projecthub.local.entities.SyncQueueEntity
import com.example.projecthub.local.entities.UserEntity
import com.example.projecthub.remote.supabase.SupabaseClientProvider
import com.example.projecthub.remote.supabase.models.UserDto
import io.github.jan.supabase.postgrest.from

class UserRepository(
    private val userDao: UserDao,
    private val syncQueueDao: SyncQueueDao
) {

    suspend fun saveUserLocally(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun getLocalUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    suspend fun registerUser(userDto: UserDto) {
        SupabaseClientProvider.client
            .from("users")
            .insert(userDto)
    }

    suspend fun getUserByEmailRemote(email: String): UserDto? {
        return SupabaseClientProvider.client
            .from("users")
            .select {
                filter {
                    eq("email", email)
                }
            }
            .decodeSingleOrNull<UserDto>()
    }

    suspend fun updateUserOffline(user: UserEntity) {
        userDao.updateUser(user)

        syncQueueDao.insertSyncAction(
            SyncQueueEntity(
                action = "update_profile",
                payload = """
                    {
                        "id": ${user.id},
                        "nome": "${user.nome}",
                        "username": "${user.username}",
                        "email": "${user.email}",
                        "foto": "${user.foto}",
                        "status": "${user.status}"
                    }
                """.trimIndent(),
                synced = false
            )
        )
    }
}