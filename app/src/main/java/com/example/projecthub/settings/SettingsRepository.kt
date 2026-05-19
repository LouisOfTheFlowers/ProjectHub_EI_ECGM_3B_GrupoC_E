package com.example.projecthub.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val Language = stringPreferencesKey("language")
        val ThemeMode = stringPreferencesKey("theme_mode")
        val DateFormat = stringPreferencesKey("date_format")
        val NotificationsEnabled = booleanPreferencesKey("notifications_enabled")
        val SoundsEnabled = booleanPreferencesKey("sounds_enabled")
    }

    val settings: Flow<AppSettings> = context.appSettingsDataStore.data.map { preferences ->
        AppSettings(
            language = AppLanguage.fromCode(preferences[Keys.Language]),
            themeMode = AppThemeMode.fromCode(preferences[Keys.ThemeMode]),
            dateFormat = AppDateFormat.fromCode(preferences[Keys.DateFormat]),
            notificationsEnabled = preferences[Keys.NotificationsEnabled] ?: true,
            soundsEnabled = preferences[Keys.SoundsEnabled] ?: true
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[Keys.Language] = language.code
        }
    }

    suspend fun setThemeMode(themeMode: AppThemeMode) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[Keys.ThemeMode] = themeMode.code
        }
    }

    suspend fun setDateFormat(dateFormat: AppDateFormat) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[Keys.DateFormat] = dateFormat.code
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[Keys.NotificationsEnabled] = enabled
        }
    }

    suspend fun setSoundsEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[Keys.SoundsEnabled] = enabled
        }
    }
}
