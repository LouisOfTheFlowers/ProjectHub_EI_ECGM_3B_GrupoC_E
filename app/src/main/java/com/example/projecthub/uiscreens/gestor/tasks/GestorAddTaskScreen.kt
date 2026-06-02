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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.R
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.GestorTaskProjectOption
import com.example.projecthub.viewmodel.GestorTaskUserOption
import com.example.projecthub.viewmodel.GestorTasksState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.projecthub.ui.theme.ProjectHubColors

private val GestorAddTaskAccent = _root_ide_package_.com.example.projecthub.uiscreens.AuthAccent
private val GestorAddTaskRed = ProjectHubColors.Danger

@Composable
fun GestorAddTaskScreen(
    state: GestorTasksState,
    onBack: () -> Unit,
    onCreate: (String, String, Int?, String, String, Set<Int>) -> Unit
) {
    val language = currentAppSettings().language
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedProject by remember { mutableStateOf<GestorTaskProjectOption?>(null) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var selectedUserIds by remember { mutableStateOf(emptySet<Int>()) }
    var activeDateField by remember { mutableStateOf<GestorTaskDateField?>(null) }
    val availableUsers = state.users.filter { selectedProject?.id in it.projectIds }

    Column {
        _root_ide_package_.com.example.projecthub.uiscreens.AppBackButton(
            text = language.t("addTask.back"),
            onClick = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        _root_ide_package_.com.example.projecthub.uiscreens.AppFormCard(title = language.t("addTask.title")) {
            _root_ide_package_.com.example.projecthub.uiscreens.AppFormLabel(language.t("addTask.taskTitle"))
            _root_ide_package_.com.example.projecthub.uiscreens.AppTextField(
                value = title,
                onValueChange = { title = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text
                ),
                placeholder = language.t("addTask.titlePlaceholder")
            )

            Spacer(modifier = Modifier.height(14.dp))

            _root_ide_package_.com.example.projecthub.uiscreens.AppFormLabel(language.t("projects.description"))
            _root_ide_package_.com.example.projecthub.uiscreens.AppTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.height(120.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text
                ),
                placeholder = language.t("addTask.descriptionPlaceholder")
            )

            Spacer(modifier = Modifier.height(14.dp))

            _root_ide_package_.com.example.projecthub.uiscreens.AppFormLabel(language.t("teams.project"))
            GestorProjectDropdown(
                selectedProject = selectedProject,
                projects = state.projects,
                onProjectSelected = {
                    selectedProject = it
                    selectedUserIds = emptySet()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            _root_ide_package_.com.example.projecthub.uiscreens.AppFormLabel(language.t("addTask.users"))
            UserMultiSelect(
                users = availableUsers,
                selectedUserIds = selectedUserIds,
                enabled = selectedProject != null,
                onToggleUser = { userId ->
                    selectedUserIds = if (userId in selectedUserIds) {
                        selectedUserIds - userId
                    } else {
                        selectedUserIds + userId
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            _root_ide_package_.com.example.projecthub.uiscreens.AppFormLabel(language.t("addProject.startDate"))
            GestorTaskDatePickerField(
                value = startDate,
                onClick = rememberSoundClick { activeDateField = GestorTaskDateField.Start }
            )

            Spacer(modifier = Modifier.height(14.dp))

            _root_ide_package_.com.example.projecthub.uiscreens.AppFormLabel(language.t("addProject.endDate"))
            GestorTaskDatePickerField(
                value = endDate,
                onClick = rememberSoundClick { activeDateField = GestorTaskDateField.End }
            )

            if (state.createErrorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.createErrorMessage,
                    color = GestorAddTaskRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            _root_ide_package_.com.example.projecthub.uiscreens.AppPrimaryButton(
                text = language.t("addTask.submit"),
                onClick = {
                    onCreate(
                        title,
                        description,
                        selectedProject?.id,
                        startDate,
                        endDate,
                        selectedUserIds
                    )
                },
                enabled = !state.isCreating,
                isLoading = state.isCreating,
                containerColor = GestorAddTaskAccent
            )

            Spacer(modifier = Modifier.height(10.dp))

            _root_ide_package_.com.example.projecthub.uiscreens.AppSecondaryButton(
                text = language.t("common.cancel"),
                onClick = onBack,
                enabled = !state.isCreating
            )
        }
    }

    if (activeDateField != null) {
        GestorTaskDatePickerDialog(
            onDismiss = { activeDateField = null },
            onDateSelected = { formattedDate ->
                when (activeDateField) {
                    GestorTaskDateField.Start -> startDate = formattedDate
                    GestorTaskDateField.End -> endDate = formattedDate
                    null -> Unit
                }
                activeDateField = null
            }
        )
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        color = ProjectHubColors.Ink,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun GestorProjectDropdown(
    selectedProject: GestorTaskProjectOption?,
    projects: List<GestorTaskProjectOption>,
    onProjectSelected: (GestorTaskProjectOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val openClick = rememberSoundClick { expanded = true }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, ProjectHubColors.Border, RoundedCornerShape(8.dp))
                .clickable(enabled = projects.isNotEmpty(), onClick = openClick)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedProject?.name ?: currentAppSettings().language.t("addTask.projectPlaceholder"),
                color = if (selectedProject == null) ProjectHubColors.Muted else ProjectHubColors.Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            _root_ide_package_.com.example.projecthub.uiscreens.AppExpandIcon(expanded = expanded)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ProjectHubColors.LightSurface)
        ) {
            projects.forEach { project ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = project.name,
                            color = ProjectHubColors.Ink,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        expanded = false
                        onProjectSelected(project)
                    }
                )
            }
        }
    }
}

@Composable
private fun UserMultiSelect(
    users: List<GestorTaskUserOption>,
    selectedUserIds: Set<Int>,
    enabled: Boolean,
    onToggleUser: (Int) -> Unit
) {
    val language = currentAppSettings().language
    when {
        !enabled -> {
            DisabledBox(language.t("addTask.selectProjectFirst"))
        }

        users.isEmpty() -> {
            DisabledBox(language.t("common.noProjectUsers"))
        }

        else -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                users.forEach { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, ProjectHubColors.Border, RoundedCornerShape(8.dp))
                            .clickable(onClick = rememberSoundClick { onToggleUser(user.id) })
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(22.dp)
                                .clip(CircleShape)
                                .background(if (user.id in selectedUserIds) GestorAddTaskAccent else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (user.id in selectedUserIds) GestorAddTaskAccent else ProjectHubColors.BorderSoft,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (user.id in selectedUserIds) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check_circle_24),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(user.name, color = ProjectHubColors.Ink, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DisabledBox(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ProjectHubColors.DisabledSoft)
            .border(1.dp, ProjectHubColors.Disabled, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, color = ProjectHubColors.Muted, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

private enum class GestorTaskDateField {
    Start,
    End
}

@Composable
private fun GestorTaskDatePickerField(
    value: String,
    onClick: () -> Unit
) {
    val click = rememberSoundClick(onClick)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, ProjectHubColors.Border, RoundedCornerShape(8.dp))
            .clickable(onClick = click)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = value.ifBlank { "dd/mm/aaaa" },
            color = if (value.isBlank()) ProjectHubColors.Muted else ProjectHubColors.Ink,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun GestorTaskDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        onDateSelected(selectedMillis.toGestorTaskDateText())
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(currentAppSettings().language.t("common.ok"))
            }
        },
        dismissButton = {
            TextButton(onClick = rememberSoundClick(onDismiss)) {
                Text(currentAppSettings().language.t("common.cancel"))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun Long.toGestorTaskDateText(): String {
    val date = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}
