package com.example.projecthub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.settings.AppDateFormat
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.settings.AppSettings
import com.example.projecthub.settings.AppThemeMode
import com.example.projecthub.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application.applicationContext)

    val settings: StateFlow<AppSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings()
    )

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun setThemeMode(themeMode: AppThemeMode) {
        viewModelScope.launch { repository.setThemeMode(themeMode) }
    }

    fun setDateFormat(dateFormat: AppDateFormat) {
        viewModelScope.launch { repository.setDateFormat(dateFormat) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    fun setSoundsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSoundsEnabled(enabled) }
    }
}
