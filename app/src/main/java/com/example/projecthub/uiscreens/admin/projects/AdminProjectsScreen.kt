package com.example.projecthub.uiscreens.admin.projects

import com.example.projecthub.uiscreens.*

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.AdminProjectListItem
import com.example.projecthub.viewmodel.AdminProjectsViewModel

@Composable
fun AdminProjectsScreen(
    viewModel: AdminProjectsViewModel = viewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
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
    } else if (state.detailState.project != null) {
        AdminProjectDetailPage(
            state = state.detailState,
            onBack = viewModel::clearProjectInfo
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
            onToggleProject = viewModel::toggleProject,
            onMoreInfo = viewModel::loadProjectInfo,
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
                AppDialogConfirmButton(
                    text = language.t("common.delete"),
                    onClick = {
                        viewModel.deleteProject(project.id)
                        projectToDelete = null
                    }
                )
            },
            dismissButton = {
                AppDialogCancelButton(
                    text = language.t("common.cancel"),
                    onClick = { projectToDelete = null }
                )
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
