package com.example.projecthub.uiscreens.admin.tasks

import com.example.projecthub.uiscreens.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.R
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.viewmodel.admin.AdminProjectTaskGroup
import com.example.projecthub.viewmodel.admin.AdminTaskListItem
import com.example.projecthub.viewmodel.admin.AdminTaskStatusFilter
import com.example.projecthub.viewmodel.admin.AdminTasksState
import com.example.projecthub.viewmodel.admin.AdminTasksViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import com.example.projecthub.ui.theme.ProjectHubColors

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

