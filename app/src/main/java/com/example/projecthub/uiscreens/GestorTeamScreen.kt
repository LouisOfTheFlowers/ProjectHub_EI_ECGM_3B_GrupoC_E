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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.viewmodel.GestorTeamMemberItem
import com.example.projecthub.viewmodel.GestorTeamProjectOption
import com.example.projecthub.viewmodel.GestorTeamState
import com.example.projecthub.viewmodel.GestorTeamViewModel
import java.util.Locale
import com.example.projecthub.ui.theme.ProjectHubColors

private val TeamAccent = AuthAccent
private val TeamGreen = ProjectHubColors.Success
private val TeamOrange = ProjectHubColors.Warning
private val TeamRed = ProjectHubColors.Danger

@Composable
fun GestorTeamScreen(
    gestorId: Int?,
    viewModel: GestorTeamViewModel = viewModel()
) {
    val state = viewModel.state

    LaunchedEffect(gestorId) {
        viewModel.loadTeam(gestorId)
    }

    Column {
        TeamHeader()
        Spacer(modifier = Modifier.height(18.dp))
        TeamStats(state = state)
        Spacer(modifier = Modifier.height(16.dp))
        TeamFilters(
            state = state,
            onSearchChange = viewModel::updateSearchQuery,
            onProjectChange = viewModel::updateProjectFilter
        )
        Spacer(modifier = Modifier.height(18.dp))
        TeamList(state = state)
    }
}

@Composable
private fun TeamHeader() {
    Column {
        Text(
            text = "Minha Equipa",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        Text(
            text = "Utilizadores associados aos teus projetos",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun TeamStats(state: GestorTeamState) {
    val visibleRatings = state.visibleMembers.mapNotNull { member ->
        state.selectedProjectId?.let { member.averageRatingByProject[it] } ?: member.averageRating
    }
    val visibleAverageRating = visibleRatings.takeIf { it.isNotEmpty() }?.average()
    val visibleCompletedTasks = state.visibleMembers.sumOf { member ->
        state.selectedProjectId?.let { member.completedTasksByProject[it] } ?: member.completedTasks
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TeamStatCard(
            label = "Membros",
            value = state.visibleMembers.size.toString(),
            color = TeamAccent,
            modifier = Modifier.weight(1f)
        )
        TeamStatCard(
            label = "Media",
            value = visibleAverageRating?.formatRating() ?: "-",
            color = TeamOrange,
            modifier = Modifier.weight(1f)
        )
        TeamStatCard(
            label = "Concluidas",
            value = visibleCompletedTasks.toString(),
            color = TeamGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TeamStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(82.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
            Text(
                text = value,
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 27.sp
            )
        }
    }
}

@Composable
private fun TeamFilters(
    state: GestorTeamState,
    onSearchChange: (String) -> Unit,
    onProjectChange: (Int?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Pesquisar por nome, username, email ou projeto...") },
                colors = appTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            ProjectDropdown(
                projects = state.projects,
                selectedProjectId = state.selectedProjectId,
                onProjectChange = onProjectChange
            )
        }
    }
}

@Composable
private fun ProjectDropdown(
    projects: List<GestorTeamProjectOption>,
    selectedProjectId: Int?,
    onProjectChange: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = projects.firstOrNull { it.id == selectedProjectId }?.name ?: "Todos os projetos"

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedLabel,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = if (expanded) "^" else "v",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text("Todos os projetos") },
                onClick = {
                    expanded = false
                    onProjectChange(null)
                }
            )
            projects.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project.name) },
                    onClick = {
                        expanded = false
                        onProjectChange(project.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun TeamList(state: GestorTeamState) {
    when {
        state.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TeamAccent)
            }
        }

        state.errorMessage != null -> {
            Text(
                text = state.errorMessage,
                color = TeamRed,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        state.visibleMembers.isEmpty() -> {
            Text(
                text = "Nenhum membro encontrado.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                fontSize = 15.sp
            )
        }

        else -> {
            state.visibleMembers.forEach { member ->
                TeamMemberCard(
                    member = member,
                    selectedProjectId = state.selectedProjectId
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun TeamMemberCard(
    member: GestorTeamMemberItem,
    selectedProjectId: Int?
) {
    val visibleRating = selectedProjectId?.let { member.averageRatingByProject[it] }
        ?: member.averageRating
    val visibleCompletedTasks = selectedProjectId?.let { member.completedTasksByProject[it] }
        ?: member.completedTasks

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(TeamAccent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.name.take(1).uppercase(),
                        color = TeamAccent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = member.email,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        fontSize = 13.sp
                    )
                }
            }

            if (selectedProjectId == null) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = member.projectNames.joinToString(separator = " · ").ifBlank { "Sem projetos" },
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MemberMetric(
                    label = "Media das estrelas",
                    value = visibleRating?.let { "${it.formatRating()} / 5" } ?: "Sem avaliacoes",
                    color = TeamOrange,
                    modifier = Modifier.weight(1f),
                    showStars = visibleRating != null,
                    rating = visibleRating
                )
                MemberMetric(
                    label = "Tarefas concluidas",
                    value = visibleCompletedTasks.toString(),
                    color = TeamGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MemberMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    showStars: Boolean = false,
    rating: Double? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(10.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (showStars && rating != null) {
            RatingStars(rating = rating, color = color)
            Spacer(modifier = Modifier.height(3.dp))
        }
        Text(
            text = value,
            color = color,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp
        )
    }
}

@Composable
private fun RatingStars(
    rating: Double,
    color: Color
) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(5) { index ->
            val filled = rating >= index + 0.5
            Canvas(modifier = Modifier.size(14.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension * 0.43f
                val points = List(10) { pointIndex ->
                    val angle = Math.toRadians((pointIndex * 36.0) - 90.0)
                    val pointRadius = if (pointIndex % 2 == 0) radius else radius * 0.45f
                    Offset(
                        x = center.x + kotlin.math.cos(angle).toFloat() * pointRadius,
                        y = center.y + kotlin.math.sin(angle).toFloat() * pointRadius
                    )
                }
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                    close()
                }
                drawPath(
                    path = path,
                    color = if (filled) color else color.copy(alpha = 0.25f)
                )
            }
        }
    }
}

private fun Double.formatRating(): String {
    return String.format(Locale.US, "%.1f", this)
}
