package com.example.projecthub.uiscreens.gestor.tasks

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.responsiveDialogBody
import com.example.projecthub.uiscreens.components.AppDialogCancelButton
import com.example.projecthub.uiscreens.components.AppDialogConfirmButton
import com.example.projecthub.uiscreens.components.AppExpandIcon
import com.example.projecthub.uiscreens.components.AppTextField
import com.example.projecthub.viewmodel.gestor.GestorTaskListItem
import com.example.projecthub.viewmodel.gestor.GestorTaskProjectOption
import com.example.projecthub.viewmodel.gestor.GestorTasksState

@Composable
internal fun DeleteGestorTaskDialog(
    task: GestorTaskListItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val language = currentAppSettings().language

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(language.t("tasks.deleteTitle")) },
        text = { Text(language.t("tasks.deleteQuestion").format(task.title)) },
        confirmButton = {
            AppDialogConfirmButton(
                text = language.t("common.delete"),
                onClick = onConfirm
            )
        },
        dismissButton = {
            AppDialogCancelButton(
                text = language.t("common.cancel"),
                onClick = onDismiss
            )
        }
    )
}
@Composable
internal fun AddGestorTaskDialog(
    state: GestorTasksState,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onCreate: (String, String, Int?, String, String, Set<Int>) -> Unit
) {
    val language = currentAppSettings().language
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedProjectId by remember(state.projects) {
        mutableStateOf(state.projects.firstOrNull()?.id)
    }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var selectedUserIds by remember { mutableStateOf(emptySet<Int>()) }

    val availableUsers = state.users.filter {
        selectedProjectId in it.projectIds
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(language.t("tasks.newTitle"))
        },
        text = {
            Column(modifier = Modifier.responsiveDialogBody()) {
                AppTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = language.t("tasks.titleField"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = language.t("tasks.descriptionField")
                )

                Spacer(modifier = Modifier.height(8.dp))

                ProjectDropdown(
                    projects = state.projects,
                    selectedProjectId = selectedProjectId,
                    onProjectSelected = {
                        selectedProjectId = it
                        selectedUserIds = emptySet()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                AppTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = "${language.t("common.start")} dd/mm/aaaa",
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                AppTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = "${language.t("common.deadline")} dd/mm/aaaa",
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = language.t("common.users"),
                    color = ProjectHubColors.Ink,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (availableUsers.isEmpty()) {
                    Text(
                        text = language.t("common.noProjectUsers"),
                        color = ProjectHubColors.Muted,
                        fontSize = 13.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(
                            items = availableUsers,
                            key = { it.id }
                        ) { user ->
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
                    Text(
                        text = it,
                        color = GestorTasksRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            AppDialogConfirmButton(
                text = if (isSaving) {
                    language.t("common.creating")
                } else {
                    language.t("common.create")
                },
                enabled = !isSaving,
                onClick = {
                    onCreate(
                        title,
                        description,
                        selectedProjectId,
                        startDate,
                        endDate,
                        selectedUserIds
                    )
                }
            )
        },
        dismissButton = {
            AppDialogCancelButton(
                text = language.t("common.cancel"),
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun ProjectDropdown(
    projects: List<GestorTaskProjectOption>,
    selectedProjectId: Int?,
    onProjectSelected: (Int) -> Unit
) {
    val language = currentAppSettings().language
    var expanded by remember { mutableStateOf(false) }
    val selected = projects.firstOrNull { it.id == selectedProjectId }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, ProjectHubColors.Border, RoundedCornerShape(8.dp))
                .clickable(onClick = rememberSoundClick { expanded = true })
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selected?.name ?: language.t("addTask.projectPlaceholder"),
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.SemiBold
            )

            AppExpandIcon(expanded = expanded)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ProjectHubColors.LightSurface)
        ) {
            projects.forEach { project ->
                DropdownMenuItem(
                    text = {
                        Text(project.name, color = ProjectHubColors.Ink)
                    },
                    onClick = {
                        expanded = false
                        onProjectSelected(project.id)
                    }
                )
            }
        }
    }
}

