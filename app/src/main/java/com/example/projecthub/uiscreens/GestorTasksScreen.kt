package com.example.projecthub.uiscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.viewmodel.GestorProjectTaskGroup
import com.example.projecthub.viewmodel.GestorTaskListItem
import com.example.projecthub.viewmodel.GestorTaskProjectOption
import com.example.projecthub.viewmodel.GestorTaskStatusFilter
import com.example.projecthub.viewmodel.GestorTaskUserOption
import com.example.projecthub.viewmodel.GestorTasksState
import com.example.projecthub.viewmodel.GestorTasksViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val GestorTasksAccent = AuthAccent
private val GestorTasksInk = Color(0xFF111827)
private val GestorTasksMuted = Color(0xFF6B7280)
private val GestorTasksGreen = Color(0xFF22C55E)
private val GestorTasksOrange = Color(0xFFF97316)
private val GestorTasksRed = Color(0xFFEF4444)
private val GestorTasksBlue = Color(0xFF2563EB)

@Composable
fun GestorTasksScreen(
    gestorId: Int?,
    viewModel: GestorTasksViewModel = viewModel()
) {
    val state = viewModel.state
    var isCreatingTask by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<GestorTaskListItem?>(null) }
    var taskToEdit by remember { mutableStateOf<GestorTaskListItem?>(null) }

    LaunchedEffect(gestorId) {
        viewModel.loadTasks(gestorId)
    }

    if (isCreatingTask) {
        GestorAddTaskScreen(
            state = state,
            onBack = {
                viewModel.clearCreateError()
                isCreatingTask = false
            },
            onCreate = { title, description, projectId, startDate, endDate, userIds ->
                viewModel.createTask(
                    gestorId = gestorId,
                    title = title,
                    description = description,
                    projectId = projectId,
                    startDateText = startDate,
                    endDateText = endDate,
                    userIds = userIds,
                    onSuccess = { isCreatingTask = false }
                )
            }
        )
    } else {
        Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Gestao de Tarefas",
                    color = GestorTasksInk,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
                Text(
                    text = "Cria tarefas e acompanha os responsaveis por projeto",
                    color = GestorTasksMuted,
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = rememberSoundClick {
                    viewModel.clearCreateError()
                    isCreatingTask = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GestorTasksAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Nova", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TaskStats(state = state)
        Spacer(modifier = Modifier.height(14.dp))
        TaskFilters(
            state = state,
            onSearchChange = viewModel::updateSearchQuery,
            onStatusChange = viewModel::updateStatusFilter
        )
        Spacer(modifier = Modifier.height(16.dp))
        TaskProjectList(
            state = state,
            onToggleProject = viewModel::toggleProject,
            onEditTask = {
                viewModel.clearCreateError()
                taskToEdit = it
            },
            onDeleteTask = { taskToDelete = it }
        )
        }
    }

    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Eliminar tarefa") },
            text = { Text("Queres eliminar \"${task.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = rememberSoundClick {
                        viewModel.deleteTask(gestorId, task.id)
                        taskToDelete = null
                    }
                ) {
                    Text("Eliminar", color = GestorTasksRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = rememberSoundClick { taskToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    taskToEdit?.let { task ->
        EditGestorTaskDialog(
            task = task,
            users = state.users.filter { task.projectId in it.projectIds },
            isSaving = state.isCreating,
            errorMessage = state.createErrorMessage,
            onDismiss = {
                viewModel.clearCreateError()
                taskToEdit = null
            },
            onSave = { title, description, status, startDate, endDate, userIds ->
                viewModel.updateTask(
                    gestorId = gestorId,
                    task = task,
                    title = title,
                    description = description,
                    startDateText = startDate,
                    endDateText = endDate,
                    userIds = userIds,
                    onSuccess = { taskToEdit = null }
                )
            }
        )
    }
}

@Composable
private fun TaskStats(state: GestorTasksState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SmallStat("Total", state.totalTasks.toString(), GestorTasksAccent, Modifier.weight(1f))
        SmallStat("Pend.", state.pendingTasks.toString(), GestorTasksOrange, Modifier.weight(1f))
        SmallStat("Progr.", state.inProgressTasks.toString(), GestorTasksBlue, Modifier.weight(1f))
        SmallStat("Conc.", state.completedTasks.toString(), GestorTasksGreen, Modifier.weight(1f))
    }
}

@Composable
private fun SmallStat(title: String, value: String, accent: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = GestorTasksMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(value, color = accent, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun TaskFilters(
    state: GestorTasksState,
    onSearchChange: (String) -> Unit,
    onStatusChange: (GestorTaskStatusFilter) -> Unit
) {
    OutlinedTextField(
        value = state.searchQuery,
        onValueChange = onSearchChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Pesquisar tarefas...") },
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color(0xFFCBD5E1),
            unfocusedIndicatorColor = Color(0xFFCBD5E1)
        )
    )

    Spacer(modifier = Modifier.height(10.dp))
    StatusDropdown(
        selected = state.selectedStatus,
        onOptionSelected = onStatusChange
    )
}

@Composable
private fun StatusDropdown(
    selected: GestorTaskStatusFilter,
    onOptionSelected: (GestorTaskStatusFilter) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                .background(Color.White)
                .clickable(onClick = rememberSoundClick { expanded = true })
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(selected.label, color = GestorTasksInk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(if (expanded) "^" else "v", color = GestorTasksMuted, fontWeight = FontWeight.Bold)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            GestorTaskStatusFilter.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label, color = GestorTasksInk) },
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun TaskProjectList(
    state: GestorTasksState,
    onToggleProject: (Int) -> Unit,
    onEditTask: (GestorTaskListItem) -> Unit,
    onDeleteTask: (GestorTaskListItem) -> Unit
) {
    when {
        state.isLoading -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = GestorTasksAccent)
        }

        state.errorMessage != null -> Text(state.errorMessage, color = GestorTasksRed, fontWeight = FontWeight.Bold)

        state.projectGroups.isEmpty() -> Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Text("Nao existem tarefas para os filtros selecionados.", color = GestorTasksMuted, modifier = Modifier.padding(18.dp))
        }

        else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.projectGroups.forEach { group ->
                ProjectTaskGroupCard(
                    group = group,
                    onToggleProject = onToggleProject,
                    onEditTask = onEditTask,
                    onDeleteTask = onDeleteTask
                )
            }
        }
    }
}

@Composable
private fun ProjectTaskGroupCard(
    group: GestorProjectTaskGroup,
    onToggleProject: (Int) -> Unit,
    onEditTask: (GestorTaskListItem) -> Unit,
    onDeleteTask: (GestorTaskListItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = rememberSoundClick { onToggleProject(group.projectId) }),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(group.projectName, color = GestorTasksInk, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(
                        "${group.totalTasks} tarefas | ${group.completedTasks} concluidas | ${group.inProgressTasks} em progresso | ${group.pendingTasks} pendentes",
                        color = GestorTasksMuted,
                        fontSize = 12.sp
                    )
                }
                Text(if (group.isExpanded) "^" else "v", color = GestorTasksMuted, fontWeight = FontWeight.Bold)
            }

            if (group.isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                if (group.visibleTasks.isEmpty()) {
                    Text("Sem tarefas visiveis neste projeto.", color = GestorTasksMuted)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        group.visibleTasks.forEach { task ->
                            TaskRow(
                                task = task,
                                onEditTask = onEditTask,
                                onDeleteTask = onDeleteTask
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: GestorTaskListItem,
    onEditTask: (GestorTaskListItem) -> Unit,
    onDeleteTask: (GestorTaskListItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, color = GestorTasksInk, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Text(task.description, color = GestorTasksMuted, fontSize = 12.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusPill(task.statusLabel)
                TaskActionIcon("✏", GestorTasksAccent) { onEditTask(task) }
                TaskActionIcon("X", GestorTasksRed) { onDeleteTask(task) }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Inicio: ${task.startDate}   Prazo: ${task.dueDate}", color = Color(0xFF334155), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Associada a:", color = GestorTasksInk, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        if (task.assignees.isEmpty()) {
            Text("Sem utilizadores associados.", color = GestorTasksMuted, fontSize = 12.sp)
        } else {
            Text(task.assignees.joinToString { it.name }, color = GestorTasksMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TaskActionIcon(
    icon: String,
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
        Text(icon, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun StatusPill(status: String) {
    val color = when (status) {
        "Concluida" -> GestorTasksGreen
        "Em progresso" -> GestorTasksBlue
        "Atrasada" -> GestorTasksRed
        else -> GestorTasksOrange
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(status, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
private fun EditGestorTaskDialog(
    task: GestorTaskListItem,
    users: List<GestorTaskUserOption>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, Set<Int>) -> Unit
) {
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
        title = { Text("Editar tarefa") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titulo") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descricao") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                EditTaskDatePickerField(
                    value = startDate,
                    label = "Inicio",
                    onClick = { activeDateField = EditTaskDateField.Start }
                )
                Spacer(modifier = Modifier.height(8.dp))
                EditTaskDatePickerField(
                    value = endDate,
                    label = "Prazo",
                    onClick = { activeDateField = EditTaskDateField.End }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Utilizadores", color = GestorTasksInk, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                if (users.isEmpty()) {
                    Text("Este projeto ainda nao tem utilizadores associados.", color = GestorTasksMuted, fontSize = 13.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        users.forEach { user ->
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
                    Text(it, color = GestorTasksRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = rememberSoundClick {
                    onSave(title, description, task.rawStatus, startDate, endDate, selectedUserIds)
                }
            ) {
                Text(if (isSaving) "A guardar..." else "Guardar", color = GestorTasksAccent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = rememberSoundClick(onDismiss)) {
                Text("Cancelar")
            }
        }
    )

    if (activeDateField != null) {
        EditTaskDatePickerDialog(
            onDismiss = { activeDateField = null },
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
    Column {
        Text(label, color = GestorTasksInk, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
            .clickable(onClick = rememberSoundClick(onClick))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value.ifBlank { "dd/mm/aaaa" },
            color = if (value.isBlank()) GestorTasksMuted else GestorTasksInk,
            fontWeight = FontWeight.SemiBold
        )
        Text("Calendario", color = GestorTasksMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
    }
}

@Composable
private fun EditTaskDatePickerDialog(
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
                        onDateSelected(selectedMillis.toEditTaskDateText())
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

private fun String.toInputDateText(): String {
    if (this == "-") return ""
    val parts = split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else this
}

@Composable
private fun AddGestorTaskDialog(
    state: GestorTasksState,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onCreate: (String, String, Int?, String, String, Set<Int>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedProjectId by remember(state.projects) { mutableStateOf(state.projects.firstOrNull()?.id) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var selectedUserIds by remember { mutableStateOf(emptySet<Int>()) }
    val availableUsers = state.users.filter { selectedProjectId in it.projectIds }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova tarefa") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titulo") }, singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descricao") })
                Spacer(modifier = Modifier.height(8.dp))
                ProjectDropdown(state.projects, selectedProjectId) {
                    selectedProjectId = it
                    selectedUserIds = emptySet()
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Inicio dd/mm/aaaa") }, singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("Prazo dd/mm/aaaa") }, singleLine = true)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Utilizadores", color = GestorTasksInk, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                if (availableUsers.isEmpty()) {
                    Text("Este projeto ainda nao tem utilizadores associados.", color = GestorTasksMuted, fontSize = 13.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        availableUsers.forEach { user ->
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
                    Text(it, color = GestorTasksRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = rememberSoundClick {
                    onCreate(title, description, selectedProjectId, startDate, endDate, selectedUserIds)
                }
            ) {
                Text(if (isSaving) "A criar..." else "Criar", color = GestorTasksAccent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = rememberSoundClick(onDismiss)) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ProjectDropdown(
    projects: List<GestorTaskProjectOption>,
    selectedProjectId: Int?,
    onProjectSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = projects.firstOrNull { it.id == selectedProjectId }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                .clickable(onClick = rememberSoundClick { expanded = true })
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selected?.name ?: "Seleciona o projeto", color = GestorTasksInk, fontWeight = FontWeight.SemiBold)
            Text(if (expanded) "^" else "v", color = GestorTasksMuted, fontWeight = FontWeight.Bold)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
            projects.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project.name, color = GestorTasksInk) },
                    onClick = {
                        expanded = false
                        onProjectSelected(project.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun UserCheckRow(
    user: GestorTaskUserOption,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
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
                .border(1.dp, if (checked) GestorTasksAccent else Color(0xFFCBD5E1), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (checked) Text("✓", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(user.name, color = GestorTasksInk, fontWeight = FontWeight.SemiBold)
    }
}
