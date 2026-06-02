package com.example.projecthub.uiscreens.gestor.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.R
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.responsiveDialogBody
import com.example.projecthub.viewmodel.gestor.GestorProjectTaskGroup
import com.example.projecthub.viewmodel.gestor.GestorTaskInfoObservation
import com.example.projecthub.viewmodel.gestor.GestorTaskInfoState
import com.example.projecthub.viewmodel.gestor.GestorTaskListItem
import com.example.projecthub.viewmodel.gestor.GestorTaskProjectOption
import com.example.projecthub.viewmodel.gestor.GestorTaskStatusFilter
import com.example.projecthub.viewmodel.gestor.GestorTaskUserOption
import com.example.projecthub.viewmodel.gestor.GestorTasksState
import com.example.projecthub.viewmodel.gestor.GestorTasksViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun TaskStats(state: GestorTasksState) {
    val language = currentAppSettings().language
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SmallStat(language.t("common.total"), state.totalTasks.toString(), GestorTasksAccent, Modifier.weight(1f))
        SmallStat(language.t("common.pending"), state.pendingTasks.toString(), GestorTasksOrange, Modifier.weight(1f))
        SmallStat(language.t("common.inProgress"), state.inProgressTasks.toString(), GestorTasksBlue, Modifier.weight(1f))
        SmallStat(language.t("common.completed"), state.completedTasks.toString(), GestorTasksGreen, Modifier.weight(1f))
    }
}

@Composable
private fun SmallStat(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier
) {
    _root_ide_package_.com.example.projecthub.uiscreens.AppCompactStatCard(
        title = title,
        value = value,
        accent = accent,
        modifier = modifier,
        heightDp = 72
    )
}

@Composable
internal fun TaskFilters(
    state: GestorTasksState,
    onSearchChange: (String) -> Unit,
    onStatusChange: (GestorTaskStatusFilter) -> Unit
) {
    val language = currentAppSettings().language

    _root_ide_package_.com.example.projecthub.uiscreens.AppSearchField(
        value = state.searchQuery,
        onValueChange = onSearchChange,
        placeholder = language.t("tasks.searchManager"),
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = ProjectHubColors.LightSurface,
            unfocusedContainerColor = ProjectHubColors.LightSurface,
            disabledContainerColor = ProjectHubColors.LightSurface,
            focusedIndicatorColor = ProjectHubColors.BorderSoft,
            unfocusedIndicatorColor = ProjectHubColors.BorderSoft
        )
    )

    Spacer(modifier = Modifier.height(10.dp))

    StatusDropdown(
        selected = state.selectedStatus,
        onOptionSelected = onStatusChange
    )
}

@Composable
private fun StatusDropdown(
    selected: GestorTaskStatusFilter,
    onOptionSelected: (GestorTaskStatusFilter) -> Unit
) {
    val language = currentAppSettings().language
    _root_ide_package_.com.example.projecthub.uiscreens.AppDropdownField(
        selected = selected,
        options = GestorTaskStatusFilter.entries,
        label = { (it ?: selected).translatedLabel(language) },
        onOptionSelected = onOptionSelected
    )
}

private fun GestorTaskStatusFilter.translatedLabel(language: com.example.projecthub.settings.AppLanguage): String {
    return when (this) {
        GestorTaskStatusFilter.All -> language.t("filters.tasks.all")
        GestorTaskStatusFilter.Pending -> language.t("filters.tasks.pending")
        GestorTaskStatusFilter.InProgress -> language.t("filters.tasks.inProgress")
        GestorTaskStatusFilter.Completed -> language.t("filters.tasks.completed")
    }
}

@Composable
internal fun TaskProjectList(
    state: GestorTasksState,
    onToggleProject: (Int) -> Unit,
    onEditTask: (GestorTaskListItem) -> Unit,
    onDeleteTask: (GestorTaskListItem) -> Unit,
    onMoreInfo: (GestorTaskListItem) -> Unit,
    onObservations: (GestorTaskListItem) -> Unit
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
                CircularProgressIndicator(color = GestorTasksAccent)
            }
        }

        state.errorMessage != null -> {
            Text(
                text = state.errorMessage.orEmpty(),
                color = GestorTasksRed,
                fontWeight = FontWeight.Bold
            )
        }

        state.projectGroups.isEmpty() -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Text(
                    text = language.t("tasks.noFiltered"),
                    color = ProjectHubColors.Muted,
                    modifier = Modifier.padding(18.dp)
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 760.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = state.projectGroups,
                    key = { it.projectId }
                ) { group ->
                    ProjectTaskGroupCard(
                        group = group,
                        onToggleProject = onToggleProject,
                        onEditTask = onEditTask,
                        onDeleteTask = onDeleteTask,
                        onMoreInfo = onMoreInfo,
                        onObservations = onObservations
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectTaskGroupCard(
    group: GestorProjectTaskGroup,
    onToggleProject: (Int) -> Unit,
    onEditTask: (GestorTaskListItem) -> Unit,
    onDeleteTask: (GestorTaskListItem) -> Unit,
    onMoreInfo: (GestorTaskListItem) -> Unit,
    onObservations: (GestorTaskListItem) -> Unit
) {
    val language = currentAppSettings().language

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = rememberSoundClick {
                            onToggleProject(group.projectId)
                        }
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.projectName,
                        color = ProjectHubColors.Ink,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = language.t("tasks.groupSummary")
                            .format(
                                group.totalTasks,
                                group.completedTasks,
                                group.inProgressTasks,
                                group.pendingTasks
                            ),
                        color = ProjectHubColors.Muted,
                        fontSize = 12.sp
                    )
                }

                _root_ide_package_.com.example.projecthub.uiscreens.AppExpandIcon(expanded = group.isExpanded)
            }

            if (group.isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                if (group.visibleTasks.isEmpty()) {
                    Text(
                        text = language.t("tasks.noVisible"),
                        color = ProjectHubColors.Muted
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 520.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = group.visibleTasks,
                            key = { it.id }
                        ) { task ->
                            TaskRow(
                                task = task,
                                onEditTask = onEditTask,
                                onDeleteTask = onDeleteTask,
                                onMoreInfo = onMoreInfo,
                                onObservations = onObservations
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: GestorTaskListItem,
    onEditTask: (GestorTaskListItem) -> Unit,
    onDeleteTask: (GestorTaskListItem) -> Unit,
    onMoreInfo: (GestorTaskListItem) -> Unit,
    onObservations: (GestorTaskListItem) -> Unit
) {
    val language = currentAppSettings().language
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ProjectHubColors.SurfaceSoft)
            .padding(12.dp)
    ) {
        Text(
            text = task.title,
            color = ProjectHubColors.Ink,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp
        )
        Text(
            text = task.description,
            color = ProjectHubColors.Muted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatusPill(task.statusLabel)

            _root_ide_package_.com.example.projecthub.uiscreens.AppMoreInfoButton(
                text = language.t("common.moreInfo"),
                onClick = { onMoreInfo(task) },
                compact = true
            )

            Spacer(modifier = Modifier.weight(1f))

            TaskActionIcon(
                painter = painterResource(R.drawable.ic_edit_24),
                contentDescription = language.t("common.edit"),
                color = GestorTasksAccent
            ) {
                onEditTask(task)
            }

            TaskActionIcon(
                painter = painterResource(R.drawable.ic_delete_24),
                contentDescription = language.t("common.delete"),
                color = GestorTasksRed
            ) {
                onDeleteTask(task)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${language.t("common.start")}: ${task.startDate}   " +
                    "${language.t("common.deadline")}: ${task.dueDate}",
            color = ProjectHubColors.Slate,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = language.t("tasks.assignedTo"),
            color = ProjectHubColors.Ink,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        if (task.assignees.isEmpty()) {
            Text(
                text = language.t("tasks.noAssignees"),
                color = ProjectHubColors.Muted,
                fontSize = 12.sp
            )
        } else {
            Text(
                text = task.assignees.joinToString { it.name },
                color = ProjectHubColors.Muted,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        _root_ide_package_.com.example.projecthub.uiscreens.AppObservationsButton(
            text = language.t("user.tasks.observations"),
            onClick = { onObservations(task) },
            compact = true
        )
    }
}

