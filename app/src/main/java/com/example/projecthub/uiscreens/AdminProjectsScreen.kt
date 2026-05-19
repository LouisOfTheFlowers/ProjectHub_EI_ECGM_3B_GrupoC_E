package com.example.projecthub.uiscreens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.AdminProjectListItem
import com.example.projecthub.viewmodel.AdminProjectManager
import com.example.projecthub.viewmodel.AdminProjectsState
import com.example.projecthub.viewmodel.AdminProjectsViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val ProjectsAccent = AuthAccent
private val ProjectsInk = Color(0xFF111827)
private val ProjectsMuted = Color(0xFF6B7280)
private val ProjectsRed = Color(0xFFEF4444)
private val ProjectsGreen = Color(0xFF22C55E)

@Composable
fun AdminProjectsScreen(
    viewModel: AdminProjectsViewModel = viewModel()
) {
    val state = viewModel.state
    var isCreatingProject by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<AdminProjectListItem?>(null) }
    var projectToEdit by remember { mutableStateOf<AdminProjectListItem?>(null) }
    val language = currentAppSettings().language

    if (isCreatingProject) {
        AdminAddProjectScreen(
            state = state,
            onBack = {
                viewModel.clearCreateError()
                isCreatingProject = false
            },
            onCreate = { name, description, startDate, endDate, managerId ->
                viewModel.createProject(
                    name = name,
                    description = description,
                    startDateText = startDate,
                    endDateText = endDate,
                    managerId = managerId,
                    onSuccess = { isCreatingProject = false }
                )
            }
        )
    } else {
        ProjectsPage(
            state = state,
            onAddProject = {
                viewModel.clearCreateError()
                isCreatingProject = true
            },
            onSearchChange = viewModel::updateSearchQuery,
            onStatusChange = viewModel::updateStatusFilter,
            onCoordinatorChange = viewModel::updateCoordinatorFilter,
            onDeleteProject = { projectToDelete = it },
            onEditProject = {
                viewModel.clearCreateError()
                projectToEdit = it
            }
        )
    }

    projectToDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text(language.t("projects.deleteTitle")) },
            text = { Text("${language.t("projects.deleteQuestion")} \"${project.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = rememberSoundClick {
                        viewModel.deleteProject(project.id)
                        projectToDelete = null
                    }
                ) {
                    Text(language.t("common.delete"), color = ProjectsRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = rememberSoundClick { projectToDelete = null }) {
                    Text(language.t("common.cancel"))
                }
            }
        )
    }

    projectToEdit?.let { project ->
        EditProjectDialog(
            project = project,
            managers = state.managers,
            errorMessage = state.createErrorMessage,
            isSaving = state.isCreating,
            onDismiss = {
                viewModel.clearCreateError()
                projectToEdit = null
            },
            onSave = { name, description, startDate, endDate, managerId ->
                viewModel.updateProject(
                    projectId = project.id,
                    name = name,
                    description = description,
                    startDateText = startDate,
                    endDateText = endDate,
                    managerId = managerId,
                    onSuccess = { projectToEdit = null }
                )
            }
        )
    }
}

@Composable
private fun ProjectsPage(
    state: AdminProjectsState,
    onAddProject: () -> Unit,
    onSearchChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onCoordinatorChange: (String) -> Unit,
    onDeleteProject: (AdminProjectListItem) -> Unit,
    onEditProject: (AdminProjectListItem) -> Unit
) {
    val language = currentAppSettings().language
    val addProjectClick = rememberSoundClick(onAddProject)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.t("projects.title"),
                    color = ProjectsInk,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
                Text(
                    text = language.t("projects.list"),
                    color = ProjectsMuted,
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = addProjectClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProjectsAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = language.t("projects.add"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        ProjectStats(state = state)
        Spacer(modifier = Modifier.height(16.dp))

        ProjectFilters(
            state = state,
            onSearchChange = onSearchChange,
            onStatusChange = onStatusChange,
            onCoordinatorChange = onCoordinatorChange
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = language.t("projects.list"),
            color = ProjectsInk,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ProjectsAccent)
                }
            }

            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage,
                    color = ProjectsRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            state.visibleProjects.isEmpty() -> {
                Text(
                    text = "Nenhum projeto encontrado.",
                    color = ProjectsMuted,
                    fontSize = 15.sp
                )
            }

            else -> {
                state.visibleProjects.forEach { project ->
                    ProjectListCard(
                        project = project,
                        onDelete = { onDeleteProject(project) },
                        onEdit = { onEditProject(project) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun ProjectStats(state: AdminProjectsState) {
    val language = currentAppSettings().language
    ProjectStatCard(
        title = language.t("common.completed"),
        value = state.completedCount.toString(),
        accent = ProjectsAccent,
        icon = ProjectStatIcon.Completed
    )
    Spacer(modifier = Modifier.height(10.dp))
    ProjectStatCard(
        title = language.t("common.inProgress"),
        value = state.inProgressCount.toString(),
        accent = ProjectsGreen,
        icon = ProjectStatIcon.Trend
    )
    Spacer(modifier = Modifier.height(10.dp))
    ProjectStatCard(
        title = language.t("common.delayed"),
        value = state.delayedCount.toString(),
        accent = ProjectsRed,
        icon = ProjectStatIcon.Clock
    )
}

@Composable
private fun ProjectStatCard(
    title: String,
    value: String,
    accent: Color,
    icon: ProjectStatIcon
) {
    val language = currentAppSettings().language
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                ProjectStatIcon(icon = icon, color = accent)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = ProjectsMuted, fontSize = 14.sp)
                Text(
                    text = value,
                    color = ProjectsInk,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                )
            }
        }
    }
}

@Composable
private fun ProjectFilters(
    state: AdminProjectsState,
    onSearchChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onCoordinatorChange: (String) -> Unit
) {
    val language = currentAppSettings().language
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(language.t("projects.search")) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterDropdown(
                    label = state.selectedStatus,
                    options = listOf("Todos", "Concluídos", "Em progresso", "Atrasados"),
                    modifier = Modifier.weight(1f),
                    onOptionSelected = onStatusChange
                )
                FilterDropdown(
                    label = state.selectedCoordinator,
                    options = listOf("Todos") + state.coordinators,
                    modifier = Modifier.weight(1f),
                    onOptionSelected = onCoordinatorChange
                )
            }
        }
    }
}

@Composable
private fun FilterDropdown(
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val openClick = rememberSoundClick { expanded = true }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                .clickable(onClick = openClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = ProjectsInk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = if (expanded) "^" else "v",
                color = ProjectsMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = ProjectsInk,
                            fontWeight = if (option == label) FontWeight.Bold else FontWeight.Medium
                        )
                    },
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
private fun ProjectListCard(
    project: AdminProjectListItem,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val statusColor = when {
        project.isCompleted -> ProjectsAccent
        project.isDelayed -> ProjectsRed
        else -> ProjectsGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        color = ProjectsInk,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = project.description,
                        color = ProjectsMuted,
                        fontSize = 13.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = project.statusLabel,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(statusColor.copy(alpha = 0.14f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionIconButton("✏", ProjectsAccent, onEdit)
                        ActionIconButton("X", ProjectsRed, onDelete)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProjectInfoRow("Coordenador", project.coordinator)
            ProjectInfoRow("Pessoas", project.memberCount.toString())
            ProjectInfoRow("Início", project.startDate.toDisplayDate(currentAppSettings().dateFormat.pattern))
            ProjectInfoRow("Prazo", project.dueDate.toDisplayDate(currentAppSettings().dateFormat.pattern))
        }
    }
}

@Composable
private fun ProjectInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = ProjectsMuted, fontSize = 13.sp)
        Text(
            text = value,
            color = ProjectsInk,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ActionIconButton(
    icon: String,
    color: Color,
    onClick: () -> Unit
) {
    val click = rememberSoundClick(onClick)
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .clickable(onClick = click),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun EditProjectDialog(
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
    var startDate by remember(project.id, datePattern) { mutableStateOf(project.startDate.toDisplayDate(datePattern)) }
    var endDate by remember(project.id, datePattern) { mutableStateOf(project.dueDate.toDisplayDate(datePattern)) }
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
            Column {
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
                            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                            .clickable { managerDropdownOpen = true }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedManagerName,
                            color = ProjectsInk,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("v", color = ProjectsMuted, fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = managerDropdownOpen,
                        onDismissRequest = { managerDropdownOpen = false },
                        modifier = Modifier.background(Color.White)
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
    val selectedDateMillis = selectedDate?.toEpochMillis()

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
            value = value.toDisplayDate(currentAppSettings().dateFormat.pattern),
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

private enum class ProjectStatIcon {
    Completed,
    Trend,
    Clock
}

@Composable
private fun ProjectStatIcon(
    icon: ProjectStatIcon,
    color: Color
) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)

        when (icon) {
            ProjectStatIcon.Completed -> {
                drawCircle(color = color, style = stroke)
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.28f, size.height * 0.52f),
                    end = Offset(size.width * 0.44f, size.height * 0.68f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.44f, size.height * 0.68f),
                    end = Offset(size.width * 0.74f, size.height * 0.34f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }

            ProjectStatIcon.Trend -> {
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.18f, size.height * 0.7f),
                    end = Offset(size.width * 0.42f, size.height * 0.48f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.42f, size.height * 0.48f),
                    end = Offset(size.width * 0.56f, size.height * 0.6f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.56f, size.height * 0.6f),
                    end = Offset(size.width * 0.82f, size.height * 0.3f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.66f, size.height * 0.3f),
                    end = Offset(size.width * 0.82f, size.height * 0.3f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.82f, size.height * 0.3f),
                    end = Offset(size.width * 0.82f, size.height * 0.46f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }

            ProjectStatIcon.Clock -> {
                drawCircle(color = color, style = stroke)
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.5f, size.height * 0.5f),
                    end = Offset(size.width * 0.5f, size.height * 0.28f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.5f, size.height * 0.5f),
                    end = Offset(size.width * 0.68f, size.height * 0.62f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

private fun String.toProjectLocalDateOrNull(): LocalDate? {
    val trimmed = trim()

    if (trimmed.isBlank() || trimmed == "-") {
        return null
    }

    return try {
        LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (_: DateTimeParseException) {
        runCatching { LocalDate.parse(trimmed.take(10)) }.getOrNull()
    }
}

private fun String.toDisplayDate(pattern: String = "dd/MM/yyyy"): String {
    val date = toProjectLocalDateOrNull() ?: return this

    return date.format(DateTimeFormatter.ofPattern(pattern))
}

private fun LocalDate.toEpochMillis(): Long {
    return atStartOfDay()
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()
}
