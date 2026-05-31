package com.example.projecthub.uiscreens

import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.AdminTeamProjectOption
import com.example.projecthub.viewmodel.AdminTeamUserItem
import com.example.projecthub.viewmodel.AdminTeamEditableRoles
import com.example.projecthub.viewmodel.AdminTeamsState
import com.example.projecthub.viewmodel.AdminTeamsViewModel
import com.example.projecthub.ui.theme.ProjectHubColors

private val TeamsAccent = AuthAccent
private val TeamsGreen = ProjectHubColors.SuccessDark
private val TeamsOrange = ProjectHubColors.Warning
private val TeamsRed = ProjectHubColors.DangerDark

@Composable
fun AdminTeamsScreen(
    viewModel: AdminTeamsViewModel = viewModel()
) {
    val state = viewModel.state
    var userToDelete by remember { mutableStateOf<AdminTeamUserItem?>(null) }
    val language = currentAppSettings().language

    Column {
        TeamsHeader()
        Spacer(modifier = Modifier.height(18.dp))
        TeamBadges(state = state)
        Spacer(modifier = Modifier.height(16.dp))
        TeamFilters(
            state = state,
            onSearchChange = viewModel::updateSearchQuery,
            onRoleSelected = viewModel::updateRoleFilter,
            onProjectSelected = viewModel::updateProjectFilter
        )
        Spacer(modifier = Modifier.height(18.dp))
        TeamUserList(
            state = state,
            onRoleSelected = viewModel::updateUserRole,
            onDeleteUser = { userToDelete = it }
        )
    }

    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text(language.t("teams.removeTitle")) },
            text = { Text(language.t("teams.removeQuestion").format(user.name)) },
            confirmButton = {
                AppDialogConfirmButton(
                    text = language.t("common.delete"),
                    onClick = {
                        viewModel.deleteUser(user)
                        userToDelete = null
                    }
                )
            },
            dismissButton = {
                AppDialogCancelButton(
                    text = language.t("common.cancel"),
                    onClick = { userToDelete = null }
                )
            }
        )
    }
}

@Composable
private fun TeamsHeader() {
    val language = currentAppSettings().language
    Column {
        Text(
            text = language.t("teams.title"),
            color = ProjectHubColors.Ink,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        Text(
            text = language.t("teams.subtitle"),
            color = ProjectHubColors.Muted,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun TeamBadges(state: AdminTeamsState) {
    val language = currentAppSettings().language
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TeamBadge(
            label = language.t("teams.totalUsers"),
            value = state.totalUsers.toString(),
            accent = TeamsAccent,
            modifier = Modifier.fillMaxWidth()
        )
        TeamBadge(
            label = language.t("teams.activeUsers"),
            value = state.activeUsers.toString(),
            accent = TeamsGreen,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TeamBadge(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val language = currentAppSettings().language
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    color = accent,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = label,
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TeamFilters(
    state: AdminTeamsState,
    onSearchChange: (String) -> Unit,
    onRoleSelected: (String?) -> Unit,
    onProjectSelected: (Int?) -> Unit
) {
    val language = currentAppSettings().language
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            AppSearchField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                placeholder = language.t("teams.searchPlaceholder")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RoleFilter(
                    selectedRole = state.selectedRole,
                    roles = state.roles,
                    onRoleSelected = onRoleSelected,
                    modifier = Modifier.weight(1f)
                )
                ProjectFilter(
                    selectedProjectId = state.selectedProjectId,
                    projects = state.projects,
                    onProjectSelected = onProjectSelected,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleFilter(
    selectedRole: String?,
    roles: List<String>,
    onRoleSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val language = currentAppSettings().language

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedRole ?: language.t("common.allFemale"),
            onValueChange = {},
            readOnly = true,
            label = { Text(language.t("teams.role")) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = appTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(language.t("common.allFemale")) },
                onClick = {
                    onRoleSelected(null)
                    expanded = false
                }
            )
            roles.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role) },
                    onClick = {
                        onRoleSelected(role)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectFilter(
    selectedProjectId: Int?,
    projects: List<AdminTeamProjectOption>,
    onProjectSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val language = currentAppSettings().language
    val selectedProjectName = projects.firstOrNull { it.id == selectedProjectId }?.name ?: language.t("common.all")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedProjectName,
            onValueChange = {},
            readOnly = true,
            label = { Text(language.t("teams.project")) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = appTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(language.t("common.all")) },
                onClick = {
                    onProjectSelected(null)
                    expanded = false
                }
            )
            projects.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project.name) },
                    onClick = {
                        onProjectSelected(project.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TeamUserList(
    state: AdminTeamsState,
    onRoleSelected: (AdminTeamUserItem, String) -> Unit,
    onDeleteUser: (AdminTeamUserItem) -> Unit
) {
    val language = currentAppSettings().language
    when {
        state.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TeamsAccent)
            }
        }

        else -> {
            Column {
                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = TeamsRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (state.visibleUsers.isEmpty()) {
                    Text(
                        text = language.t("teams.noMatching"),
                        color = ProjectHubColors.Muted,
                        fontSize = 15.sp
                    )
                } else {
                    state.visibleUsers.forEach { user ->
                        TeamUserCard(
                            user = user,
                            isUpdatingRole = state.updatingRoleUserId == user.id,
                            isDeleting = state.deletingUserId == user.id,
                            onRoleSelected = onRoleSelected,
                            onDelete = { onDeleteUser(user) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}
@Composable
private fun TeamUserCard(
    user: AdminTeamUserItem,
    isUpdatingRole: Boolean,
    isDeleting: Boolean,
    onRoleSelected: (AdminTeamUserItem, String) -> Unit,
    onDelete: () -> Unit
) {
    val language = currentAppSettings().language
    val statusColor = if (user.isActive) TeamsGreen else TeamsOrange

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(TeamsAccent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = TeamsAccent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        color = ProjectHubColors.Ink,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "@${user.username}",
                        color = ProjectHubColors.Muted,
                        fontSize = 13.sp
                    )
                    Text(
                        text = user.email,
                        color = ProjectHubColors.Muted,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                StatusChip(text = user.status, color = statusColor)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(
                    text = if (user.projectNames.isEmpty()) {
                        "Sem projeto"
                    } else {
                        user.projectNames.joinToString(limit = 2, truncated = "+")
                    },
                    color = ProjectHubColors.Muted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            UserRoleEditor(
                user = user,
                isUpdating = isUpdatingRole,
                isDeleting = isDeleting,
                onRoleSelected = onRoleSelected,
                onDelete = onDelete
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserRoleEditor(
    user: AdminTeamUserItem,
    isUpdating: Boolean,
    isDeleting: Boolean,
    onRoleSelected: (AdminTeamUserItem, String) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val language = currentAppSettings().language
    val deleteClick = rememberSoundClick(onDelete)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (!isUpdating) {
                    expanded = !expanded
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = user.role,
                onValueChange = {},
                readOnly = true,
                enabled = !isUpdating,
                label = { Text(language.t("teams.role")) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = appTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        enabled = !isUpdating
                    )
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                AdminTeamEditableRoles.forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role) },
                        onClick = {
                            onRoleSelected(user, role)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (isUpdating) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = TeamsAccent
            )
        }

        Button(
            onClick = deleteClick,
            enabled = !isUpdating && !isDeleting,
            colors = ButtonDefaults.buttonColors(
                containerColor = TeamsRed,
                contentColor = Color.White,
                disabledContainerColor = TeamsRed.copy(alpha = 0.45f),
                disabledContentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isDeleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(
                    text = language.t("teams.remove"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    AppStatusChip(text = text, contentColor = color)
}
