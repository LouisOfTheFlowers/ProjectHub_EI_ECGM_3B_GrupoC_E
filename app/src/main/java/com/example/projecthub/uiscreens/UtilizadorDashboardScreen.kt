package com.example.projecthub.uiscreens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.R
import com.example.projecthub.navigation.AppRoutes
import com.example.projecthub.remote.supabase.models.UserDto
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.viewmodel.UtilizadorDashboardState
import com.example.projecthub.viewmodel.UtilizadorDashboardViewModel

private val UtilizadorAccent = AuthAccent
private val UtilizadorGreen = ProjectHubColors.Success
private val UtilizadorBlue = ProjectHubColors.InfoLight
private val UtilizadorOrange = ProjectHubColors.Warning
private val UtilizadorRed = ProjectHubColors.Danger

@Composable
fun UtilizadorDashboardScreen(
    userId: Int?,
    currentUser: UserDto?,
    onUserUpdated: (UserDto) -> Unit,
    onLogout: () -> Unit,
    selectedRoute: String = AppRoutes.UserDashboard,
    taskObservationsId: Int? = null,
    projectHistoryId: Int? = null,
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: UtilizadorDashboardViewModel = viewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.loadDashboard(userId)
    }

    UtilizadorScaffold(
        selectedRoute = selectedRoute,
        onNavigate = onNavigate,
        profilePhotoUri = currentUser?.foto,
        profileName = currentUser?.nome,
        onLogout = onLogout
    ) {
        when (selectedRoute) {
            AppRoutes.UserDashboard -> {
                UtilizadorDashboardHeader()
                Spacer(modifier = Modifier.height(22.dp))
                UtilizadorDashboardContent(state = state)
            }

            AppRoutes.UserTasks -> UtilizadorTasksSection(
                state = state,
                taskObservationsId = taskObservationsId,
                onOpenObservations = { taskId ->
                    onNavigate(AppRoutes.userTaskObservations(taskId))
                },
                onBack = onBack,
                onAddObservation = { taskId, text, photoUri ->
                    viewModel.addObservation(
                        userId = userId,
                        taskId = taskId,
                        text = text,
                        photoUri = photoUri
                    )
                },
                onCompleteTask = { taskId, date, location, hours ->
                    viewModel.completeTask(
                        userId = userId,
                        taskId = taskId,
                        completionDate = date,
                        location = location,
                        spentHours = hours
                    )
                }
            )

            AppRoutes.UserProjects -> UtilizadorProjectsSection(
                state = state,
                projectHistoryId = projectHistoryId,
                onOpenHistory = { projectId ->
                    onNavigate(AppRoutes.userProjectHistory(projectId))
                },
                onBack = onBack
            )

            AppRoutes.UserSettings -> SettingsScreen()

            AppRoutes.UserProfile -> ProfileScreen(
                user = currentUser,
                onUserUpdated = onUserUpdated,
                onAccountDeleted = onLogout
            )
        }
    }
}

@Composable
private fun UtilizadorDashboardHeader() {
    val language = currentAppSettings().language
    Column {
        Text(
            text = language.t("user.dashboard.title"),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = language.t("user.dashboard.subtitle"),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun UtilizadorDashboardContent(state: UtilizadorDashboardState) {
    val language = currentAppSettings().language
    val isLandscape = isLandscapeLayout()

    when {
        state.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = UtilizadorAccent)
            }
        }

        state.errorMessage != null -> {
            AppDashboardMetricCard(
                metric = AppDashboardMetric(
                    label = language.t("dashboard.state"),
                    value = language.t("dashboard.error"),
                    accent = UtilizadorRed,
                    detail = state.errorMessage.orEmpty(),
                    iconRes = userDashboardIconRes(UserDashboardIcon.Warning)
                )
            )
        }

        else -> {
            val cards: @Composable (Modifier) -> Unit = { modifier ->
                AppDashboardMetricCard(
                    metric = AppDashboardMetric(
                        label = language.t("common.inProgress"),
                        value = state.inProgressTasks.toString(),
                        accent = UtilizadorBlue,
                        detail = language.t("user.dashboard.progressDetail"),
                        iconRes = userDashboardIconRes(UserDashboardIcon.Progress)
                    ),
                    modifier = modifier
                )
                if (!isLandscape) Spacer(modifier = Modifier.height(12.dp))
                AppDashboardMetricCard(
                    metric = AppDashboardMetric(
                        label = language.t("common.completed"),
                        value = state.completedTasks.toString(),
                        accent = UtilizadorGreen,
                        detail = language.t("user.dashboard.completedDetail"),
                        iconRes = userDashboardIconRes(UserDashboardIcon.Completed)
                    ),
                    modifier = modifier
                )
                if (!isLandscape) Spacer(modifier = Modifier.height(12.dp))
                AppDashboardMetricCard(
                    metric = AppDashboardMetric(
                        label = language.t("common.pending"),
                        value = state.pendingTasks.toString(),
                        accent = UtilizadorOrange,
                        detail = language.t("user.dashboard.pendingDetail"),
                        iconRes = userDashboardIconRes(UserDashboardIcon.Pending)
                    ),
                    modifier = modifier
                )
                if (!isLandscape) Spacer(modifier = Modifier.height(12.dp))
                AppDashboardMetricCard(
                    metric = AppDashboardMetric(
                        label = language.t("common.delayed"),
                        value = state.lateTasks.toString(),
                        accent = UtilizadorRed,
                        detail = language.t("user.dashboard.lateDetail"),
                        iconRes = userDashboardIconRes(UserDashboardIcon.Late)
                    ),
                    modifier = modifier
                )
            }

            if (isLandscape) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    cards(Modifier.weight(1f))
                }
            } else {
                cards(Modifier.fillMaxWidth())
            }
        }
    }
}

private fun userDashboardIconRes(icon: UserDashboardIcon): Int {
    return when (icon) {
        UserDashboardIcon.Late -> R.drawable.ic_warning_24
        UserDashboardIcon.Pending -> R.drawable.ic_tasks_24
        UserDashboardIcon.Completed -> R.drawable.ic_check_circle_24
        UserDashboardIcon.Progress -> R.drawable.ic_trending_up_24
        UserDashboardIcon.Warning -> R.drawable.ic_warning_24
    }
}

@Composable
private fun UtilizadorPlaceholderSection(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )
}

private enum class UserDashboardIcon {
    Progress,
    Completed,
    Pending,
    Late,
    Warning
}
