package com.example.projecthub.uiscreens.admin.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.R
import com.example.projecthub.viewmodel.admin.AdminTaskListItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.components.AppStatusChip
import com.example.projecthub.uiscreens.components.AppInfoRow
import com.example.projecthub.uiscreens.components.statusColorForLabel

@Composable
internal fun TaskCard(task: AdminTaskListItem) {
    val statusColor = statusColorForLabel(task.statusLabel)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.SurfaceSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = task.title,
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
            Text(
                text = task.description,
                color = ProjectHubColors.Muted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppStatusChip(text = task.statusLabel)

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

