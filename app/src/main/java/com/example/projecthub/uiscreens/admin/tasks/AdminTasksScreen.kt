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
