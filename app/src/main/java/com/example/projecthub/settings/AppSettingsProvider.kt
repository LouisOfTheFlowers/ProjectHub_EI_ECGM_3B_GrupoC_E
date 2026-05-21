package com.example.projecthub.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember

val LocalAppSettings = compositionLocalOf { AppSettings() }

@Composable
fun AppSettingsProvider(
    settings: AppSettings,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppSettings provides settings) {
        content()
    }
}

@Composable
@ReadOnlyComposable
fun currentAppSettings(): AppSettings = LocalAppSettings.current

@Composable
fun rememberSoundClick(onClick: () -> Unit): () -> Unit {
    val settings = LocalAppSettings.current
    return remember(settings.soundsEnabled, onClick) {
        {
            if (settings.soundsEnabled) {
                AppSoundPlayer.playClick()
            }
            onClick()
        }
    }
}
