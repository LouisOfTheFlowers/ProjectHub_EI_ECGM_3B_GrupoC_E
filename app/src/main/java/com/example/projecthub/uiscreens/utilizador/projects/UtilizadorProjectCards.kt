package com.example.projecthub.uiscreens.utilizador.projects

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.AppBackButton
import com.example.projecthub.uiscreens.AppMessageCard
import com.example.projecthub.uiscreens.AppOutlinedActionButton
import com.example.projecthub.uiscreens.AppStatusChip
import com.example.projecthub.uiscreens.AuthAccent
import com.example.projecthub.uiscreens.isLandscapeLayout
import com.example.projecthub.viewmodel.utilizador.UtilizadorProjectItem
import com.example.projecthub.viewmodel.utilizador.UtilizadorProjectsState
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun UserProjectCard(
    item: UtilizadorProjectItem,
    onClick: () -> Unit
) {
    val language = currentAppSettings().language
    val project = item.project
    val isLandscape = isLandscapeLayout()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.nome,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (project.descricao?.isNotBlank() == true) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = project.descricao,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                }

                ProjectStatusPill(status = project.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProjectMeta(label = language.t("common.start"), value = project.data_inicio.toUiDate(), modifier = Modifier.weight(1f))
                ProjectMeta(label = language.t("common.end"), value = project.data_fim.toUiDate(), modifier = Modifier.weight(1f))
                ProjectMeta(label = language.t("user.projects.tasks"), value = item.tasksCount.toString(), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLandscape) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProjectMetric(
                            label = language.t("common.completed"),
                            value = item.completedTasks.toString(),
                            color = ProjectHubColors.Success,
                            modifier = Modifier.weight(1f)
                        )
                        ProjectMetric(
                            label = language.t("user.projects.late"),
                            value = item.lateTasks.toString(),
                            color = ProjectHubColors.Danger,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProjectMetric(
                            label = language.t("common.progress"),
                            value = projectProgress(item),
                            color = AuthAccent,
                            modifier = Modifier.weight(1f)
                        )
                        HistoryButton(
                            text = language.t("user.projects.viewHistory"),
                            onClick = onClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProjectMetric(
                            label = language.t("common.completed"),
                            value = item.completedTasks.toString(),
                            color = ProjectHubColors.Success,
                            modifier = Modifier.weight(1f)
                        )
                        ProjectMetric(
                            label = language.t("user.projects.late"),
                            value = item.lateTasks.toString(),
                            color = ProjectHubColors.Danger,
                            modifier = Modifier.weight(1f)
                        )
                        ProjectMetric(
                            label = language.t("common.progress"),
                            value = projectProgress(item),
                            color = AuthAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HistoryButton(
                        text = language.t("user.projects.viewHistory"),
                        onClick = onClick,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }
    }
}

