package com.example.projecthub.uiscreens.admin.teams

import com.example.projecthub.uiscreens.*

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.components.AppStatusChip

internal val TeamsAccent = AuthAccent
internal val TeamsGreen = ProjectHubColors.SuccessDark
internal val TeamsOrange = ProjectHubColors.Warning
internal val TeamsRed = ProjectHubColors.DangerDark


@Composable
internal fun StatusChip(text: String, color: Color) {
    AppStatusChip(text = text, contentColor = color)
}

