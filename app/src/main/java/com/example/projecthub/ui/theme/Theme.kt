package com.example.projecthub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ProjectHubColors.AccentLight,
    secondary = Mint80,
    tertiary = Cyan80,
    background = ProjectHubColors.DarkBackground,
    surface = ProjectHubColors.DarkSurface,
    error = ProjectHubColors.Danger,
    onPrimary = ProjectHubColors.DarkInk,
    onSecondary = ProjectHubColors.DarkInk,
    onTertiary = ProjectHubColors.DarkInk,
    onBackground = ProjectHubColors.DarkInk,
    onSurface = ProjectHubColors.DarkInk
)

private val LightColorScheme = lightColorScheme(
    primary = ProjectHubColors.Accent,
    secondary = ProjectHubColors.SuccessDark,
    tertiary = Cyan40,
    background = ProjectHubColors.LightBackground,
    surface = ProjectHubColors.LightSurface,
    error = ProjectHubColors.Danger,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = ProjectHubColors.LightInk,
    onSurface = ProjectHubColors.LightInk
)

@Composable
fun ProjectHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
