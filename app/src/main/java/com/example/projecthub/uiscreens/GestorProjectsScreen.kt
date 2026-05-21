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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.viewmodel.GestorProjectListItem
import com.example.projecthub.viewmodel.GestorProjectsState
import com.example.projecthub.viewmodel.GestorProjectsViewModel
import com.example.projecthub.viewmodel.GestorUserOption

private val GestorProjectsAccent = AuthAccent
private val GestorProjectsInk = Color(0xFF111827)
private val GestorProjectsMuted = Color(0xFF6B7280)
private val GestorProjectsBlue = Color(0xFF2563EB)
private val GestorProjectsGreen = Color(0xFF22C55E)
private val GestorProjectsGray = Color(0xFF94A3B8)
private val GestorProjectsRed = Color(0xFFEF4444)

private val GestorProjectStatuses = listOf(
    "Todos os Status",
    "Em Progresso",
    "Pendentes",
    "Concluidos"
)

@Composable
fun GestorProjectsScreen(
    gestorId: Int?,
    viewModel: GestorProjectsViewModel = viewModel()
) {
    val state = viewModel.state
    var projectToAssociate by remember { mutableStateOf<GestorProjectListItem?>(null) }
    var projectToComplete by remember { mutableStateOf<GestorProjectListItem?>(null) }

    LaunchedEffect(gestorId) {
        viewModel.loadProjects(gestorId)
    }

    Column {
        Text(
            text = "Meus Projetos",
            color = GestorProjectsInk,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        Text(
            text = "Acompanha projetos e equipa atribuida",
            color = GestorProjectsMuted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        ProjectFilters(
            state = state,
            onSearchChange = viewModel::updateSearchQuery,
            onStatusChange = viewModel::updateStatusFilter
        )

        Spacer(modifier = Modifier.height(18.dp))

        ProjectList(
            state = state,
            onToggleProject = viewModel::toggleProject,
            onAssociateUser = { project ->
                viewModel.clearMessages()
                projectToAssociate = project
            },
            onCompleteProject = { project ->
                viewModel.clearMessages()
                projectToComplete = project
            }
        )
    }

    projectToAssociate?.let { project ->
        AssociateUserDialog(
            project = project,
            users = state.userOptions.filterNot { option ->
                project.members.any { it.id == option.id }
            },
            isSaving = state.isAssociating,
            errorMessage = state.errorMessage,
            onDismiss = {
                viewModel.clearMessages()
                projectToAssociate = null
            },
            onConfirm = { userId ->
                viewModel.associateUserToProject(
                    projetoId = project.id,
                    userId = userId,
                    gestorId = gestorId
                )
                projectToAssociate = null
            }
        )
    }

    projectToComplete?.let { project ->
        CompleteProjectDialog(
            project = project,
            isSaving = state.isAssociating,
            errorMessage = state.errorMessage,
            onDismiss = {
                viewModel.clearMessages()
                projectToComplete = null
            },
            onConfirm = { ratings ->
                viewModel.completeProjectWithRatings(
                    project = project,
                    ratings = ratings,
                    gestorId = gestorId
                )
                projectToComplete = null
            }
        )
    }
}

@Composable
private fun ProjectFilters(
    state: GestorProjectsState,
    onSearchChange: (String) -> Unit,
    onStatusChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Pesquisar projetos...") },
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
}

@Composable
private fun StatusDropdown(
    selected: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val openClick = rememberSoundClick { expanded = true }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                .background(Color.White)
                .clickable(onClick = openClick)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selected,
                color = GestorProjectsInk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                text = if (expanded) "^" else "v",
                color = GestorProjectsMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            GestorProjectStatuses.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = GestorProjectsInk,
                            fontWeight = if (option == selected) FontWeight.Bold else FontWeight.Medium
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
private fun ProjectList(
    state: GestorProjectsState,
    onToggleProject: (Int) -> Unit,
    onAssociateUser: (GestorProjectListItem) -> Unit,
    onCompleteProject: (GestorProjectListItem) -> Unit
) {
    when {
        state.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GestorProjectsAccent)
            }
        }

        state.errorMessage != null -> {
            Text(
                text = state.errorMessage,
                color = GestorProjectsRed,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        state.visibleProjects.isEmpty() -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Text(
                    text = "Nao existem projetos para os filtros selecionados.",
                    color = GestorProjectsMuted,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(18.dp)
                )
            }
        }

        else -> {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                state.visibleProjects.forEach { project ->
                        ProjectCard(
                            project = project,
                            onToggleProject = onToggleProject,
                            onAssociateUser = onAssociateUser,
                        onCompleteProject = onCompleteProject
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: GestorProjectListItem,
    onToggleProject: (Int) -> Unit,
    onAssociateUser: (GestorProjectListItem) -> Unit,
    onCompleteProject: (GestorProjectListItem) -> Unit
) {
    val toggleClick = rememberSoundClick { onToggleProject(project.id) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = toggleClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        color = GestorProjectsInk,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = project.description,
                        color = Color(0xFF475569),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(project.statusLabel)
                    if (!project.isCompleted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CompleteIconButton(onClick = { onCompleteProject(project) })
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (project.isExpanded) "^" else "v",
                        color = GestorProjectsMuted,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }

            if (project.isExpanded) {
                Spacer(modifier = Modifier.height(18.dp))
                ProjectDetails(project = project)
                Spacer(modifier = Modifier.height(16.dp))
                ProjectMembers(
                    project = project,
                    onAssociateUser = onAssociateUser
                )
            }
        }
    }
}

@Composable
private fun ProjectDetails(project: GestorProjectListItem) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailItem(
            label = "Inicio",
            value = project.startDate,
            modifier = Modifier.weight(1f)
        )
        DetailItem(
            label = "Prazo",
            value = project.dueDate,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TaskLegend(color = GestorProjectsGreen, text = "Concluidas: ${project.completedTasks}")
        TaskLegend(color = GestorProjectsBlue, text = "Em progresso: ${project.inProgressTasks}")
        TaskLegend(color = GestorProjectsGray, text = "Pendentes: ${project.pendingTasks}")
    }

    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "Total de tarefas: ${project.totalTasks}",
        color = GestorProjectsMuted,
        fontSize = 13.sp
    )
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = GestorProjectsMuted,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            color = Color(0xFF334155),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun TaskLegend(
    color: Color,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            color = GestorProjectsInk,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ProjectMembers(
    project: GestorProjectListItem,
    onAssociateUser: (GestorProjectListItem) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TeamIcon(color = GestorProjectsMuted)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Equipa do Projeto",
                color = GestorProjectsInk,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }

        if (!project.isCompleted) {
            Button(
                onClick = rememberSoundClick { onAssociateUser(project) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GestorProjectsAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Associar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (project.members.isEmpty()) {
        Text(
            text = "Sem membros associados.",
            color = GestorProjectsMuted,
            fontSize = 14.sp
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            project.members.forEach { member ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = member.name,
                            color = GestorProjectsInk,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        if (project.isCompleted && member.rating != null) {
                            StarRatingText(rating = member.rating)
                        }
                    }
                    Text(
                        text = member.email,
                        color = GestorProjectsMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CompleteIconButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(GestorProjectsGreen)
            .clickable(onClick = rememberSoundClick(onClick)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "✓",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun CompleteProjectDialog(
    project: GestorProjectListItem,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (Map<Int, Int>) -> Unit
) {
    var ratings by remember(project.id) {
        mutableStateOf(project.members.associate { member -> member.id to (member.rating ?: 0) })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Concluir projeto") },
        text = {
            Column {
                Text(
                    text = "Avalia cada membro de 0 a 5 estrelas.",
                    color = GestorProjectsMuted,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (project.members.isEmpty()) {
                    Text(
                        text = "Este projeto nao tem membros para avaliar.",
                        color = GestorProjectsMuted
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        project.members.forEach { member ->
                            Column {
                                Text(
                                    text = member.name,
                                    color = GestorProjectsInk,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                StarSelector(
                                    rating = ratings[member.id] ?: 0,
                                    onRatingChange = { value ->
                                        ratings = ratings + (member.id to value)
                                    }
                                )
                            }
                        }
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = it, color = GestorProjectsRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = rememberSoundClick { onConfirm(ratings) }
            ) {
                Text(
                    text = if (isSaving) "A concluir..." else "Concluir",
                    color = GestorProjectsGreen,
                    fontWeight = FontWeight.Bold
                )
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
private fun StarSelector(
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        (0..5).forEach { value ->
            Text(
                text = if (value == 0) "0" else if (value <= rating) "★" else "☆",
                color = if (value == 0) GestorProjectsMuted else if (value <= rating) Color(0xFFF59E0B) else Color(0xFFD1D5DB),
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (value == 0) 15.sp else 26.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = rememberSoundClick { onRatingChange(value) })
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun StarRatingText(rating: Int) {
    val clamped = rating.coerceIn(0, 5)
    Text(
        text = "${"★".repeat(clamped)}${"☆".repeat(5 - clamped)} $clamped/5",
        color = Color(0xFFF59E0B),
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
    )
}

@Composable
private fun StatusPill(status: String) {
    val color = when (status) {
        "Concluido" -> GestorProjectsGreen
        "Em Progresso" -> GestorProjectsBlue
        "Pendente" -> GestorProjectsGray
        else -> GestorProjectsMuted
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 13.dp, vertical = 7.dp)
    ) {
        Text(
            text = status,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun AssociateUserDialog(
    project: GestorProjectListItem,
    users: List<GestorUserOption>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (Int?) -> Unit
) {
    var selectedUserId by remember(project.id, users) { mutableStateOf(users.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Associar utilizador") },
        text = {
            Column {
                Text(
                    text = project.name,
                    color = GestorProjectsInk,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (users.isEmpty()) {
                    Text(
                        text = "Nao existem utilizadores disponiveis para associar.",
                        color = GestorProjectsMuted
                    )
                } else {
                    UserDropdown(
                        users = users,
                        selectedUserId = selectedUserId,
                        onUserSelected = { selectedUserId = it }
                    )
                }
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = it, color = GestorProjectsRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving && users.isNotEmpty(),
                onClick = rememberSoundClick { onConfirm(selectedUserId) }
            ) {
                Text(
                    text = if (isSaving) "A associar..." else "Associar",
                    color = GestorProjectsAccent,
                    fontWeight = FontWeight.Bold
                )
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
private fun UserDropdown(
    users: List<GestorUserOption>,
    selectedUserId: Int?,
    onUserSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = users.firstOrNull { it.id == selectedUserId }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                .clickable(onClick = rememberSoundClick { expanded = true })
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selected?.name ?: "Seleciona um utilizador",
                color = GestorProjectsInk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = if (expanded) "^" else "v",
                color = GestorProjectsMuted,
                fontWeight = FontWeight.Bold
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            users.forEach { user ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(user.name, color = GestorProjectsInk, fontWeight = FontWeight.Bold)
                            Text(user.email, color = GestorProjectsMuted, fontSize = 12.sp)
                        }
                    },
                    onClick = {
                        expanded = false
                        onUserSelected(user.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun TeamIcon(color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val stroke = Stroke(width = 2.1.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color = color, radius = size.width * 0.13f, center = Offset(size.width * 0.38f, size.height * 0.34f), style = stroke)
        drawCircle(color = color, radius = size.width * 0.11f, center = Offset(size.width * 0.65f, size.height * 0.42f), style = stroke)
        drawArc(color = color, startAngle = 200f, sweepAngle = 140f, useCenter = false, topLeft = Offset(size.width * 0.18f, size.height * 0.52f), size = androidx.compose.ui.geometry.Size(size.width * 0.38f, size.height * 0.28f), style = stroke)
        drawArc(color = color, startAngle = 215f, sweepAngle = 110f, useCenter = false, topLeft = Offset(size.width * 0.52f, size.height * 0.6f), size = androidx.compose.ui.geometry.Size(size.width * 0.3f, size.height * 0.2f), style = stroke)
    }
}
