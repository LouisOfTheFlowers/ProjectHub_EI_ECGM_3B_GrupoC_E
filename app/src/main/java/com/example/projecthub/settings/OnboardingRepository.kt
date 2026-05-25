package com.example.projecthub.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.onboardingDataStore by preferencesDataStore(name = "intro_onboarding")

class OnboardingRepository(private val context: Context) {
    private object Keys {
        val SeenIntros = stringSetPreferencesKey("seen_intro_keys")
    }

    suspend fun hasSeenIntro(userKey: String, role: String): Boolean {
        val key = introKey(userKey, role)
        val preferences = context.onboardingDataStore.data.first()
        return key in preferences[Keys.SeenIntros].orEmpty()
    }

    suspend fun markIntroSeen(userKey: String, role: String) {
        val key = introKey(userKey, role)
        context.onboardingDataStore.edit { preferences ->
            preferences[Keys.SeenIntros] = preferences[Keys.SeenIntros].orEmpty() + key
        }
    }

    private fun introKey(userKey: String, role: String): String {
        return "${userKey.trim().lowercase()}::${role.trim().uppercase()}"
    }
}
