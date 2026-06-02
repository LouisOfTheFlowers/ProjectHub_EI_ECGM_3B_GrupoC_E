package com.example.projecthub.uiscreens.gestor.tasks

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.R
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.responsiveDialogBody
import com.example.projecthub.viewmodel.gestor.GestorProjectTaskGroup
import com.example.projecthub.viewmodel.gestor.GestorTaskInfoObservation
import com.example.projecthub.viewmodel.gestor.GestorTaskInfoState
import com.example.projecthub.viewmodel.gestor.GestorTaskListItem
import com.example.projecthub.viewmodel.gestor.GestorTaskProjectOption
import com.example.projecthub.viewmodel.gestor.GestorTaskStatusFilter
import com.example.projecthub.viewmodel.gestor.GestorTaskUserOption
import com.example.projecthub.viewmodel.gestor.GestorTasksState
import com.example.projecthub.viewmodel.gestor.GestorTasksViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal val GestorTasksAccent = _root_ide_package_.com.example.projecthub.uiscreens.AuthAccent
internal val GestorTasksGreen = ProjectHubColors.Success
internal val GestorTasksOrange = ProjectHubColors.Warning
internal val GestorTasksRed = ProjectHubColors.Danger
internal val GestorTasksBlue = ProjectHubColors.Info
@Composable
internal fun TaskInfoMessageCard(
    title: String,
    detail: String
) {
    _root_ide_package_.com.example.projecthub.uiscreens.AppMessageCard(
        title = title,
        detail = detail
    )
}


@Composable
internal fun TaskActionIcon(
    painter: Painter,
    contentDescription: String?,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .clickable(onClick = rememberSoundClick(onClick)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier.size(17.dp)
        )
    }
}


@Composable
internal fun StatusPill(status: String) {
    _root_ide_package_.com.example.projecthub.uiscreens.AppStatusChip(text = status)
}


internal fun String.toInputDateText(): String {
    if (this == "-") return ""

    val parts = split("-")

    return if (parts.size == 3) {
        "${parts[2]}/${parts[1]}/${parts[0]}"
    } else {
        this
    }
}


@Composable
internal fun UserCheckRow(
    user: GestorTaskUserOption,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ProjectHubColors.SurfaceSoft)
            .clickable(onClick = rememberSoundClick(onToggle))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(22.dp)
                .clip(CircleShape)
                .background(if (checked) GestorTasksAccent else Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (checked) GestorTasksAccent else ProjectHubColors.BorderSoft,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Text(
                    text = "âœ“",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = user.name,
            color = ProjectHubColors.Ink,
            fontWeight = FontWeight.SemiBold
        )
    }
}

