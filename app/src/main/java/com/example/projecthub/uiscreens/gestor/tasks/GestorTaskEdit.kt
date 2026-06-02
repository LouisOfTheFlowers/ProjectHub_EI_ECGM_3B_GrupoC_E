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

@Composable
internal fun EditGestorTaskDialog(
    task: GestorTaskListItem,
    users: List<GestorTaskUserOption>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, Set<Int>) -> Unit
) {
    val language = currentAppSettings().language
    var title by remember(task.id) { mutableStateOf(task.title) }
    var description by remember(task.id) { mutableStateOf(task.description) }
    var startDate by remember(task.id) { mutableStateOf(task.startDate.toInputDateText()) }
    var endDate by remember(task.id) { mutableStateOf(task.dueDate.toInputDateText()) }
    var selectedUserIds by remember(task.id) {
        mutableStateOf(task.assignees.map { it.id }.toSet())
    }
    var activeDateField by remember { mutableStateOf<EditTaskDateField?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(language.t("tasks.editTitle"))
        },
        text = {
            Column(modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion.responsiveDialogBody()) {
                _root_ide_package_.com.example.projecthub.uiscreens.AppTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = language.t("tasks.titleField"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                _root_ide_package_.com.example.projecthub.uiscreens.AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = language.t("tasks.descriptionField")
                )

                Spacer(modifier = Modifier.height(8.dp))

                EditTaskDatePickerField(
                    value = startDate,
                    label = language.t("common.start"),
                    onClick = {
                        activeDateField = EditTaskDateField.Start
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                EditTaskDatePickerField(
                    value = endDate,
                    label = language.t("common.deadline"),
                    onClick = {
                        activeDateField = EditTaskDateField.End
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = language.t("common.users"),
                    color = ProjectHubColors.Ink,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (users.isEmpty()) {
                    Text(
                        text = language.t("common.noProjectUsers"),
                        color = ProjectHubColors.Muted,
                        fontSize = 13.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(
                            items = users,
                            key = { it.id }
                        ) { user ->
                            UserCheckRow(
                                user = user,
                                checked = user.id in selectedUserIds,
                                onToggle = {
                                    selectedUserIds = if (user.id in selectedUserIds) {
                                        selectedUserIds - user.id
                                    } else {
                                        selectedUserIds + user.id
                                    }
                                }
                            )
                        }
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = it,
                        color = GestorTasksRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            _root_ide_package_.com.example.projecthub.uiscreens.AppDialogConfirmButton(
                text = if (isSaving) language.t("common.saving") else language.t("common.save"),
                enabled = !isSaving,
                onClick = {
                    onSave(
                        title,
                        description,
                        task.rawStatus,
                        startDate,
                        endDate,
                        selectedUserIds
                    )
                }
            )
        },
        dismissButton = {
            _root_ide_package_.com.example.projecthub.uiscreens.AppDialogCancelButton(
                text = language.t("common.cancel"),
                onClick = onDismiss
            )
        }
    )

    if (activeDateField != null) {
        EditTaskDatePickerDialog(
            onDismiss = {
                activeDateField = null
            },
            onDateSelected = { formattedDate ->
                when (activeDateField) {
                    EditTaskDateField.Start -> startDate = formattedDate
                    EditTaskDateField.End -> endDate = formattedDate
                    null -> Unit
                }

                activeDateField = null
            }
        )
    }
}

@Composable
private fun EditTaskDatePickerField(
    value: String,
    label: String,
    onClick: () -> Unit
) {
    val language = currentAppSettings().language

    Column {
        Text(
            text = label,
            color = ProjectHubColors.Ink,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, ProjectHubColors.Border, RoundedCornerShape(8.dp))
                .clickable(onClick = rememberSoundClick(onClick))
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value.ifBlank { "dd/mm/aaaa" },
                color = if (value.isBlank()) ProjectHubColors.Muted else ProjectHubColors.Ink,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = language.t("settings.dateFormat"),
                color = ProjectHubColors.Muted,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun EditTaskDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val language = currentAppSettings().language
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis

                    if (selectedMillis != null) {
                        onDateSelected(selectedMillis.toEditTaskDateText())
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(language.t("common.ok"))
            }
        },
        dismissButton = {
            TextButton(onClick = rememberSoundClick(onDismiss)) {
                Text(language.t("common.cancel"))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private enum class EditTaskDateField {
    Start,
    End
}

private fun Long.toEditTaskDateText(): String {
    val date = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

