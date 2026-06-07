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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.uiscreens.components.AppExpandIcon
import com.example.projecthub.viewmodel.admin.AdminProjectTaskGroup
import com.example.projecthub.viewmodel.admin.AdminTaskListItem
import com.example.projecthub.viewmodel.admin.AdminTaskStatusFilter
import com.example.projecthub.viewmodel.admin.AdminTasksState
import com.example.projecthub.viewmodel.admin.AdminTasksViewModel
import com.example.projecthub.ui.theme.ProjectHubColors

@Composable
internal fun TaskProjectList(
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.projectName,
                        color = ProjectHubColors.Ink,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
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

                Spacer(modifier = Modifier.width(8.dp))

                AppExpandIcon(expanded = group.isExpanded)
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

