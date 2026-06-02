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

private val TasksAccent = AuthAccent
private val TasksGreen = ProjectHubColors.Success
private val TasksOrange = ProjectHubColors.Warning
private val TasksRed = ProjectHubColors.Danger

@Composable
fun AdminTasksScreen(
    viewModel: AdminTasksViewModel = viewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    var isCreatingTask by remember { mutableStateOf(false) }

    if (isCreatingTask) {
        AdminAddTaskScreen(
            state = state,
            onBack = {
                viewModel.clearCreateError()
                isCreatingTask = false
            },
            onCreate = { title, description, projectId, startDate, endDate ->
                viewModel.createTask(
                    title = title,
                    description = description,
                    projectId = projectId,
                    startDateText = startDate,
                    endDateText = endDate,
                    onSuccess = { isCreatingTask = false }
                )
            }
        )
    } else {
        Column {
            TasksHeader(onAddTask = {
                viewModel.clearCreateError()
                isCreatingTask = true
            })
            Spacer(modifier = Modifier.height(18.dp))
            TaskStats(state = state)
            Spacer(modifier = Modifier.height(16.dp))
            TaskFilters(
                state = state,
                onSearchChange = viewModel::updateSearchQuery,
                onStatusChange = viewModel::updateStatusFilter
            )
            Spacer(modifier = Modifier.height(18.dp))
            TaskProjectList(
                state = state,
                onToggleProject = viewModel::toggleProject
            )
        }
    }
}

@Composable
private fun TasksHeader(onAddTask: () -> Unit) {
    val language = currentAppSettings().language
    val addTaskClick = rememberSoundClick(onAddTask)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = language.t("tasks.title"),
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
            )
            Text(
                text = language.t("tasks.subtitle"),
                color = ProjectHubColors.Muted,
                fontSize = 14.sp
            )
        }

        Button(
            onClick = addTaskClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = TasksAccent,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = language.t("tasks.add"),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun TaskStats(state: AdminTasksState) {
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

@Composable
private fun TaskFilters(
    state: AdminTasksState,
    onSearchChange: (String) -> Unit,
    onStatusChange: (AdminTaskStatusFilter) -> Unit
) {
    val language = currentAppSettings().language
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(language.t("tasks.search")) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            StatusDropdown(
                selected = state.selectedStatus,
                onOptionSelected = onStatusChange
            )
        }
    }
}

@Composable
private fun StatusDropdown(
    selected: AdminTaskStatusFilter,
    onOptionSelected: (AdminTaskStatusFilter) -> Unit
) {
    val language = currentAppSettings().language
    AppDropdownField(
        selected = selected,
        options = AdminTaskStatusFilter.entries,
        label = { (it ?: selected).translatedLabel(language) },
        onOptionSelected = onOptionSelected
    )
}

private fun AdminTaskStatusFilter.translatedLabel(language: AppLanguage): String {
    return when (this) {
        AdminTaskStatusFilter.All -> language.t("filters.tasks.all")
        AdminTaskStatusFilter.Pending -> language.t("filters.tasks.pending")
        AdminTaskStatusFilter.Completed -> language.t("filters.tasks.completed")
    }
}

@Composable
private fun TaskProjectList(
    state: AdminTasksState,
    onToggleProject: (Int) -> Unit
) {
    val language = currentAppSettings().language
    when {
        state.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TasksAccent)
            }
        }

        state.errorMessage != null -> {
            Text(
                text = state.errorMessage.orEmpty(),
                color = TasksRed,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        state.projectGroups.isEmpty() -> {
            Text(
                text = language.t("tasks.noProjects"),
                color = ProjectHubColors.Muted,
                fontSize = 15.sp
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 760.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = state.projectGroups,
                    key = { it.projectId }
                ) { group ->
                    ProjectTaskSection(
                        group = group,
                        onToggleProject = { onToggleProject(group.projectId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectTaskSection(
    group: AdminProjectTaskGroup,
    onToggleProject: () -> Unit
) {
    val language = currentAppSettings().language
    val toggleClick = rememberSoundClick(onToggleProject)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = toggleClick)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(TasksAccent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (group.isExpanded) "-" else "+",
                        color = TasksAccent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.projectName,
                        color = ProjectHubColors.Ink,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = language.t("tasks.visibleSummary").format(group.visibleTasks.size, group.pendingTasks, group.completedTasks),
                        color = ProjectHubColors.Muted,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = group.totalTasks.toString(),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(TasksAccent.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    color = TasksAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            if (group.isExpanded) {
                if (group.visibleTasks.isEmpty()) {
                    Text(
                        text = language.t("tasks.noMatching"),
                        modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                        color = ProjectHubColors.Muted,
                        fontSize = 14.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 520.dp)
                            .padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = group.visibleTasks,
                            key = { it.id }
                        ) { task ->
                            TaskCard(task = task)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCard(task: AdminTaskListItem) {
    val statusColor = when {
        task.isCompleted -> TasksGreen
        task.isDelayed -> TasksRed
        else -> TasksOrange
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.SurfaceSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.statusLabel.uppercase(),
                        color = statusColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.title,
                        color = ProjectHubColors.Ink,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = task.description,
                        color = ProjectHubColors.Muted,
                        fontSize = 13.sp
                    )
                }

                CompletionIcon(
                    isCompleted = task.isCompleted,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TaskInfoRow(currentAppSettings().language.t("common.start"), task.startDate.toDisplayDate(currentAppSettings().dateFormat.pattern))
            TaskInfoRow(currentAppSettings().language.t("common.deadline"), task.dueDate.toDisplayDate(currentAppSettings().dateFormat.pattern))
        }
    }
}

@Composable
private fun CompletionIcon(
    isCompleted: Boolean,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(if (isCompleted) R.drawable.ic_check_circle_24 else R.drawable.ic_schedule_24),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun TaskInfoRow(label: String, value: String) {
    AppInfoRow(label = label, value = value)
}

private fun String.toDisplayDate(pattern: String = "dd/MM/yyyy"): String {
    val trimmed = trim()

    if (trimmed.isBlank() || trimmed == "-") {
        return trimmed
    }

    val date = try {
        LocalDate.parse(trimmed.take(10))
    } catch (_: DateTimeParseException) {
        return trimmed
    }

    return date.format(DateTimeFormatter.ofPattern(pattern))
}
