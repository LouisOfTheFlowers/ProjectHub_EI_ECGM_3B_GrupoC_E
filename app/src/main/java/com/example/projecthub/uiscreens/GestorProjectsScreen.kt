package com.example.projecthub.uiscreens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.R
import com.example.projecthub.viewmodel.GestorProjectInfoObservation
import com.example.projecthub.viewmodel.GestorProjectInfoState
import com.example.projecthub.viewmodel.GestorProjectInfoTask
import com.example.projecthub.viewmodel.GestorProjectListItem
import com.example.projecthub.viewmodel.GestorProjectsState
import com.example.projecthub.viewmodel.GestorProjectsViewModel
import com.example.projecthub.viewmodel.GestorUserOption

private val GestorProjectsAccent = AuthAccent
private val GestorProjectsBlue = ProjectHubColors.Info
private val GestorProjectsGreen = ProjectHubColors.Success
private val GestorProjectsRed = ProjectHubColors.Danger

private val GestorProjectStatuses = listOf(
    "Todos os Status",
    "Em progresso",
    "Pendentes",
    "Concluídos"
)

@Composable
fun GestorProjectsScreen(
    gestorId: Int?,
    viewModel: GestorProjectsViewModel = viewModel()
) {
    val state = viewModel.state
    val language = currentAppSettings().language

    var projectToAssociate by remember { mutableStateOf<GestorProjectListItem?>(null) }
    var projectToComplete by remember { mutableStateOf<GestorProjectListItem?>(null) }
    var projectToView by remember { mutableStateOf<GestorProjectListItem?>(null) }

    LaunchedEffect(gestorId) {
        viewModel.loadProjects(gestorId)
    }

    LaunchedEffect(state.actionMessage) {
        if (state.actionMessage != null && projectToComplete != null) {
            projectToComplete = null
        }
    }

    projectToView?.let { project ->
        LaunchedEffect(project.id) {
            viewModel.loadProjectInfo(project)
        }

        ProjectInfoPage(
            state = state.detailState,
            onBack = {
                viewModel.clearProjectInfo()
                projectToView = null
            }
        )
        return
    }

    Column {
        Text(
            text = language.t("manager.projects.title"),
            color = ProjectHubColors.Ink,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )

        Text(
            text = language.t("manager.projects.subtitle"),
            color = ProjectHubColors.Muted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        ProjectFilters(
            state = state,
            onSearchChange = viewModel::updateSearchQuery,
            onStatusChange = viewModel::updateStatusFilter
        )

        Spacer(modifier = Modifier.height(18.dp))

        ProjectList(
            state = state,
            onToggleProject = viewModel::toggleProject,
            onAssociateUser = { project ->
                viewModel.clearMessages()
                projectToAssociate = project
            },
            onCompleteProject = { project ->
                viewModel.clearMessages()
                projectToComplete = project
            },
            onMoreInfo = { project ->
                viewModel.clearMessages()
                projectToView = project
            }
        )
    }

    projectToAssociate?.let { project ->
        AssociateUserDialog(
            project = project,
            users = state.userOptions.filterNot { option ->
                project.members.any { it.id == option.id }
            },
            isSaving = state.isAssociating,
            errorMessage = state.errorMessage,
            onDismiss = {
                viewModel.clearMessages()
                projectToAssociate = null
            },
            onConfirm = { userId ->
                viewModel.associateUserToProject(
                    projetoId = project.id,
                    userId = userId,
                    gestorId = gestorId
                )
                projectToAssociate = null
            }
        )
    }

    projectToComplete?.let { project ->
        CompleteProjectDialog(
            project = project,
            isSaving = state.isAssociating,
            errorMessage = state.errorMessage,
            onDismiss = {
                viewModel.clearMessages()
                projectToComplete = null
            },
            onConfirm = { ratings ->
                viewModel.completeProjectWithRatings(
                    project = project,
                    ratings = ratings,
                    gestorId = gestorId
                )
            }
        )
    }
}

@Composable
private fun ProjectFilters(
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
    AppDropdownField(
        selected = selected,
        options = GestorProjectStatuses,
        label = { it ?: selected },
        onOptionSelected = onOptionSelected
    )
}

@Composable
private fun ProjectList(
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
                text = state.errorMessage,
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
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                state.visibleProjects.forEach { project ->
                    ProjectCard(
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
private fun ProjectCard(
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

@Composable
private fun ProjectInfoPage(
    state: GestorProjectInfoState,
    onBack: () -> Unit
) {
    Column {
        AppBackButton(
            text = currentAppSettings().language.t("manager.projects.back"),
            onClick = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                InfoMessageCard(
                    title = currentAppSettings().language.t("manager.projects.detailsTitle"),
                    detail = state.errorMessage
                )
            }

            state.project == null -> {
                InfoMessageCard(
                    title = currentAppSettings().language.t("manager.projects.notFoundTitle"),
                    detail = currentAppSettings().language.t("manager.projects.notFoundDetail")
                )
            }

            else -> {
                val project = state.project

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.name,
                            color = ProjectHubColors.Ink,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = currentAppSettings().language.t("manager.projects.detailsSubtitle"),
                            color = ProjectHubColors.Muted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    StatusPill(project.statusLabel)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = project.description,
                            color = ProjectHubColors.SlateMuted,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        ProjectDetails(project = project)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                ProjectInfoParticipants(state = state)

                Spacer(modifier = Modifier.height(14.dp))

                ProjectInfoTasks(tasks = state.tasks)
            }
        }
    }
}

@Composable
private fun ProjectInfoParticipants(
    state: GestorProjectInfoState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = currentAppSettings().language.t("manager.projects.participants"),
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (state.participants.isEmpty()) {
                Text(
                    text = currentAppSettings().language.t("manager.projects.noParticipants"),
                    color = ProjectHubColors.Muted,
                    fontSize = 14.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.participants.forEach { participant ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ProjectHubColors.SurfaceSoft)
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = participant.name,
                                    color = ProjectHubColors.Ink,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                participant.rating?.let { rating ->
                                    StarRatingText(rating = rating)
                                }
                            }

                            Text(
                                text = participant.email,
                                color = ProjectHubColors.Muted,
                                fontSize = 12.sp
                            )

                            participant.comment
                                ?.takeIf { it.isNotBlank() }
                                ?.let { comment ->
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = comment,
                                        color = ProjectHubColors.Slate,
                                        fontSize = 12.sp
                                    )
                                }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectInfoTasks(
    tasks: List<GestorProjectInfoTask>
) {
    var selectedTask by remember {
        mutableStateOf<GestorProjectInfoTask?>(null)
    }

    selectedTask?.let { task ->
        ProjectTaskObservationsPage(
            task = task,
            onBack = {
                selectedTask = null
            }
        )
        return
    }

    Text(
        text = currentAppSettings().language.t("reports.tasks"),
        color = ProjectHubColors.Ink,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp
    )

    Spacer(modifier = Modifier.height(10.dp))

    if (tasks.isEmpty()) {
        InfoMessageCard(
            title = currentAppSettings().language.t("manager.projects.noTasksTitle"),
            detail = currentAppSettings().language.t("manager.projects.noTasksDetail")
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            tasks.forEach { task ->
                ProjectInfoTaskCard(
                    task = task,
                    onOpenObservations = {
                        selectedTask = task
                    }
                )
            }
        }
    }
}

@Composable
private fun ProjectInfoTaskCard(
    task: GestorProjectInfoTask,
    onOpenObservations: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = task.description,
                        color = ProjectHubColors.Muted,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                InfoTaskStatusPill(task.statusLabel)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailItem(
                    label = currentAppSettings().language.t("common.start"),
                    value = task.startDate,
                    modifier = Modifier.weight(1f)
                )

                DetailItem(
                    label = currentAppSettings().language.t("common.deadline"),
                    value = task.dueDate,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${currentAppSettings().language.t("tasks.responsibles")}: ${
                    task.assignees
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString()
                        ?: currentAppSettings().language.t("tasks.noAssignees")
                }",
                color = ProjectHubColors.Slate,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppObservationsButton(
                text = currentAppSettings().language.t("user.tasks.observations"),
                onClick = onOpenObservations,
                modifier = Modifier.fillMaxWidth(),
                count = task.observations.size
            )
        }
    }
}

@Composable
private fun ProjectTaskObservationsPage(
    task: GestorProjectInfoTask,
    onBack: () -> Unit
) {
    var selectedObservation by remember {
        mutableStateOf<GestorProjectInfoObservation?>(null)
    }

    selectedObservation?.let { observation ->
        ProjectObservationDetailPage(
            task = task,
            observation = observation,
            onBack = {
                selectedObservation = null
            }
        )
        return
    }

    Column {
        AppBackButton(
            text = currentAppSettings().language.t("manager.projects.backProject"),
            onClick = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = task.title,
            color = ProjectHubColors.Ink,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = currentAppSettings().language.t("tasks.observationsTitle"),
            color = ProjectHubColors.Muted,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (task.observations.isEmpty()) {
            InfoMessageCard(
                title = currentAppSettings().language.t("user.tasks.noObservationsTitle"),
                detail = currentAppSettings().language.t("user.tasks.noObservationsDetail")
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                task.observations.forEach { observation ->
                    ProjectObservationRow(
                        observation = observation,
                        onClick = {
                            selectedObservation = observation
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectObservationDetailPage(
    task: GestorProjectInfoTask,
    observation: GestorProjectInfoObservation,
    onBack: () -> Unit
) {
    Column {
        AppBackButton(
            text = currentAppSettings().language.t("tasks.backObservations"),
            onClick = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = task.title,
            color = ProjectHubColors.Ink,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = currentAppSettings().language.t("tasks.observationDetail"),
            color = ProjectHubColors.Muted,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = observation.text,
                    color = ProjectHubColors.Ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailItem(
                        label = currentAppSettings().language.t("common.user"),
                        value = observation.userName,
                        modifier = Modifier.weight(1f)
                    )

                    DetailItem(
                        label = currentAppSettings().language.t("common.date"),
                        value = observation.date,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailItem(
                        label = currentAppSettings().language.t("user.tasks.completion"),
                        value = "${observation.completionPercent}%",
                        modifier = Modifier.weight(1f)
                    )

                    DetailItem(
                        label = currentAppSettings().language.t("common.hours"),
                        value = observation.spentHours?.let { "$it h" } ?: "-",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                DetailItem(
                    label = currentAppSettings().language.t("common.location"),
                    value = observation.local,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = currentAppSettings().language.t("common.photos"),
            color = ProjectHubColors.Ink,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (observation.photoUrls.isEmpty()) {
            InfoMessageCard(
                title = currentAppSettings().language.t("common.noPhotos"),
                detail = currentAppSettings().language.t("common.photoEmpty")
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                observation.photoUrls.forEach { photoUrl ->
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = currentAppSettings().language.t("profile.photoDescription"),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ProjectHubColors.SurfaceSoft)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectObservationRow(
    observation: GestorProjectInfoObservation,
    onClick: () -> Unit
) {
    AppObservationCard(
        observation = AppObservationUiModel(
            text = observation.text,
            userName = observation.userName,
            date = observation.date,
            completionPercent = observation.completionPercent,
            spentHours = observation.spentHours,
            photoUrls = observation.photoUrls
        ),
        onClick = onClick,
        showPhotoPreview = false
    )
}

@Composable
private fun InfoTaskStatusPill(
    status: String
) {
    AppStatusChip(text = status)
}

@Composable
private fun InfoMessageCard(
    title: String,
    detail: String
) {
    AppMessageCard(title = title, detail = detail)
}

@Composable
private fun ProjectDetails(
    project: GestorProjectListItem
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailItem(
            label = currentAppSettings().language.t("common.start"),
            value = project.startDate,
            modifier = Modifier.weight(1f)
        )

        DetailItem(
            label = currentAppSettings().language.t("common.deadline"),
            value = project.dueDate,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TaskLegend(
            color = GestorProjectsGreen,
            text = "${currentAppSettings().language.t("common.completed")}: ${project.completedTasks}"
        )

        TaskLegend(
            color = GestorProjectsBlue,
            text = "${currentAppSettings().language.t("common.inProgress")}: ${project.inProgressTasks}"
        )

        TaskLegend(
            color = ProjectHubColors.SidebarMutedText,
            text = "${currentAppSettings().language.t("common.pending")}: ${project.pendingTasks}"
        )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = currentAppSettings().language.t("manager.projects.tasksTotal").format(project.totalTasks),
        color = ProjectHubColors.Muted,
        fontSize = 13.sp
    )
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    AppDetailItem(label = label, value = value, modifier = modifier)
}

@Composable
private fun TaskLegend(
    color: Color,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = text,
            color = ProjectHubColors.Ink,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ProjectMembers(
    project: GestorProjectListItem,
    onAssociateUser: (GestorProjectListItem) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TeamIcon(color = ProjectHubColors.Muted)

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = currentAppSettings().language.t("manager.projects.projectTeam"),
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }

        if (!project.isCompleted) {
            Button(
                onClick = rememberSoundClick {
                    onAssociateUser(project)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GestorProjectsAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = currentAppSettings().language.t("manager.projects.associate"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (project.members.isEmpty()) {
        Text(
            text = currentAppSettings().language.t("manager.projects.noMembers"),
            color = ProjectHubColors.Muted,
            fontSize = 14.sp
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            project.members.forEach { member ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ProjectHubColors.SurfaceSoft)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = member.name,
                            color = ProjectHubColors.Ink,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )

                        if (project.isCompleted && member.rating != null) {
                            StarRatingText(rating = member.rating)
                        }
                    }

                    Text(
                        text = member.email,
                        color = ProjectHubColors.Muted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CompleteIconButton(
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    AppActionIconButton(
        painter = painterResource(R.drawable.ic_check_circle_24),
        contentDescription = currentAppSettings().language.t("common.complete"),
        color = GestorProjectsGreen,
        onClick = onClick,
        enabled = enabled
    )
}

@Composable
private fun CompleteProjectDialog(
    project: GestorProjectListItem,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (Map<Int, Int>) -> Unit
) {
    var ratings by remember(project.id) {
        mutableStateOf(
            project.members.associate { member ->
                member.id to (member.rating ?: 0)
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(currentAppSettings().language.t("manager.projects.completeTitle"))
        },
        text = {
            Column {
                Text(
                    text = currentAppSettings().language.t("manager.projects.rateMembers"),
                    color = ProjectHubColors.Muted,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (project.members.isEmpty()) {
                    Text(
                        text = currentAppSettings().language.t("manager.projects.noMembersToRate"),
                        color = ProjectHubColors.Muted
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        project.members.forEach { member ->
                            Column {
                                Text(
                                    text = member.name,
                                    color = ProjectHubColors.Ink,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                StarSelector(
                                    rating = ratings[member.id] ?: 0,
                                    onRatingChange = { value ->
                                        ratings = ratings + (member.id to value)
                                    }
                                )
                            }
                        }
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = it,
                        color = GestorProjectsRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = rememberSoundClick {
                    onConfirm(ratings)
                }
            ) {
                Text(
                    text = if (isSaving) {
                        currentAppSettings().language.t("common.completing")
                    } else {
                        currentAppSettings().language.t("user.tasks.complete")
                    },
                    color = GestorProjectsGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = rememberSoundClick(onDismiss)) {
                Text(currentAppSettings().language.t("common.cancel"))
            }
        }
    )
}

@Composable
private fun StarSelector(
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        (0..5).forEach { value ->
            Text(
                text = if (value == 0) {
                    "0"
                } else if (value <= rating) {
                    "★"
                } else {
                    "☆"
                },
                color = if (value == 0) {
                    ProjectHubColors.Muted
                } else if (value <= rating) {
                    ProjectHubColors.Rating
                } else {
                    ProjectHubColors.Border
                },
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (value == 0) 15.sp else 26.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(
                        onClick = rememberSoundClick {
                            onRatingChange(value)
                        }
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun StarRatingText(
    rating: Int
) {
    val clamped = rating.coerceIn(0, 5)

    Text(
        text = "${"★".repeat(clamped)}${"☆".repeat(5 - clamped)} $clamped/5",
        color = ProjectHubColors.Rating,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
    )
}

@Composable
private fun StatusPill(
    status: String
) {
    AppStatusChip(text = status)
}

@Composable
private fun AssociateUserDialog(
    project: GestorProjectListItem,
    users: List<GestorUserOption>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (Int?) -> Unit
) {
    var selectedUserId by remember(project.id, users) {
        mutableStateOf(users.firstOrNull()?.id)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(currentAppSettings().language.t("manager.projects.associateUser"))
        },
        text = {
            Column {
                Text(
                    text = project.name,
                    color = ProjectHubColors.Ink,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (users.isEmpty()) {
                    Text(
                        text = currentAppSettings().language.t("manager.projects.noAvailableUsers"),
                        color = ProjectHubColors.Muted
                    )
                } else {
                    UserDropdown(
                        users = users,
                        selectedUserId = selectedUserId,
                        onUserSelected = {
                            selectedUserId = it
                        }
                    )
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = it,
                        color = GestorProjectsRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving && users.isNotEmpty(),
                onClick = rememberSoundClick {
                    onConfirm(selectedUserId)
                }
            ) {
                Text(
                    text = if (isSaving) {
                        currentAppSettings().language.t("common.saving")
                    } else {
                        currentAppSettings().language.t("manager.projects.associate")
                    },
                    color = GestorProjectsAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = rememberSoundClick(onDismiss)) {
                Text(currentAppSettings().language.t("common.cancel"))
            }
        }
    )
}

@Composable
private fun UserDropdown(
    users: List<GestorUserOption>,
    selectedUserId: Int?,
    onUserSelected: (Int) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    val selected = users.firstOrNull {
        it.id == selectedUserId
    }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, ProjectHubColors.Border, RoundedCornerShape(8.dp))
                .clickable(
                    onClick = rememberSoundClick {
                        expanded = true
                    }
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selected?.name ?: currentAppSettings().language.t("common.selectUser"),
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            AppExpandIcon(expanded = expanded)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier.background(ProjectHubColors.LightSurface)
        ) {
            users.forEach { user ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = user.name,
                                color = ProjectHubColors.Ink,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = user.email,
                                color = ProjectHubColors.Muted,
                                fontSize = 12.sp
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onUserSelected(user.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun TeamIcon(
    color: Color
) {
    Icon(
        painter = painterResource(R.drawable.ic_group_24),
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(22.dp)
    )
}


