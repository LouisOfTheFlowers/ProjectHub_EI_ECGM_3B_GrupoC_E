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

                _root_ide_package_.com.example.projecthub.uiscreens.AppFilledActionButton(
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
        DeleteGestorTaskDialog(
            task = task,
            onDismiss = { taskToDelete = null },
            onConfirm = {
                viewModel.deleteTask(gestorId, task.id)
                taskToDelete = null
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
