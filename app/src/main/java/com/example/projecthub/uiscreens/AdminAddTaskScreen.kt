package com.example.projecthub.uiscreens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.AdminTaskProjectOption
import com.example.projecthub.viewmodel.AdminTasksState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val AddTaskAccent = AuthAccent
private val AddTaskInk = Color(0xFF111827)
private val AddTaskMuted = Color(0xFF6B7280)
private val AddTaskRed = Color(0xFFEF4444)

@Composable
fun AdminAddTaskScreen(
    state: AdminTasksState,
    onBack: () -> Unit,
    onCreate: (String, String, Int?, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedProject by remember { mutableStateOf<AdminTaskProjectOption?>(null) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var activeDateField by remember { mutableStateOf<TaskDateField?>(null) }
    val language = currentAppSettings().language
    val backClick = rememberSoundClick(onBack)
    val createClick = rememberSoundClick {
        onCreate(title, description, selectedProject?.id, startDate, endDate)
    }

    Column {
        TextButton(
            onClick = backClick,
            colors = ButtonDefaults.textButtonColors(contentColor = AddTaskInk)
        ) {
            Text(
                text = language.t("addTask.back"),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = language.t("addTask.title"),
                    color = AddTaskInk,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                FormLabel(language.t("addTask.taskTitle"))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text
                    ),
                    placeholder = { Text(language.t("addTask.titlePlaceholder")) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                FormLabel(language.t("projects.description"))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text
                    ),
                    placeholder = { Text(language.t("addTask.descriptionPlaceholder")) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                FormLabel(language.t("teams.project"))
                ProjectDropdown(
                    selectedProject = selectedProject,
                    projects = state.projects,
                    onProjectSelected = { selectedProject = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                FormLabel(language.t("addProject.startDate"))
                TaskDatePickerField(
                    value = startDate,
                    onClick = rememberSoundClick { activeDateField = TaskDateField.Start }
                )

                Spacer(modifier = Modifier.height(14.dp))

                FormLabel(language.t("addProject.endDate"))
                TaskDatePickerField(
                    value = endDate,
                    onClick = rememberSoundClick { activeDateField = TaskDateField.End }
                )

                if (state.createErrorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = state.createErrorMessage,
                        color = AddTaskRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = createClick,
                    enabled = !state.isCreating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AddTaskAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (state.isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = language.t("addTask.submit"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = backClick,
                    enabled = !state.isCreating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE5E7EB),
                        contentColor = AddTaskInk
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = language.t("common.cancel"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    if (activeDateField != null) {
        TaskDatePickerDialog(
            onDismiss = { activeDateField = null },
            onDateSelected = { formattedDate ->
                when (activeDateField) {
                    TaskDateField.Start -> startDate = formattedDate
                    TaskDateField.End -> endDate = formattedDate
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
        color = AddTaskInk,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun ProjectDropdown(
    selectedProject: AdminTaskProjectOption?,
    projects: List<AdminTaskProjectOption>,
    onProjectSelected: (AdminTaskProjectOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val language = currentAppSettings().language
    val openClick = rememberSoundClick { expanded = true }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                .clickable(enabled = projects.isNotEmpty(), onClick = openClick)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedProject?.name ?: language.t("addTask.projectPlaceholder"),
                color = if (selectedProject == null) AddTaskMuted else AddTaskInk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                text = if (expanded) "^" else "v",
                color = AddTaskMuted,
                fontWeight = FontWeight.Bold
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            projects.forEach { project ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = project.name,
                            color = AddTaskInk,
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

private enum class TaskDateField {
    Start,
    End
}

@Composable
private fun TaskDatePickerField(
    value: String,
    onClick: () -> Unit
) {
    val click = rememberSoundClick(onClick)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
            .clickable(onClick = click)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = value.ifBlank { "dd/mm/aaaa" },
            color = if (value.isBlank()) AddTaskMuted else AddTaskInk,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
        Text(
            text = currentAppSettings().language.t("settings.dateFormat"),
            color = AddTaskMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun TaskDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    val language = currentAppSettings().language

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        onDateSelected(selectedMillis.toTaskDateText())
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text("OK")
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

private fun Long.toTaskDateText(): String {
    val date = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}
