package com.example.projecthub.uiscreens.gestor.projects

import androidx.compose.foundation.background
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
import coil.compose.AsyncImage
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.components.AppBackButton
import com.example.projecthub.uiscreens.components.AppMessageCard
import com.example.projecthub.uiscreens.components.AppObservationCard
import com.example.projecthub.uiscreens.components.AppObservationUiModel
import com.example.projecthub.uiscreens.components.AppObservationsButton
import com.example.projecthub.uiscreens.components.AppStatusChip
import com.example.projecthub.viewmodel.gestor.GestorProjectInfoObservation
import com.example.projecthub.viewmodel.gestor.GestorProjectInfoState
import com.example.projecthub.viewmodel.gestor.GestorProjectInfoTask

@Composable
internal fun GestorProjectInfoPage(
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
    val language = currentAppSettings().language

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = language.t("manager.projects.participants"),
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (state.participants.isEmpty()) {
                Text(
                    text = language.t("manager.projects.noParticipants"),
                    color = ProjectHubColors.Muted,
                    fontSize = 14.sp
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.participants,
                        key = { it.id }
                    ) { participant ->
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
    val language = currentAppSettings().language
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
        text = language.t("reports.tasks"),
        color = ProjectHubColors.Ink,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp
    )

    Spacer(modifier = Modifier.height(10.dp))

    if (tasks.isEmpty()) {
        InfoMessageCard(
            title = language.t("manager.projects.noTasksTitle"),
            detail = language.t("manager.projects.noTasksDetail")
        )
    } else {
        LazyColumn(
            modifier = Modifier.heightIn(max = 680.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = tasks,
                key = { it.id }
            ) { task ->
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
    val language = currentAppSettings().language

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
                    label = language.t("common.start"),
                    value = task.startDate,
                    modifier = Modifier.weight(1f)
                )

                DetailItem(
                    label = language.t("common.deadline"),
                    value = task.dueDate,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${language.t("tasks.responsibles")}: ${
                    task.assignees
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString()
                        ?: language.t("tasks.noAssignees")
                }",
                color = ProjectHubColors.Slate,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppObservationsButton(
                text = language.t("user.tasks.observations"),
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
    val language = currentAppSettings().language
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
            text = language.t("manager.projects.backProject"),
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
            text = language.t("tasks.observationsTitle"),
            color = ProjectHubColors.Muted,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (task.observations.isEmpty()) {
            InfoMessageCard(
                title = language.t("user.tasks.noObservationsTitle"),
                detail = language.t("user.tasks.noObservationsDetail")
            )
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = task.observations,
                    key = { it.id ?: it.text.hashCode() }
                ) { observation ->
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

                DetailItem(
                    label = currentAppSettings().language.t("common.hours"),
                    value = observation.spentHours?.let { "$it h" } ?: "-",
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
            LazyColumn(
                modifier = Modifier.heightIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(observation.photoUrls) { photoUrl ->
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
            spentHours = observation.spentHours,
            photoUrls = observation.photoUrls
        ),
        onClick = onClick
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
    AppMessageCard(
        title = title,
        detail = detail
    )
}
