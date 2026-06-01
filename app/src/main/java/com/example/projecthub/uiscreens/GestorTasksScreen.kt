package com.example.projecthub.uiscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import com.example.projecthub.viewmodel.GestorProjectTaskGroup
import com.example.projecthub.viewmodel.GestorTaskInfoObservation
import com.example.projecthub.viewmodel.GestorTaskInfoState
import com.example.projecthub.viewmodel.GestorTaskListItem
import com.example.projecthub.viewmodel.GestorTaskProjectOption
import com.example.projecthub.viewmodel.GestorTaskStatusFilter
import com.example.projecthub.viewmodel.GestorTaskUserOption
import com.example.projecthub.viewmodel.GestorTasksState
import com.example.projecthub.viewmodel.GestorTasksViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val GestorTasksAccent = AuthAccent
private val GestorTasksGreen = ProjectHubColors.Success
private val GestorTasksOrange = ProjectHubColors.Warning
private val GestorTasksRed = ProjectHubColors.Danger
private val GestorTasksBlue = ProjectHubColors.Info

@Composable
fun GestorTasksScreen(
    gestorId: Int?,
    viewModel: GestorTasksViewModel = viewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val language = currentAppSettings().language

    var isCreatingTask by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<GestorTaskListItem?>(null) }
    var taskToEdit by remember { mutableStateOf<GestorTaskListItem?>(null) }
    var taskToView by remember { mutableStateOf<GestorTaskListItem?>(null) }
    var taskToViewObservations by remember { mutableStateOf<GestorTaskListItem?>(null) }

    LaunchedEffect(gestorId) {
        viewModel.loadTasks(gestorId)
    }

    taskToView?.let { task ->
        LaunchedEffect(task.id) {
            viewModel.loadTaskInfo(task)
        }

        GestorTaskInfoPage(
            state = state.detailState,
            onBack = {
                viewModel.clearTaskInfo()
                taskToView = null
            }
        )
        return
    }

    taskToViewObservations?.let { task ->
        LaunchedEffect(task.id) {
            viewModel.loadTaskInfo(task)
        }

        GestorTaskObservationsPage(
            state = state.detailState,
            onBack = {
                viewModel.clearTaskInfo()
                taskToViewObservations = null
            }
        )
        return
    }

    if (isCreatingTask) {
        GestorAddTaskScreen(
            state = state,
            onBack = {
                viewModel.clearCreateError()
                isCreatingTask = false
            },
            onCreate = { title, description, projectId, startDate, endDate, userIds ->
                viewModel.createTask(
                    gestorId = gestorId,
                    title = title,
                    description = description,
                    projectId = projectId,
                    startDateText = startDate,
                    endDateText = endDate,
                    userIds = userIds,
                    onSuccess = { isCreatingTask = false }
                )
            }
        )
    } else {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = language.t("tasks.managementTitle"),
                        color = ProjectHubColors.Ink,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                    Text(
                        text = language.t("tasks.managementSubtitle"),
                        color = ProjectHubColors.Muted,
                        fontSize = 14.sp
                    )
                }

                AppFilledActionButton(
                    text = language.t("tasks.add"),
                    onClick = {
                        viewModel.clearCreateError()
                        isCreatingTask = true
                    },
                    containerColor = GestorTasksAccent
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TaskStats(state = state)

            Spacer(modifier = Modifier.height(14.dp))

            TaskFilters(
                state = state,
                onSearchChange = viewModel::updateSearchQuery,
                onStatusChange = viewModel::updateStatusFilter
            )

            Spacer(modifier = Modifier.height(16.dp))

            TaskProjectList(
                state = state,
                onToggleProject = viewModel::toggleProject,
                onEditTask = {
                    viewModel.clearCreateError()
                    taskToEdit = it
                },
                onDeleteTask = { taskToDelete = it },
                onMoreInfo = { taskToView = it },
                onObservations = { taskToViewObservations = it }
            )
        }
    }

    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text(language.t("tasks.deleteTitle")) },
            text = { Text(language.t("tasks.deleteQuestion").format(task.title)) },
            confirmButton = {
                AppDialogConfirmButton(
                    text = language.t("common.delete"),
                    onClick = {
                        viewModel.deleteTask(gestorId, task.id)
                        taskToDelete = null
                    }
                )
            },
            dismissButton = {
                AppDialogCancelButton(
                    text = language.t("common.cancel"),
                    onClick = { taskToDelete = null }
                )
            }
        )
    }

    taskToEdit?.let { task ->
        EditGestorTaskDialog(
            task = task,
            users = state.users.filter { task.projectId in it.projectIds },
            isSaving = state.isCreating,
            errorMessage = state.createErrorMessage,
            onDismiss = {
                viewModel.clearCreateError()
                taskToEdit = null
            },
            onSave = { title, description, status, startDate, endDate, userIds ->
                viewModel.updateTask(
                    gestorId = gestorId,
                    task = task,
                    title = title,
                    description = description,
                    startDateText = startDate,
                    endDateText = endDate,
                    userIds = userIds,
                    onSuccess = { taskToEdit = null }
                )
            }
        )
    }
}

@Composable
private fun TaskStats(state: GestorTasksState) {
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
    AppCompactStatCard(title = title, value = value, accent = accent, modifier = modifier, heightDp = 72)
}

@Composable
private fun TaskFilters(
    state: GestorTasksState,
    onSearchChange: (String) -> Unit,
    onStatusChange: (GestorTaskStatusFilter) -> Unit
) {
    val language = currentAppSettings().language

    AppSearchField(
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
    AppDropdownField(
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
private fun TaskProjectList(
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

                AppExpandIcon(expanded = group.isExpanded)
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

            AppMoreInfoButton(
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

        AppObservationsButton(
            text = language.t("user.tasks.observations"),
            onClick = { onObservations(task) },
            compact = true
        )
    }
}

@Composable
private fun GestorTaskInfoPage(
    state: GestorTaskInfoState,
    onBack: () -> Unit
) {
    val language = currentAppSettings().language

    Column {
        AppBackButton(
            text = language.t("user.tasks.back"),
            onClick = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GestorTasksAccent)
            }

            state.errorMessage != null -> TaskInfoMessageCard(
                title = language.t("tasks.details"),
                detail = state.errorMessage
            )

            state.task == null -> TaskInfoMessageCard(
                title = language.t("tasks.notFound"),
                detail = language.t("tasks.noMatching")
            )

            else -> state.task?.let { task ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            color = ProjectHubColors.Ink,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = language.t("tasks.detailsSubtitle"),
                            color = ProjectHubColors.Muted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    StatusPill(task.statusLabel)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(task.description, color = ProjectHubColors.Muted, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TaskInfoMeta(language.t("common.start"), task.startDate.toInputDateText(), Modifier.weight(1f))
                            TaskInfoMeta(language.t("common.deadline"), task.dueDate.toInputDateText(), Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        TaskInfoMeta(
                            label = language.t("tasks.assignedTo"),
                            value = task.assignees.takeIf { it.isNotEmpty() }
                                ?.joinToString { it.name }
                                ?: language.t("tasks.noAssignees"),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TaskInfoMeta(
                            label = language.t("tasks.records"),
                            value = state.recordsCount.toString(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TaskObservationsSection(
                    observations = state.observations
                )
            }
        }
    }
}

@Composable
private fun GestorTaskObservationsPage(
    state: GestorTaskInfoState,
    onBack: () -> Unit
) {
    val language = currentAppSettings().language

    Column {
        AppBackButton(
            text = language.t("user.tasks.back"),
            onClick = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GestorTasksAccent)
            }

            state.errorMessage != null -> TaskInfoMessageCard(
                title = language.t("user.tasks.observations"),
                detail = state.errorMessage
            )

            state.task == null -> TaskInfoMessageCard(
                title = language.t("tasks.notFound"),
                detail = language.t("tasks.noMatching")
            )

            else -> {
                Text(
                    text = state.task.title,
                    color = ProjectHubColors.Ink,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = language.t("user.tasks.observations"),
                    color = ProjectHubColors.Muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(14.dp))
                TaskObservationsSection(observations = state.observations)
            }
        }
    }
}

@Composable
private fun TaskObservationsSection(
    observations: List<GestorTaskInfoObservation>
) {
    val language = currentAppSettings().language

    Text(
        text = language.t("user.tasks.observations"),
        color = ProjectHubColors.Ink,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp
    )
    Spacer(modifier = Modifier.height(10.dp))

    if (observations.isEmpty()) {
        TaskInfoMessageCard(
            title = language.t("user.tasks.noObservationsTitle"),
            detail = language.t("user.tasks.noObservationsDetail")
        )
    } else {
        LazyColumn(
            modifier = Modifier.heightIn(max = 620.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = observations,
                key = { it.id ?: it.text.hashCode() }
            ) { observation ->
                TaskObservationRow(observation = observation)
            }
        }
    }
}

@Composable
private fun TaskInfoMeta(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, color = ProjectHubColors.Muted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(value, color = ProjectHubColors.Slate, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun TaskObservationRow(observation: GestorTaskInfoObservation) {
    AppObservationCard(
        observation = AppObservationUiModel(
            text = observation.text,
            userName = observation.userName,
            date = observation.date,
            completionPercent = observation.completionPercent,
            spentHours = observation.spentHours
        ),
        showPhotoPreview = false
    )
}

@Composable
private fun TaskInfoMessageCard(
    title: String,
    detail: String
) {
    AppMessageCard(title = title, detail = detail)
}

@Composable
private fun TaskActionIcon(
    painter: Painter,
    contentDescription: String?,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .clickable(onClick = rememberSoundClick(onClick)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun StatusPill(status: String) {
    AppStatusChip(text = status)
}

@Composable
private fun EditGestorTaskDialog(
    task: GestorTaskListItem,
    users: List<GestorTaskUserOption>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, Set<Int>) -> Unit
) {
    val language = currentAppSettings().language
    var title by remember(task.id) { mutableStateOf(task.title) }
    var description by remember(task.id) { mutableStateOf(task.description) }
    var startDate by remember(task.id) { mutableStateOf(task.startDate.toInputDateText()) }
    var endDate by remember(task.id) { mutableStateOf(task.dueDate.toInputDateText()) }
    var selectedUserIds by remember(task.id) {
        mutableStateOf(task.assignees.map { it.id }.toSet())
    }
    var activeDateField by remember { mutableStateOf<EditTaskDateField?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(language.t("tasks.editTitle"))
        },
        text = {
            Column(modifier = Modifier.responsiveDialogBody()) {
                AppTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = language.t("tasks.titleField"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = language.t("tasks.descriptionField")
                )

                Spacer(modifier = Modifier.height(8.dp))

                EditTaskDatePickerField(
                    value = startDate,
                    label = language.t("common.start"),
                    onClick = {
                        activeDateField = EditTaskDateField.Start
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                EditTaskDatePickerField(
                    value = endDate,
                    label = language.t("common.deadline"),
                    onClick = {
                        activeDateField = EditTaskDateField.End
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = language.t("common.users"),
                    color = ProjectHubColors.Ink,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (users.isEmpty()) {
                    Text(
                        text = language.t("common.noProjectUsers"),
                        color = ProjectHubColors.Muted,
                        fontSize = 13.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(
                            items = users,
                            key = { it.id }
                        ) { user ->
                            UserCheckRow(
                                user = user,
                                checked = user.id in selectedUserIds,
                                onToggle = {
                                    selectedUserIds = if (user.id in selectedUserIds) {
                                        selectedUserIds - user.id
                                    } else {
                                        selectedUserIds + user.id
                                    }
                                }
                            )
                        }
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = it,
                        color = GestorTasksRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            AppDialogConfirmButton(
                text = if (isSaving) language.t("common.saving") else language.t("common.save"),
                enabled = !isSaving,
                onClick = {
                    onSave(
                        title,
                        description,
                        task.rawStatus,
                        startDate,
                        endDate,
                        selectedUserIds
                    )
                }
            )
        },
        dismissButton = {
            AppDialogCancelButton(
                text = language.t("common.cancel"),
                onClick = onDismiss
            )
        }
    )

    if (activeDateField != null) {
        EditTaskDatePickerDialog(
            onDismiss = {
                activeDateField = null
            },
            onDateSelected = { formattedDate ->
                when (activeDateField) {
                    EditTaskDateField.Start -> startDate = formattedDate
                    EditTaskDateField.End -> endDate = formattedDate
                    null -> Unit
                }

                activeDateField = null
            }
        )
    }
}

@Composable
private fun EditTaskDatePickerField(
    value: String,
    label: String,
    onClick: () -> Unit
) {
    val language = currentAppSettings().language

    Column {
        Text(
            text = label,
            color = ProjectHubColors.Ink,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, ProjectHubColors.Border, RoundedCornerShape(8.dp))
                .clickable(onClick = rememberSoundClick(onClick))
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value.ifBlank { "dd/mm/aaaa" },
                color = if (value.isBlank()) ProjectHubColors.Muted else ProjectHubColors.Ink,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = language.t("settings.dateFormat"),
                color = ProjectHubColors.Muted,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun EditTaskDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val language = currentAppSettings().language
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis

                    if (selectedMillis != null) {
                        onDateSelected(selectedMillis.toEditTaskDateText())
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(language.t("common.ok"))
            }
        },
        dismissButton = {
            TextButton(onClick = rememberSoundClick(onDismiss)) {
                Text(language.t("common.cancel"))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private enum class EditTaskDateField {
    Start,
    End
}

private fun Long.toEditTaskDateText(): String {
    val date = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

private fun String.toInputDateText(): String {
    if (this == "-") return ""

    val parts = split("-")

    return if (parts.size == 3) {
        "${parts[2]}/${parts[1]}/${parts[0]}"
    } else {
        this
    }
}

@Composable
private fun AddGestorTaskDialog(
    state: GestorTasksState,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onCreate: (String, String, Int?, String, String, Set<Int>) -> Unit
) {
    val language = currentAppSettings().language
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedProjectId by remember(state.projects) {
        mutableStateOf(state.projects.firstOrNull()?.id)
    }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var selectedUserIds by remember { mutableStateOf(emptySet<Int>()) }

    val availableUsers = state.users.filter {
        selectedProjectId in it.projectIds
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(language.t("tasks.newTitle"))
        },
        text = {
            Column(modifier = Modifier.responsiveDialogBody()) {
                AppTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = language.t("tasks.titleField"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = language.t("tasks.descriptionField")
                )

                Spacer(modifier = Modifier.height(8.dp))

                ProjectDropdown(
                    projects = state.projects,
                    selectedProjectId = selectedProjectId,
                    onProjectSelected = {
                        selectedProjectId = it
                        selectedUserIds = emptySet()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                AppTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = "${language.t("common.start")} dd/mm/aaaa",
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                AppTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = "${language.t("common.deadline")} dd/mm/aaaa",
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = language.t("common.users"),
                    color = ProjectHubColors.Ink,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (availableUsers.isEmpty()) {
                    Text(
                        text = language.t("common.noProjectUsers"),
                        color = ProjectHubColors.Muted,
                        fontSize = 13.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(
                            items = availableUsers,
                            key = { it.id }
                        ) { user ->
                            UserCheckRow(
                                user = user,
                                checked = user.id in selectedUserIds,
                                onToggle = {
                                    selectedUserIds = if (user.id in selectedUserIds) {
                                        selectedUserIds - user.id
                                    } else {
                                        selectedUserIds + user.id
                                    }
                                }
                            )
                        }
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = it,
                        color = GestorTasksRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            AppDialogConfirmButton(
                text = if (isSaving) {
                    language.t("common.creating")
                } else {
                    language.t("common.create")
                },
                enabled = !isSaving,
                onClick = {
                    onCreate(
                        title,
                        description,
                        selectedProjectId,
                        startDate,
                        endDate,
                        selectedUserIds
                    )
                }
            )
        },
        dismissButton = {
            AppDialogCancelButton(
                text = language.t("common.cancel"),
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun ProjectDropdown(
    projects: List<GestorTaskProjectOption>,
    selectedProjectId: Int?,
    onProjectSelected: (Int) -> Unit
) {
    val language = currentAppSettings().language
    var expanded by remember { mutableStateOf(false) }
    val selected = projects.firstOrNull { it.id == selectedProjectId }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, ProjectHubColors.Border, RoundedCornerShape(8.dp))
                .clickable(onClick = rememberSoundClick { expanded = true })
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selected?.name ?: language.t("addTask.projectPlaceholder"),
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.SemiBold
            )

            AppExpandIcon(expanded = expanded)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ProjectHubColors.LightSurface)
        ) {
            projects.forEach { project ->
                DropdownMenuItem(
                    text = {
                        Text(project.name, color = ProjectHubColors.Ink)
                    },
                    onClick = {
                        expanded = false
                        onProjectSelected(project.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun UserCheckRow(
    user: GestorTaskUserOption,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ProjectHubColors.SurfaceSoft)
            .clickable(onClick = rememberSoundClick(onToggle))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(22.dp)
                .clip(CircleShape)
                .background(if (checked) GestorTasksAccent else Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (checked) GestorTasksAccent else ProjectHubColors.BorderSoft,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = user.name,
            color = ProjectHubColors.Ink,
            fontWeight = FontWeight.SemiBold
        )
    }
}
