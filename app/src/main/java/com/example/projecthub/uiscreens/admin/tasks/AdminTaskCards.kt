package com.example.projecthub.uiscreens.admin.tasks

import com.example.projecthub.uiscreens.*

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.R
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.viewmodel.admin.AdminProjectTaskGroup
import com.example.projecthub.viewmodel.admin.AdminTaskListItem
import com.example.projecthub.viewmodel.admin.AdminTaskStatusFilter
import com.example.projecthub.viewmodel.admin.AdminTasksState
import com.example.projecthub.viewmodel.admin.AdminTasksViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import com.example.projecthub.ui.theme.ProjectHubColors

@Composable
internal fun TaskCard(task: AdminTaskListItem) {
    val statusColor = when {
        task.isCompleted -> TasksGreen
        task.isDelayed -> TasksRed
        else -> TasksOrange
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.SurfaceSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.statusLabel.uppercase(),
                        color = statusColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.title,
                        color = ProjectHubColors.Ink,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = task.description,
                        color = ProjectHubColors.Muted,
                        fontSize = 13.sp
                    )
                }

                CompletionIcon(
                    isCompleted = task.isCompleted,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TaskInfoRow(currentAppSettings().language.t("common.start"), task.startDate.toDisplayDate(currentAppSettings().dateFormat.pattern))
            TaskInfoRow(currentAppSettings().language.t("common.deadline"), task.dueDate.toDisplayDate(currentAppSettings().dateFormat.pattern))
        }
    }
}

@Composable
private fun CompletionIcon(
    isCompleted: Boolean,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(if (isCompleted) R.drawable.ic_check_circle_24 else R.drawable.ic_schedule_24),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun TaskInfoRow(label: String, value: String) {
    AppInfoRow(label = label, value = value)
}

private fun String.toDisplayDate(pattern: String = "dd/MM/yyyy"): String {
    val trimmed = trim()

    if (trimmed.isBlank() || trimmed == "-") {
        return trimmed
    }

    val date = try {
        LocalDate.parse(trimmed.take(10))
    } catch (_: DateTimeParseException) {
        return trimmed
    }

    return date.format(DateTimeFormatter.ofPattern(pattern))
}

