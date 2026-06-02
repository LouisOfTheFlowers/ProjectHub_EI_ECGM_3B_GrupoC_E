package com.example.projecthub.uiscreens.admin

import com.example.projecthub.uiscreens.*

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.R
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.viewmodel.AdminProjectListItem
import com.example.projecthub.viewmodel.AdminProjectsState

@Composable
internal fun ProjectsPage(
    state: AdminProjectsState,
    onAddProject: () -> Unit,
    onSearchChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onCoordinatorChange: (String) -> Unit,
    onToggleProject: (Int) -> Unit,
    onMoreInfo: (AdminProjectListItem) -> Unit,
    onDeleteProject: (AdminProjectListItem) -> Unit,
    onEditProject: (AdminProjectListItem) -> Unit
) {
    val language = currentAppSettings().language
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.t("projects.title"),
                    color = ProjectHubColors.Ink,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
                Text(
                    text = language.t("projects.list"),
                    color = ProjectHubColors.Muted,
                    fontSize = 14.sp
                )
            }

            AppFilledActionButton(
                text = language.t("projects.add"),
                onClick = onAddProject,
                containerColor = ProjectsAccent
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        ProjectStats(state = state)
        Spacer(modifier = Modifier.height(16.dp))

        ProjectFilters(
            state = state,
            onSearchChange = onSearchChange,
            onStatusChange = onStatusChange,
            onCoordinatorChange = onCoordinatorChange
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = language.t("projects.list"),
            color = ProjectHubColors.Ink,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ProjectsAccent)
                }
            }

            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage.orEmpty(),
                    color = ProjectsRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            state.visibleProjects.isEmpty() -> {
                Text(
                    text = language.t("projects.notFound"),
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
                        items = state.visibleProjects,
                        key = { it.id }
                    ) { project ->
                        ProjectListCard(
                            project = project,
                            onToggle = { onToggleProject(project.id) },
                            onMoreInfo = { onMoreInfo(project) },
                            onDelete = { onDeleteProject(project) },
                            onEdit = { onEditProject(project) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectStats(state: AdminProjectsState) {
    val language = currentAppSettings().language
    ProjectStatCard(
        title = language.t("common.completed"),
        value = state.completedCount.toString(),
        accent = ProjectsAccent,
        icon = ProjectStatIcon.Completed
    )
    Spacer(modifier = Modifier.height(10.dp))
    ProjectStatCard(
        title = language.t("common.inProgress"),
        value = state.inProgressCount.toString(),
        accent = ProjectsGreen,
        icon = ProjectStatIcon.Trend
    )
    Spacer(modifier = Modifier.height(10.dp))
    ProjectStatCard(
        title = language.t("common.delayed"),
        value = state.delayedCount.toString(),
        accent = ProjectsRed,
        icon = ProjectStatIcon.Clock
    )
}

@Composable
private fun ProjectStatCard(
    title: String,
    value: String,
    accent: Color,
    icon: ProjectStatIcon
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                ProjectStatIcon(icon = icon, color = accent)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = ProjectHubColors.Muted, fontSize = 14.sp)
                Text(
                    text = value,
                    color = ProjectHubColors.Ink,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                )
            }
        }
    }
}

@Composable
private fun ProjectFilters(
    state: AdminProjectsState,
    onSearchChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onCoordinatorChange: (String) -> Unit
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
                placeholder = { Text(language.t("projects.search")) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterDropdown(
                    label = state.selectedStatus,
                    options = listOf("Todos", "ConcluÃ­dos", "Em progresso", "Atrasados"),
                    displayLabel = { it.toProjectStatusFilterLabel(language) },
                    modifier = Modifier.weight(1f),
                    onOptionSelected = onStatusChange
                )
                FilterDropdown(
                    label = state.selectedCoordinator,
                    options = listOf("Todos") + state.coordinators,
                    displayLabel = { if (it == "Todos") language.t("filters.projects.all") else it },
                    modifier = Modifier.weight(1f),
                    onOptionSelected = onCoordinatorChange
                )
            }
        }
    }
}

@Composable
private fun FilterDropdown(
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    displayLabel: (String) -> String = { it },
    onOptionSelected: (String) -> Unit
) {
    AppDropdownField(
        selected = label,
        options = options,
        label = { displayLabel(it ?: label) },
        onOptionSelected = onOptionSelected,
        modifier = modifier
    )
}

private fun String.toProjectStatusFilterLabel(language: com.example.projecthub.settings.AppLanguage): String {
    return when (this) {
        "Todos" -> language.t("filters.projects.all")
        "ConcluÃ­dos" -> language.t("filters.projects.completed")
        "Em progresso" -> language.t("filters.projects.inProgress")
        "Atrasados" -> language.t("filters.projects.delayed")
        else -> this
    }
}

@Composable
private fun ProjectListCard(
    project: AdminProjectListItem,
    onToggle: () -> Unit,
    onMoreInfo: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val language = currentAppSettings().language
    val statusColor = when {
        project.isCompleted -> ProjectsAccent
        project.isDelayed -> ProjectsRed
        else -> ProjectsGreen
    }

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
                    .clickable(onClick = rememberSoundClick(onToggle)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        color = ProjectHubColors.Ink,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = project.description,
                        color = ProjectHubColors.Muted,
                        fontSize = 13.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = project.statusLabel,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(statusColor.copy(alpha = 0.14f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppExpandIcon(expanded = project.isExpanded)
                }
            }

            if (project.isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                ProjectInfoRow(language.t("addProject.manager"), project.coordinator)
                ProjectInfoRow(language.t("manager.projects.participants"), project.memberCount.toString())
                ProjectInfoRow(language.t("common.start"), project.startDate.toAdminProjectDisplayDate(currentAppSettings().dateFormat.pattern))
                ProjectInfoRow(language.t("common.deadline"), project.dueDate.toAdminProjectDisplayDate(currentAppSettings().dateFormat.pattern))

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppMoreInfoButton(
                        text = language.t("common.moreInfo"),
                        onClick = onMoreInfo,
                        modifier = Modifier.weight(1f),
                        compact = true
                    )
                    AppActionIconButton(
                        painter = painterResource(R.drawable.ic_edit_24),
                        contentDescription = language.t("common.edit"),
                        color = ProjectsAccent,
                        onClick = onEdit
                    )
                    AppActionIconButton(
                        painter = painterResource(R.drawable.ic_delete_24),
                        contentDescription = language.t("common.delete"),
                        color = ProjectsRed,
                        onClick = onDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectStatIcon(
    icon: ProjectStatIcon,
    color: Color
) {
    val iconRes = when (icon) {
        ProjectStatIcon.Completed -> R.drawable.ic_check_circle_24
        ProjectStatIcon.Trend -> R.drawable.ic_trending_up_24
        ProjectStatIcon.Clock -> R.drawable.ic_schedule_24
    }

    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}
