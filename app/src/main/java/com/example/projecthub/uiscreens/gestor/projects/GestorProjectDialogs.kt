package com.example.projecthub.uiscreens.gestor.projects

import com.example.projecthub.uiscreens.*

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.projecthub.R
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.viewmodel.gestor.GestorProjectInfoObservation
import com.example.projecthub.viewmodel.gestor.GestorProjectInfoState
import com.example.projecthub.viewmodel.gestor.GestorProjectInfoTask
import com.example.projecthub.viewmodel.gestor.GestorProjectListItem
import com.example.projecthub.viewmodel.gestor.GestorProjectsState
import com.example.projecthub.viewmodel.gestor.GestorProjectsViewModel
import com.example.projecthub.viewmodel.gestor.GestorUserOption
@Composable
internal fun GestorCompleteProjectDialog(
    project: GestorProjectListItem,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (Map<Int, Int>) -> Unit
) {
    var ratings by remember(project.id) {
        mutableStateOf(
            project.members.associate { member ->
                member.id to (member.rating ?: 0)
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(currentAppSettings().language.t("manager.projects.completeTitle"))
        },
        text = {
            Column {
                Text(
                    text = currentAppSettings().language.t("manager.projects.rateMembers"),
                    color = ProjectHubColors.Muted,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (project.members.isEmpty()) {
                    Text(
                        text = currentAppSettings().language.t("manager.projects.noMembersToRate"),
                        color = ProjectHubColors.Muted
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = project.members,
                            key = { it.id }
                        ) { member ->
                            Column {
                                Text(
                                    text = member.name,
                                    color = ProjectHubColors.Ink,
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

                    Text(
                        text = it,
                        color = GestorProjectsRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = rememberSoundClick {
                    onConfirm(ratings)
                }
            ) {
                Text(
                    text = if (isSaving) {
                        currentAppSettings().language.t("common.completing")
                    } else {
                        currentAppSettings().language.t("user.tasks.complete")
                    },
                    color = GestorProjectsGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = rememberSoundClick(onDismiss)) {
                Text(currentAppSettings().language.t("common.cancel"))
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
                text = if (value == 0) {
                    "0"
                } else if (value <= rating) {
                    "★"
                } else {
                    "☆"
                },
                color = if (value == 0) {
                    ProjectHubColors.Muted
                } else if (value <= rating) {
                    ProjectHubColors.Rating
                } else {
                    ProjectHubColors.Border
                },
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (value == 0) 15.sp else 26.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(
                        onClick = rememberSoundClick {
                            onRatingChange(value)
                        }
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
internal fun GestorAssociateUserDialog(
    project: GestorProjectListItem,
    users: List<GestorUserOption>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (Int?) -> Unit
) {
    var selectedUserId by remember(project.id, users) {
        mutableStateOf(users.firstOrNull()?.id)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(currentAppSettings().language.t("manager.projects.associateUser"))
        },
        text = {
            Column {
                Text(
                    text = project.name,
                    color = ProjectHubColors.Ink,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (users.isEmpty()) {
                    Text(
                        text = currentAppSettings().language.t("manager.projects.noAvailableUsers"),
                        color = ProjectHubColors.Muted
                    )
                } else {
                    UserDropdown(
                        users = users,
                        selectedUserId = selectedUserId,
                        onUserSelected = {
                            selectedUserId = it
                        }
                    )
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = it,
                        color = GestorProjectsRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving && users.isNotEmpty(),
                onClick = rememberSoundClick {
                    onConfirm(selectedUserId)
                }
            ) {
                Text(
                    text = if (isSaving) {
                        currentAppSettings().language.t("common.saving")
                    } else {
                        currentAppSettings().language.t("manager.projects.associate")
                    },
                    color = GestorProjectsAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = rememberSoundClick(onDismiss)) {
                Text(currentAppSettings().language.t("common.cancel"))
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
    var expanded by remember {
        mutableStateOf(false)
    }

    val selected = users.firstOrNull {
        it.id == selectedUserId
    }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, ProjectHubColors.Border, RoundedCornerShape(8.dp))
                .clickable(
                    onClick = rememberSoundClick {
                        expanded = true
                    }
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selected?.name ?: currentAppSettings().language.t("common.selectUser"),
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            AppExpandIcon(expanded = expanded)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier.background(ProjectHubColors.LightSurface)
        ) {
            users.forEach { user ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = user.name,
                                color = ProjectHubColors.Ink,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = user.email,
                                color = ProjectHubColors.Muted,
                                fontSize = 12.sp
                            )
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
