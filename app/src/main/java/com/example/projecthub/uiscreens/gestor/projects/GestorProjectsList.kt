package com.example.projecthub.uiscreens.gestor.projects

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.components.AppDropdownField
import com.example.projecthub.uiscreens.components.AppExpandIcon
import com.example.projecthub.uiscreens.components.AppMoreInfoButton
import com.example.projecthub.viewmodel.gestor.GestorProjectListItem
import com.example.projecthub.viewmodel.gestor.GestorProjectsState

@Composable
internal fun GestorProjectFilters(
    state: GestorProjectsState,
    onSearchChange: (String) -> Unit,
    onStatusChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(currentAppSettings().language.t("projects.search"))
            },
            shape = RoundedCornerShape(8.dp),
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
}

@Composable
private fun StatusDropdown(
    selected: String,
    onOptionSelected: (String) -> Unit
) {
    val language = currentAppSettings().language
    AppDropdownField(
        selected = selected,
        options = GestorProjectStatuses,
        label = { (it ?: selected).toGestorProjectStatusFilterLabel(language) },
        onOptionSelected = onOptionSelected
    )
}

private fun String.toGestorProjectStatusFilterLabel(language: com.example.projecthub.settings.AppLanguage): String {
    return when (this) {
        "Todos os Status" -> language.t("filters.allStatuses")
        "Em progresso" -> language.t("filters.projects.inProgress")
        "Pendentes" -> language.t("filters.projects.pending")
        "Concluídos" -> language.t("filters.projects.completed")
        else -> this
    }
}

@Composable
internal fun GestorProjectList(
    state: GestorProjectsState,
    onToggleProject: (Int) -> Unit,
    onAssociateUser: (GestorProjectListItem) -> Unit,
    onCompleteProject: (GestorProjectListItem) -> Unit,
    onMoreInfo: (GestorProjectListItem) -> Unit
) {
    when {
        state.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GestorProjectsAccent)
            }
        }

        state.errorMessage != null -> {
            Text(
                text = state.errorMessage.orEmpty(),
                color = GestorProjectsRed,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        state.visibleProjects.isEmpty() -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Text(
                    text = currentAppSettings().language.t("manager.projects.noFiltered"),
                    color = ProjectHubColors.Muted,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(18.dp)
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 760.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(
                    items = state.visibleProjects,
                    key = { it.id }
                ) { project ->
                    GestorProjectCard(
                        project = project,
                        onToggleProject = onToggleProject,
                        onAssociateUser = onAssociateUser,
                        onCompleteProject = onCompleteProject,
                        onMoreInfo = onMoreInfo
                    )
                }
            }
        }
    }
}

@Composable
private fun GestorProjectCard(
    project: GestorProjectListItem,
    onToggleProject: (Int) -> Unit,
    onAssociateUser: (GestorProjectListItem) -> Unit,
    onCompleteProject: (GestorProjectListItem) -> Unit,
    onMoreInfo: (GestorProjectListItem) -> Unit
) {
    val toggleClick = rememberSoundClick {
        onToggleProject(project.id)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = toggleClick)
                ) {
                    Text(
                        text = project.name,
                        color = ProjectHubColors.Ink,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = project.description,
                        color = ProjectHubColors.SlateMuted,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppMoreInfoButton(
                            text = currentAppSettings().language.t("common.moreInfo"),
                            onClick = { onMoreInfo(project) },
                            compact = true
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        AppExpandIcon(
                            expanded = project.isExpanded,
                            modifier = Modifier.clickable(onClick = toggleClick)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(project.statusLabel)

                        if (!project.isCompleted) {
                            Spacer(modifier = Modifier.width(8.dp))

                            CompleteIconButton(
                                onClick = {
                                    onCompleteProject(project)
                                }
                            )
                        }
                    }
                }
            }

            if (project.isExpanded) {
                Spacer(modifier = Modifier.height(18.dp))

                ProjectDetails(project = project)

                Spacer(modifier = Modifier.height(16.dp))

                ProjectMembers(
                    project = project,
                    onAssociateUser = onAssociateUser
                )
            }
        }
    }
}
