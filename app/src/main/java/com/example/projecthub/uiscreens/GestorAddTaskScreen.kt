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
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.viewmodel.GestorTaskProjectOption
import com.example.projecthub.viewmodel.GestorTaskUserOption
import com.example.projecthub.viewmodel.GestorTasksState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val GestorAddTaskAccent = AuthAccent
private val GestorAddTaskInk = Color(0xFF111827)
private val GestorAddTaskMuted = Color(0xFF6B7280)
private val GestorAddTaskRed = Color(0xFFEF4444)

@Composable
fun GestorAddTaskScreen(
    state: GestorTasksState,
    onBack: () -> Unit,
    onCreate: (String, String, Int?, String, String, Set<Int>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedProject by remember { mutableStateOf<GestorTaskProjectOption?>(null) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var selectedUserIds by remember { mutableStateOf(emptySet<Int>()) }
    var activeDateField by remember { mutableStateOf<GestorTaskDateField?>(null) }
    val availableUsers = state.users.filter { selectedProject?.id in it.projectIds }

    val backClick = rememberSoundClick(onBack)
    val createClick = rememberSoundClick {
        onCreate(title, description, selectedProject?.id, startDate, endDate, selectedUserIds)
    }

    Column {
        TextButton(
            onClick = backClick,
            colors = ButtonDefaults.textButtonColors(contentColor = GestorAddTaskInk)
        ) {
            Text("< Voltar às Tarefas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                    text = "Adicionar Nova Tarefa",
                    color = GestorAddTaskInk,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                FormLabel("Título")
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text
                    ),
                    placeholder = { Text("Ex: Preparar documentação") }
                )

                Spacer(modifier = Modifier.height(14.dp))

                FormLabel("Descrição")
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
                    placeholder = { Text("Descreve o objetivo da tarefa...") }
                )

                Spacer(modifier = Modifier.height(14.dp))

                FormLabel("Projeto")
                GestorProjectDropdown(
                    selectedProject = selectedProject,
                    projects = state.projects,
                    onProjectSelected = {
                        selectedProject = it
                        selectedUserIds = emptySet()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                FormLabel("Utilizadores")
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

                FormLabel("Data de Início")
                GestorTaskDatePickerField(
                    value = startDate,
                    onClick = rememberSoundClick { activeDateField = GestorTaskDateField.Start }
                )

                Spacer(modifier = Modifier.height(14.dp))

                FormLabel("Data de Fim (Prazo)")
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

                Button(
                    onClick = createClick,
                    enabled = !state.isCreating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GestorAddTaskAccent,
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
                        Text("Criar Tarefa", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = backClick,
                    enabled = !state.isCreating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE5E7EB),
                        contentColor = GestorAddTaskInk
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
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
        color = GestorAddTaskInk,
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
                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                .clickable(enabled = projects.isNotEmpty(), onClick = openClick)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedProject?.name ?: "Selecione um projeto",
                color = if (selectedProject == null) GestorAddTaskMuted else GestorAddTaskInk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                text = if (expanded) "^" else "v",
                color = GestorAddTaskMuted,
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
                            color = GestorAddTaskInk,
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
    when {
        !enabled -> {
            DisabledBox("Seleciona primeiro um projeto")
        }

        users.isEmpty() -> {
            DisabledBox("Este projeto ainda não tem utilizadores associados")
        }

        else -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                users.forEach { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
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
                                    if (user.id in selectedUserIds) GestorAddTaskAccent else Color(0xFFCBD5E1),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (user.id in selectedUserIds) {
                                Text("✓", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(user.name, color = GestorAddTaskInk, fontWeight = FontWeight.SemiBold)
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
            .background(Color(0xFFF3F4F6))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, color = GestorAddTaskMuted, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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
            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
            .clickable(onClick = click)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = value.ifBlank { "dd/mm/aaaa" },
            color = if (value.isBlank()) GestorAddTaskMuted else GestorAddTaskInk,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
        Text(
            text = "Formato de data",
            color = GestorAddTaskMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
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
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = rememberSoundClick(onDismiss)) {
                Text("Cancelar")
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
