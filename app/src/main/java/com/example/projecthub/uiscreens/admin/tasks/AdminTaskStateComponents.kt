package com.example.projecthub.uiscreens.admin.tasks

import com.example.projecthub.uiscreens.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.admin.AdminTasksState
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.components.AppCompactStatCard

internal val TasksAccent = AuthAccent
internal val TasksGreen = ProjectHubColors.Success
internal val TasksOrange = ProjectHubColors.Warning
internal val TasksRed = ProjectHubColors.Danger


@Composable
internal fun TaskStats(state: AdminTasksState) {
    val language = currentAppSettings().language
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CompactStatCard(
            title = language.t("common.total"),
            value = state.totalTasks.toString(),
            accent = TasksAccent,
            modifier = Modifier.weight(1f)
        )
        CompactStatCard(
            title = language.t("common.pending"),
            value = state.pendingTasks.toString(),
            accent = TasksOrange,
            modifier = Modifier.weight(1f)
        )
        CompactStatCard(
            title = language.t("common.completed"),
            value = state.completedTasks.toString(),
            accent = TasksGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CompactStatCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    AppCompactStatCard(title = title, value = value, accent = accent, modifier = modifier)
}

