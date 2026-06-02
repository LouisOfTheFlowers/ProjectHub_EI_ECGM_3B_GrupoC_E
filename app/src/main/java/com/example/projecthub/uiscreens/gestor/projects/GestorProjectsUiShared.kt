package com.example.projecthub.uiscreens.gestor.projects

import com.example.projecthub.uiscreens.*

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.projecthub.R
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.viewmodel.gestor.GestorProjectInfoObservation
import com.example.projecthub.viewmodel.gestor.GestorProjectInfoState
import com.example.projecthub.viewmodel.gestor.GestorProjectInfoTask
import com.example.projecthub.viewmodel.gestor.GestorProjectListItem
import com.example.projecthub.viewmodel.gestor.GestorProjectsState
import com.example.projecthub.viewmodel.gestor.GestorProjectsViewModel
import com.example.projecthub.viewmodel.gestor.GestorUserOption
internal val GestorProjectsAccent = AuthAccent
internal val GestorProjectsBlue = ProjectHubColors.Info
internal val GestorProjectsGreen = ProjectHubColors.Success
internal val GestorProjectsRed = ProjectHubColors.Danger

internal val GestorProjectStatuses = listOf(
    "Todos os Status",
    "Em progresso",
    "Pendentes",
    "Concluídos"
)

@Composable
internal fun ProjectDetails(
    project: GestorProjectListItem
) {
    val language = currentAppSettings().language
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailItem(
            label = language.t("common.start"),
            value = project.startDate,
            modifier = Modifier.weight(1f)
        )

        DetailItem(
            label = language.t("common.deadline"),
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
            text = "${language.t("common.completed")}: ${project.completedTasks}"
        )

        TaskLegend(
            color = GestorProjectsBlue,
            text = "${language.t("common.inProgress")}: ${project.inProgressTasks}"
        )

        TaskLegend(
            color = ProjectHubColors.SidebarMutedText,
            text = "${language.t("common.pending")}: ${project.pendingTasks}"
        )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = language.t("manager.projects.tasksTotal").format(project.totalTasks),
        color = ProjectHubColors.Muted,
        fontSize = 13.sp
    )
}

@Composable
internal fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    AppDetailItem(
        label = label,
        value = value,
        modifier = modifier
    )
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
internal fun ProjectMembers(
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
        LazyColumn(
            modifier = Modifier.heightIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = project.members,
                key = { it.id }
            ) { member ->
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
internal fun CompleteIconButton(
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
internal fun StarRatingText(
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
internal fun StatusPill(
    status: String
) {
    AppStatusChip(text = status)
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
