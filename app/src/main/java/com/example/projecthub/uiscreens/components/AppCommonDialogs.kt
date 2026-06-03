package com.example.projecthub.uiscreens.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.AuthAccent
import com.example.projecthub.uiscreens.rememberSoundClick

@Composable
fun AppDialogConfirmButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = rememberSoundClick(onClick),
        enabled = enabled,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = AuthAccent,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AppDialogCancelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = rememberSoundClick(onClick),
        enabled = enabled,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = ProjectHubColors.Muted,
            fontWeight = FontWeight.Bold
        )
    }
}

