package com.example.projecthub.remote.supabase

import android.content.Intent
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.parseFragmentAndImportSession
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRemoteDataSource {

    suspend fun register(nome: String, username: String, email: String, password: String): UserInfo? {
        return SupabaseClientProvider.client.auth.signUpWith(
            Email,
            redirectUrl = EMAIL_CONFIRMATION_DEEP_LINK
        ) {
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

    suspend fun restoreSession(): UserSession? {
        val auth = SupabaseClientProvider.client.auth

        auth.awaitInitialization()

        auth.currentSessionOrNull()?.let { session ->
            return session
        }

        val sessionFound = auth.loadFromStorage()
        return if (sessionFound) {
            auth.currentSessionOrNull()
        } else {
            null
        }
    }

    fun currentJwt(): String? {
        return SupabaseClientProvider.client.auth.currentAccessTokenOrNull()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        SupabaseClientProvider.client.auth.resetPasswordForEmail(
            email = email,
            redirectUrl = PASSWORD_RESET_DEEP_LINK
        )
    }

    fun handlePasswordRecoveryDeepLink(intent: Intent, onSessionReady: (UserSession) -> Unit) {
        SupabaseClientProvider.client.handleDeeplinks(intent, onSessionReady)
    }

    @OptIn(SupabaseInternal::class)
    suspend fun importPasswordRecoveryDeepLink(intent: Intent): UserSession? {
        val data = intent.data ?: return null

        if (data.scheme != "projecthub" || data.host != "reset-password") {
            return null
        }

        val auth = SupabaseClientProvider.client.auth

        data.fragment?.takeIf { it.isNotBlank() }?.let { fragment ->
            return suspendCoroutine { continuation ->
                auth.parseFragmentAndImportSession(fragment) { session ->
                    continuation.resume(session)
                }
            }
        }

        data.getQueryParameter("code")?.takeIf { it.isNotBlank() }?.let { code ->
            return auth.exchangeCodeForSession(code)
        }

        return auth.currentSessionOrNull()
    }

    suspend fun sendReauthenticationCode() {
        SupabaseClientProvider.client.auth.reauthenticate()
    }

    suspend fun updateEmail(newEmail: String, nonce: String): UserInfo {
        return SupabaseClientProvider.client.auth.updateUser {
            email = newEmail
            this.nonce = nonce
        }
    }

    suspend fun updatePassword(newPassword: String): UserInfo {
        return SupabaseClientProvider.client.auth.updateUser {
            password = newPassword
        }
    }

    suspend fun logout() {
        SupabaseClientProvider.client.auth.signOut()
    }

    private companion object {
        const val EMAIL_CONFIRMATION_DEEP_LINK = "projecthub://confirm-email"
        const val PASSWORD_RESET_DEEP_LINK = "projecthub://reset-password"
    }
}
