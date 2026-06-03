package com.example.projecthub.uiscreens.gestor.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.components.AppBackButton
import com.example.projecthub.viewmodel.gestor.GestorTaskInfoState

@Composable
internal fun GestorTaskInfoPage(
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

