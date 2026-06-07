package com.example.projecthub.uiscreens

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.heightIn

data class AppResponsiveLayout(
    val isLandscape: Boolean,
    val isCompactHeight: Boolean,
    val sidebarWidth: Dp,
    val topBarHeight: Dp,
    val contentPadding: PaddingValues
)

@Composable
fun appResponsiveLayout(): AppResponsiveLayout {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isCompactHeight = configuration.screenHeightDp < 500
    val sidebarWidth = when {
        !isLandscape -> 284.dp
        configuration.screenWidthDp < 760 -> 216.dp
        else -> 252.dp
    }

    return AppResponsiveLayout(
        isLandscape = isLandscape,
        isCompactHeight = isCompactHeight,
        sidebarWidth = sidebarWidth,
        topBarHeight = if (isCompactHeight) 54.dp else 62.dp,
        contentPadding = PaddingValues(
            horizontal = if (isLandscape) 24.dp else 20.dp,
            vertical = if (isCompactHeight) 14.dp else 22.dp
        )
    )
}

@Composable
fun isLandscapeLayout(): Boolean {
    return appResponsiveLayout().isLandscape
}

@Composable
fun Modifier.responsiveDialogBody(): Modifier {
    val layout = appResponsiveLayout()
    val maxHeight = if (layout.isCompactHeight) 280.dp else 560.dp
    return heightIn(max = maxHeight)
        .verticalScroll(rememberScrollState())
}
