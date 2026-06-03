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
internal fun ProjectTaskHistoryPage(
    item: UtilizadorProjectItem,
    onBack: () -> Unit
) {
    val language = currentAppSettings().language
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppBackButton(
            text = language.t("user.projects.back"),
            onClick = onBack
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.project.nome,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = language.t("user.projects.historySubtitle"),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            ProjectStatusPill(status = item.project.status)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProjectMetric(
                label = language.t("common.completed"),
                value = item.completedTasks.toString(),
                color = ProjectHubColors.Success,
                modifier = Modifier.weight(1f)
            )
            ProjectMetric(
                label = language.t("common.total"),
                value = item.tasksCount.toString(),
                color = AuthAccent,
                modifier = Modifier.weight(1f)
            )
            ProjectMetric(
                label = language.t("common.progress"),
                value = projectProgress(item),
                color = ProjectHubColors.InfoLight,
                modifier = Modifier.weight(1f)
            )
        }

        if (item.completedTaskHistory.isEmpty()) {
            ProjectMessageCard(
                title = language.t("user.projects.noCompletedTitle"),
                detail = language.t("user.projects.noCompletedDetail")
            )
        } else {
            item.completedTaskHistory.forEach { task ->
                CompletedTaskHistoryRow(task = task)
            }
        }
    }
}

@Composable
private fun CompletedTaskHistoryRow(task: TarefaDto) {
    val language = currentAppSettings().language
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = task.titulo,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            if (task.descricao?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.descricao,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProjectMeta(label = language.t("common.status"), value = task.status, modifier = Modifier.weight(1f))
                ProjectMeta(label = language.t("common.start"), value = task.data_inicio.toUiDate(), modifier = Modifier.weight(1f))
                ProjectMeta(label = language.t("common.end"), value = task.data_fim.toUiDate(), modifier = Modifier.weight(1f))
            }
        }
    }
}

