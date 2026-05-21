package com.example.projecthub.remote.supabase

import com.example.projecthub.remote.supabase.models.UserDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class UserRemoteDataSource {

    @Serializable
    private data class DeleteUserParams(
        @SerialName("p_user_id")
        val userId: Int
    )

    @Serializable
    private data class UpdateOwnEmailParams(
        @SerialName("p_new_email")
        val newEmail: String
    )

    suspend fun getUsers(): List<UserDto> {
        return SupabaseClientProvider.client
            .from("users")
            .select()
            .decodeAs<List<UserDto>>()
    }

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

    suspend fun updateUserPhoto(userId: Int, photoUri: String?) {
        SupabaseClientProvider.client
            .from("users")
            .update(mapOf("foto" to photoUri)) {
                filter {
                    eq("id", userId)
                }
            }
    }

    suspend fun updateOwnEmail(newEmail: String) {
        SupabaseClientProvider.client
            .postgrest
            .rpc("user_update_own_email", UpdateOwnEmailParams(newEmail))
    }

    suspend fun deleteUser(userId: Int) {
        SupabaseClientProvider.client
            .postgrest
            .rpc("admin_delete_user", DeleteUserParams(userId))
    }

    suspend fun deleteOwnAccount() {
        SupabaseClientProvider.client
            .postgrest
            .rpc("user_delete_own_account")
    }
}
