package com.example.projecthub.uiscreens

import android.content.res.Configuration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.heightIn

@Composable
fun isLandscapeLayout(): Boolean {
    return LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
}

@Composable
fun Modifier.responsiveDialogBody(): Modifier {
    val maxHeight = if (isLandscapeLayout()) 320.dp else 560.dp
    return heightIn(max = maxHeight)
        .verticalScroll(rememberScrollState())
}
