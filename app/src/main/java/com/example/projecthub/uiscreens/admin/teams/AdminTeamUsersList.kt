package com.example.projecthub.uiscreens.admin.teams

import com.example.projecthub.uiscreens.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.admin.AdminTeamProjectOption
import com.example.projecthub.viewmodel.admin.AdminTeamUserItem
import com.example.projecthub.viewmodel.admin.AdminTeamEditableRoles
import com.example.projecthub.viewmodel.admin.AdminTeamsState
import com.example.projecthub.viewmodel.admin.AdminTeamsViewModel
import com.example.projecthub.ui.theme.ProjectHubColors

@Composable
internal fun TeamUserList(
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
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 720.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = state.visibleUsers,
                            key = { it.id }
                        ) { user ->
                            TeamUserCard(
                                user = user,
                                isUpdatingRole = state.updatingRoleUserId == user.id,
                                isDeleting = state.deletingUserId == user.id,
                                onRoleSelected = onRoleSelected,
                                onDelete = { onDeleteUser(user) }
                            )
                        }
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
                        language.t("common.noProject")
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

