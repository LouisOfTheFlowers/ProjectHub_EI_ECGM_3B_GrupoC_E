package com.example.projecthub.remote.supabase

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRemoteDataSource {

    suspend fun register(nome: String, username: String, email: String, password: String): UserInfo? {
        return SupabaseClientProvider.client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("nome", nome)
                put("username", username)
            }
        }
    }

    suspend fun login(email: String, password: String): UserSession {
        val auth = SupabaseClientProvider.client.auth

        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        return auth.currentSessionOrNull()
            ?: throw IllegalStateException("Sessão JWT não encontrada após o login.")
    }

    fun currentJwt(): String? {
        return SupabaseClientProvider.client.auth.currentAccessTokenOrNull()
    }

    suspend fun logout() {
        SupabaseClientProvider.client.auth.signOut()
    }
}
