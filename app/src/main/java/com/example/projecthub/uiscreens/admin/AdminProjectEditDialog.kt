package com.example.projecthub.uiscreens.admin

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.viewmodel.AdminProjectListItem
import com.example.projecthub.viewmodel.AdminProjectManager
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
internal fun EditProjectDialog(
    project: AdminProjectListItem,
    managers: List<AdminProjectManager>,
    errorMessage: String?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Int?) -> Unit
) {
    val language = currentAppSettings().language
    val datePattern = currentAppSettings().dateFormat.pattern
    var name by remember(project.id) { mutableStateOf(project.name) }
    var description by remember(project.id) { mutableStateOf(project.description) }
    var startDate by remember(project.id, datePattern) { mutableStateOf(project.startDate.toAdminProjectDisplayDate(datePattern)) }
    var endDate by remember(project.id, datePattern) { mutableStateOf(project.dueDate.toAdminProjectDisplayDate(datePattern)) }
    var selectedManagerId by remember(project.id) { mutableStateOf(project.managerId) }
    var managerDropdownOpen by remember { mutableStateOf(false) }
    val saveClick = rememberSoundClick { onSave(name, description, startDate, endDate, selectedManagerId) }
    val dismissClick = rememberSoundClick(onDismiss)

    val selectedManagerName = managers.firstOrNull { it.id == selectedManagerId }?.name
        ?: "Seleciona um gestor"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(language.t("projects.editTitle")) },
        text = {
            Column(modifier = Modifier.responsiveDialogBody()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(language.t("projects.name")) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    label = { Text(language.t("projects.description")) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProjectDatePickerField(
                    label = language.t("addProject.startDate"),
                    value = startDate,
                    onDateSelected = { selectedDate ->
                        startDate = selectedDate

                        val start = selectedDate.toProjectLocalDateOrNull()
                        val end = endDate.toProjectLocalDateOrNull()

                        if (start != null && end != null && end.isBefore(start)) {
                            endDate = ""
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProjectDatePickerField(
                    label = language.t("addProject.endDate"),
                    value = endDate,
                    minDate = startDate.toProjectLocalDateOrNull(),
                    onDateSelected = { selectedDate ->
                        endDate = selectedDate
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, ProjectHubColors.Border, RoundedCornerShape(8.dp))
                            .clickable { managerDropdownOpen = true }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedManagerName,
                            color = ProjectHubColors.Ink,
                            fontWeight = FontWeight.SemiBold
                        )
                        AppExpandIcon(expanded = false)
                    }

                    DropdownMenu(
                        expanded = managerDropdownOpen,
                        onDismissRequest = { managerDropdownOpen = false },
                        modifier = Modifier.background(ProjectHubColors.LightSurface)
                    ) {
                        managers.forEach { manager ->
                            DropdownMenuItem(
                                text = { Text(manager.name) },
                                onClick = {
                                    selectedManagerId = manager.id
                                    managerDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(errorMessage, color = ProjectsRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = saveClick
            ) {
                Text(language.t("common.save"), color = ProjectsAccent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = dismissClick, enabled = !isSaving) {
                Text(language.t("common.cancel"))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectDatePickerField(
    label: String,
    value: String,
    minDate: LocalDate? = null,
    onDateSelected: (String) -> Unit
) {
    var isDialogOpen by remember { mutableStateOf(false) }
    val language = currentAppSettings().language
    val openClick = rememberSoundClick { isDialogOpen = true }

    val selectedDate = value.toProjectLocalDateOrNull()
    val selectedDateMillis = selectedDate?.toAdminProjectEpochMillis()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant
                    .ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()

                return minDate == null || !date.isBefore(minDate)
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = openClick)
    ) {
        OutlinedTextField(
            value = value.toAdminProjectDisplayDate(currentAppSettings().dateFormat.pattern),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = true,
            singleLine = true,
            label = { Text(label) }
        )
    }

    if (isDialogOpen) {
        DatePickerDialog(
            onDismissRequest = { isDialogOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis

                        if (millis != null) {
                            val selected = Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()

                            onDateSelected(
                                selected.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            )
                        }

                        isDialogOpen = false
                    }
                ) {
                    Text(language.t("common.confirm"), color = ProjectsAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = rememberSoundClick { isDialogOpen = false }) {
                    Text(language.t("common.cancel"))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
