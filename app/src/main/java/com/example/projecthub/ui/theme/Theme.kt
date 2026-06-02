package com.example.projecthub.ui.theme

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
    surfaceVariant = ProjectHubColors.DarkSurfaceSoft,
    outline = ProjectHubColors.DarkBorder,
    error = ProjectHubColors.Danger,
    onPrimary = Color(0xFF032127),
    onSecondary = Color(0xFF062015),
    onTertiary = Color(0xFF082031),
    onBackground = ProjectHubColors.DarkInk,
    onSurface = ProjectHubColors.DarkInk,
    onSurfaceVariant = ProjectHubColors.DarkMuted,
    inverseSurface = Color(0xFFE8F3F5),
    inverseOnSurface = Color(0xFF0B141B)
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
    ProjectHubColors.applyTheme(darkTheme)

    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val colorScheme = if (darkTheme) {
        baseColorScheme
    } else {
        baseColorScheme.copy(
            surfaceContainerLowest = ProjectHubColors.LightSurface,
            surfaceContainerLow = ProjectHubColors.LightSurface,
            surfaceContainer = ProjectHubColors.LightSurface,
            surfaceContainerHigh = ProjectHubColors.LightSurface,
            surfaceContainerHighest = ProjectHubColors.LightSurface
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
