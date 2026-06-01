package com.example.projecthub.uiscreens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.R
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
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(gestorId) {
        viewModel.loadTeam(gestorId)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item { TeamHeader() }
        item { TeamStats(state = state) }
        item {
            TeamFilters(
                state = state,
                onSearchChange = viewModel::updateSearchQuery,
                onProjectChange = viewModel::updateProjectFilter
            )
        }
        when {
            state.isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TeamAccent)
                    }
                }
            }

            state.errorMessage != null -> {
                item {
                    Text(
                        text = state.errorMessage.orEmpty(),
                        color = TeamRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            state.visibleMembers.isEmpty() -> {
                item {
                    Text(
                        text = currentAppSettings().language.t("manager.team.noMembers"),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        fontSize = 15.sp
                    )
                }
            }

            else -> {
                items(
                    items = state.visibleMembers,
                    key = { it.id }
                ) { member ->
                    TeamMemberCard(
                        member = member,
                        selectedProjectId = state.selectedProjectId
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamHeader() {
    val language = currentAppSettings().language
    Column {
        Text(
            text = language.t("manager.team.title"),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        Text(
            text = language.t("manager.team.subtitle"),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun TeamStats(state: GestorTeamState) {
    val language = currentAppSettings().language
    val visibleRatings = state.visibleMembers.mapNotNull { member ->
        state.selectedProjectId?.let { member.averageRatingByProject[it] } ?: member.averageRating
    }
    val visibleAverageRating = visibleRatings.takeIf { it.isNotEmpty() }?.average()
    val visibleCompletedTasks = state.visibleMembers.sumOf { member ->
        state.selectedProjectId?.let { member.completedTasksByProject[it] } ?: member.completedTasks
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TeamStatCard(
            label = language.t("manager.team.members"),
            value = state.visibleMembers.size.toString(),
            color = TeamAccent,
            modifier = Modifier.weight(1f)
        )
        TeamStatCard(
            label = language.t("manager.team.average"),
            value = visibleAverageRating?.formatRating() ?: "-",
            color = TeamOrange,
            modifier = Modifier.weight(1f)
        )
        TeamStatCard(
            label = language.t("manager.team.completed"),
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
    val language = currentAppSettings().language
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
                placeholder = { Text(language.t("manager.team.search")) },
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
    val language = currentAppSettings().language
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = projects.firstOrNull { it.id == selectedProjectId }?.name ?: language.t("manager.team.allProjects")

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
            AppExpandIcon(
                expanded = expanded,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text(language.t("manager.team.allProjects")) },
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
private fun TeamMemberCard(
    member: GestorTeamMemberItem,
    selectedProjectId: Int?
) {
    val language = currentAppSettings().language
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
                    text = member.projectNames.joinToString(separator = " · ").ifBlank { language.t("manager.team.noProjects") },
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MemberMetric(
                    label = language.t("manager.team.ratingAverage"),
                    value = visibleRating?.let { "${it.formatRating()} / 5" } ?: language.t("manager.team.noRatings"),
                    color = TeamOrange,
                    modifier = Modifier.weight(1f),
                    showStars = visibleRating != null,
                    rating = visibleRating
                )
                MemberMetric(
                    label = language.t("manager.team.completedTasks"),
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
            Icon(
                painter = painterResource(R.drawable.ic_star_24),
                contentDescription = null,
                tint = if (filled) color else color.copy(alpha = 0.25f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

private fun Double.formatRating(): String {
    return String.format(Locale.US, "%.1f", this)
}

