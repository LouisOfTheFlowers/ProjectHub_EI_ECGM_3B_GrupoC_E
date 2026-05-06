package com.example.projecthub.remote.supabase

import com.example.projecthub.remote.supabase.models.UserDto
import io.github.jan.supabase.postgrest.from

class UserRemoteDataSource {

    suspend fun registerUser(user: UserDto) {
        SupabaseClientProvider.client
            .from("users")
            .insert(user)
    }

    suspend fun getUserByEmail(email: String): UserDto? {
        return try {
            SupabaseClientProvider.client
                .from("users")
                .select {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeAs<List<UserDto>>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUser(userId: Int, user: UserDto) {
        SupabaseClientProvider.client
            .from("users")
            .update(user) {
                filter {
                    eq("id", userId)
                }
            }
    }
}