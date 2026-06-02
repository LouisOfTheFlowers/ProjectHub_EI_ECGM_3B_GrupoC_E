package com.example.projecthub.uiscreens.admin.projects

import com.example.projecthub.uiscreens.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.viewmodel.AdminProjectInfoObservation
import com.example.projecthub.viewmodel.AdminProjectInfoParticipant
import com.example.projecthub.viewmodel.AdminProjectInfoState
import com.example.projecthub.viewmodel.AdminProjectInfoTask
import com.example.projecthub.viewmodel.AdminProjectListItem

@Composable
internal fun AdminProjectDetailPage(
    state: AdminProjectInfoState,
    onBack: () -> Unit
) {
    val language = currentAppSettings().language
    val project = state.project

    Column {
        AppBackButton(
            text = language.t("manager.projects.back"),
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
                CircularProgressIndicator(color = ProjectsAccent)
            }

            state.errorMessage != null -> AdminProjectMessageCard(
                title = language.t("manager.projects.detailsTitle"),
                detail = state.errorMessage
            )

            project == null -> AdminProjectMessageCard(
                title = language.t("manager.projects.notFoundTitle"),
                detail = language.t("manager.projects.notFoundDetail")
            )

            else -> {
                Text(
                    text = project.name,
                    color = ProjectHubColors.Ink,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
                Text(
                    text = language.t("manager.projects.detailsSubtitle"),
                    color = ProjectHubColors.Muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                AdminProjectDetailsCard(project = project)
                Spacer(modifier = Modifier.height(12.dp))
                AdminProjectParticipantsCard(participants = state.participants)
                Spacer(modifier = Modifier.height(12.dp))
                AdminProjectRatingsCard(participants = state.participants)
                Spacer(modifier = Modifier.height(12.dp))
                AdminProjectTasksCard(tasks = state.tasks)
            }
        }
    }
}

@Composable
private fun AdminProjectDetailsCard(project: AdminProjectListItem) {
    val language = currentAppSettings().language
    AdminProjectSectionCard(title = language.t("manager.projects.detailsTitle")) {
        Text(project.description, color = ProjectHubColors.Muted, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))
        ProjectInfoRow(language.t("addProject.manager"), project.coordinator)
        ProjectInfoRow(language.t("common.status"), project.statusLabel)
        ProjectInfoRow(
            language.t("common.start"),
            project.startDate.toAdminProjectDisplayDate(currentAppSettings().dateFormat.pattern)
        )
        ProjectInfoRow(
            language.t("common.deadline"),
            project.dueDate.toAdminProjectDisplayDate(currentAppSettings().dateFormat.pattern)
        )
    }
}

@Composable
private fun AdminProjectParticipantsCard(participants: List<AdminProjectInfoParticipant>) {
    val language = currentAppSettings().language
    AdminProjectSectionCard(title = language.t("manager.projects.participants")) {
        if (participants.isEmpty()) {
            Text(language.t("manager.projects.noParticipants"), color = ProjectHubColors.Muted, fontSize = 14.sp)
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = participants,
                    key = { it.id }
                ) { participant ->
                    Column {
                        Text(participant.name, color = ProjectHubColors.Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(participant.email, color = ProjectHubColors.Muted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminProjectRatingsCard(participants: List<AdminProjectInfoParticipant>) {
    val language = currentAppSettings().language

    AdminProjectSectionCard(title = language.t("admin.projects.ratings")) {
        if (participants.isEmpty()) {
            Text(language.t("manager.team.noRatings"), color = ProjectHubColors.Muted, fontSize = 14.sp)
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = participants,
                    key = { it.id }
                ) { participant ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = participant.name,
                                color = ProjectHubColors.Ink,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = participant.rating
                                    ?.let { rating ->
                                        val clamped = rating.coerceIn(0, 5)
                                        "${"â˜…".repeat(clamped)}${"â˜†".repeat(5 - clamped)} $clamped/5"
                                    }
                                    ?: "â˜†â˜†â˜†â˜†â˜† 0/5",
                                color = participant.rating?.let { ProjectHubColors.Rating } ?: ProjectHubColors.Muted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        participant.comment?.takeIf { it.isNotBlank() }?.let { comment ->
                            Text(comment, color = ProjectHubColors.Muted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminProjectTasksCard(tasks: List<AdminProjectInfoTask>) {
    val language = currentAppSettings().language
    AdminProjectSectionCard(title = language.t("reports.tasks")) {
        if (tasks.isEmpty()) {
            AdminProjectMessageText(
                title = language.t("manager.projects.noTasksTitle"),
                detail = language.t("manager.projects.noTasksDetail")
            )
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = tasks,
                    key = { it.id }
                ) { task ->
                    AdminProjectTaskBlock(task = task)
                }
            }
        }
    }
}

@Composable
private fun AdminProjectTaskBlock(task: AdminProjectInfoTask) {
    val language = currentAppSettings().language
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ProjectHubColors.SurfaceSoft)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, color = ProjectHubColors.Ink, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Text(task.description, color = ProjectHubColors.Muted, fontSize = 12.sp)
            }
            Text(
                text = task.statusLabel,
                color = ProjectsAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        ProjectInfoRow(language.t("common.start"), task.startDate)
        ProjectInfoRow(language.t("common.deadline"), task.dueDate)
        ProjectInfoRow(
            language.t("tasks.responsibles"),
            task.assignees.takeIf { it.isNotEmpty() }?.joinToString()
                ?: language.t("tasks.noAssignees")
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = language.t("user.tasks.observations"),
            color = ProjectHubColors.Ink,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        if (task.observations.isEmpty()) {
            Text(language.t("user.tasks.noObservationsDetail"), color = ProjectHubColors.Muted, fontSize = 12.sp)
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = task.observations,
                    key = { it.id ?: it.text.hashCode() }
                ) { observation ->
                    AdminProjectObservationBlock(observation = observation)
                }
            }
        }
    }
}

@Composable
private fun AdminProjectObservationBlock(observation: AdminProjectInfoObservation) {
    AppObservationCard(
        observation = AppObservationUiModel(
            text = observation.text,
            userName = observation.userName,
            date = observation.date,
            completionPercent = observation.completionPercent,
            spentHours = observation.spentHours,
            location = observation.local,
            photoUrls = observation.photoUrls
        ),
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun AdminProjectSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = ProjectHubColors.Ink, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun AdminProjectMessageCard(
    title: String,
    detail: String
) {
    AppMessageCard(title = title, detail = detail, titleSize = 17)
}

@Composable
private fun AdminProjectMessageText(
    title: String,
    detail: String
) {
    Text(title, color = ProjectHubColors.Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    Text(detail, color = ProjectHubColors.Muted, fontSize = 12.sp)
}
