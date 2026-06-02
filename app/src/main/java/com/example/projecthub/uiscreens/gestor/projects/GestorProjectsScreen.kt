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
fun GestorProjectsScreen(
    gestorId: Int?,
    viewModel: GestorProjectsViewModel = viewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val language = currentAppSettings().language

    var projectToAssociate by remember { mutableStateOf<GestorProjectListItem?>(null) }
    var projectToComplete by remember { mutableStateOf<GestorProjectListItem?>(null) }
    var projectToView by remember { mutableStateOf<GestorProjectListItem?>(null) }

    LaunchedEffect(gestorId) {
        viewModel.loadProjects(gestorId)
    }

    LaunchedEffect(state.actionMessage) {
        if (state.actionMessage != null && projectToComplete != null) {
            projectToComplete = null
        }
    }

    projectToView?.let { project ->
        LaunchedEffect(project.id) {
            viewModel.loadProjectInfo(project)
        }

        GestorProjectInfoPage(
            state = state.detailState,
            onBack = {
                viewModel.clearProjectInfo()
                projectToView = null
            }
        )
        return
    }

    Column {
        Text(
            text = language.t("manager.projects.title"),
            color = ProjectHubColors.Ink,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )

        Text(
            text = language.t("manager.projects.subtitle"),
            color = ProjectHubColors.Muted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        GestorProjectFilters(
            state = state,
            onSearchChange = viewModel::updateSearchQuery,
            onStatusChange = viewModel::updateStatusFilter
        )

        Spacer(modifier = Modifier.height(18.dp))

        GestorProjectList(
            state = state,
            onToggleProject = viewModel::toggleProject,
            onAssociateUser = { project ->
                viewModel.clearMessages()
                projectToAssociate = project
            },
            onCompleteProject = { project ->
                viewModel.clearMessages()
                projectToComplete = project
            },
            onMoreInfo = { project ->
                viewModel.clearMessages()
                projectToView = project
            }
        )
    }

    projectToAssociate?.let { project ->
        GestorAssociateUserDialog(
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
        GestorCompleteProjectDialog(
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
            }
        )
    }
}
