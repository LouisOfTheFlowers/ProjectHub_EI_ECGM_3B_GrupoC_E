package com.example.projecthub.uiscreens

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
import androidx.compose.material3.OutlinedButton
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
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.viewmodel.UtilizadorDashboardState
import com.example.projecthub.viewmodel.UtilizadorProjectItem
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun UtilizadorProjectsSection(
    state: UtilizadorDashboardState,
    projectHistoryId: Int?,
    onOpenHistory: (Int) -> Unit,
    onBack: () -> Unit
) {
    val language = currentAppSettings().language
    if (projectHistoryId != null) {
        val item = state.projects.firstOrNull { it.project.id == projectHistoryId }
        when {
            state.isLoading || (state.projects.isEmpty() && state.errorMessage == null) -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AuthAccent)
                }
            }

            item != null -> ProjectTaskHistoryPage(
                item = item,
                onBack = onBack
            )

            else -> Column {
                AppBackButton(
                    text = language.t("user.projects.back"),
                    onClick = onBack
                )
                Spacer(modifier = Modifier.height(18.dp))
                ProjectMessageCard(
                    title = language.t("user.projects.notFoundTitle"),
                    detail = language.t("user.projects.notFoundDetail")
                )
            }
        }
        return
    }

    Column {
        Text(
            text = language.t("user.projects.title"),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = language.t("user.projects.subtitle"),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(18.dp))

        if (state.errorMessage != null) {
            ProjectMessageCard(
                title = language.t("user.projects.stateTitle"),
                detail = state.errorMessage
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AuthAccent)
                }
            }

            state.projects.isEmpty() -> {
                ProjectMessageCard(
                    title = language.t("user.projects.emptyTitle"),
                    detail = language.t("user.projects.emptyDetail")
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 720.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.projects,
                        key = { it.project.id ?: it.project.nome }
                    ) { item ->
                        UserProjectCard(
                            item = item,
                            onClick = { item.project.id?.let(onOpenHistory) }
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun UserProjectCard(
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

@Composable
private fun ProjectTaskHistoryPage(
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

@Composable
private fun ProjectMeta(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProjectMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Column {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun HistoryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppOutlinedActionButton(
        text = text,
        onClick = onClick,
        modifier = modifier.height(50.dp)
    )
}

@Composable
private fun ProjectStatusPill(status: String) {
    AppStatusChip(text = status)
}

@Composable
private fun ProjectMessageCard(
    title: String,
    detail: String
) {
    AppMessageCard(title = title, detail = detail)
}

private fun projectProgress(item: UtilizadorProjectItem): String {
    if (item.tasksCount == 0) return "0%"
    return "${((item.completedTasks.toFloat() / item.tasksCount.toFloat()) * 100).toInt()}%"
}

private fun String?.toUiDate(): String {
    val date = this?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: return "-"
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

private fun String.isCompletedStatus(): Boolean {
    return normalizedStatus() in setOf(
        "CONCLUIDO",
        "CONCLUIDA",
        "COMPLETO",
        "COMPLETA",
        "COMPLETADO",
        "COMPLETADA",
        "FINALIZADO",
        "FINALIZADA"
    )
}

private fun String.normalizedStatus(): String {
    val withoutAccents = Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")

    return withoutAccents
        .trim()
        .replace(" ", "_")
        .replace("-", "_")
        .uppercase()
}
