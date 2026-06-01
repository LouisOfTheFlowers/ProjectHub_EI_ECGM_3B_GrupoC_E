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
import androidx.compose.ui.geometry.Size
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
import com.example.projecthub.viewmodel.GestorDashboardState
import com.example.projecthub.viewmodel.GestorDashboardViewModel

private val GestorAccent = AuthAccent
private val GestorOrange = ProjectHubColors.Warning
private val GestorRed = ProjectHubColors.Danger

@Composable
fun GestorDashboardScreen(
    gestorId: Int?,
    currentUser: UserDto?,
    onUserUpdated: (UserDto) -> Unit,
    onLogout: () -> Unit,
    selectedRoute: String = AppRoutes.GestorDashboard,
    hasInternet: Boolean = true,
    onNavigate: (String) -> Unit = {},
    viewModel: GestorDashboardViewModel = viewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(gestorId, selectedRoute, hasInternet) {
        if (hasInternet && selectedRoute == AppRoutes.GestorDashboard) {
            viewModel.loadDashboard(gestorId)
        }
    }

    GestorScaffold(
        selectedRoute = selectedRoute,
        onNavigate = onNavigate,
        profilePhotoUri = currentUser?.foto,
        profileName = currentUser?.nome,
        onLogout = onLogout
    ) {
        when {
            !hasInternet && selectedRoute != AppRoutes.GestorProfile -> AppOfflineState()

            selectedRoute == AppRoutes.GestorDashboard -> {
                DashboardHeader()
                Spacer(modifier = Modifier.height(22.dp))
                DashboardContent(state = state)
            }

            selectedRoute == AppRoutes.GestorProjects -> GestorProjectsScreen(gestorId = gestorId)

            selectedRoute == AppRoutes.GestorTasks -> GestorTasksScreen(gestorId = gestorId)

            selectedRoute == AppRoutes.GestorTeam -> GestorTeamScreen(gestorId = gestorId)

            selectedRoute == AppRoutes.GestorReports -> GestorReportsScreen(gestorId = gestorId)

            selectedRoute == AppRoutes.GestorSettings -> SettingsScreen()

            selectedRoute == AppRoutes.GestorProfile -> ProfileScreen(
                user = currentUser,
                onUserUpdated = onUserUpdated,
                onAccountDeleted = onLogout
            )
        }
    }
}

@Composable
private fun PlaceholderSection(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )
}

@Composable
private fun DashboardHeader() {
    val language = currentAppSettings().language
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = language.t("manager.dashboard.title"),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun DashboardContent(state: GestorDashboardState) {
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
                CircularProgressIndicator(color = GestorAccent)
            }
        }

        state.errorMessage != null -> {
            AppDashboardMetricCard(
                metric = AppDashboardMetric(
                    label = language.t("dashboard.state"),
                    value = language.t("dashboard.error"),
                    accent = GestorRed,
                    detail = state.errorMessage.orEmpty(),
                    iconRes = gestorDashboardIconRes(GestorDashboardIcon.Warning)
                )
            )
        }

        else -> {
            val cards: @Composable (Modifier) -> Unit = { modifier ->
                AppDashboardMetricCard(
                    metric = AppDashboardMetric(
                        label = language.t("manager.dashboard.projects"),
                        value = state.totalProjects.toString(),
                        accent = GestorOrange,
                        detail = language.t("manager.dashboard.projectsDetail"),
                        iconRes = gestorDashboardIconRes(GestorDashboardIcon.Projects)
                    ),
                    modifier = modifier
                )
                if (!isLandscape) Spacer(modifier = Modifier.height(12.dp))
                AppDashboardMetricCard(
                    metric = AppDashboardMetric(
                        label = language.t("common.completed"),
                        value = state.completedTasks.toString(),
                        accent = GestorAccent,
                        detail = language.t("manager.dashboard.completedDetail"),
                        iconRes = gestorDashboardIconRes(GestorDashboardIcon.Completed)
                    ),
                    modifier = modifier
                )
                if (!isLandscape) Spacer(modifier = Modifier.height(12.dp))
                AppDashboardMetricCard(
                    metric = AppDashboardMetric(
                        label = language.t("common.inProgress"),
                        value = state.inProgressTasks.toString(),
                        accent = GestorRed,
                        detail = language.t("manager.dashboard.progressDetail"),
                        iconRes = gestorDashboardIconRes(GestorDashboardIcon.Pending)
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

private fun gestorDashboardIconRes(icon: GestorDashboardIcon): Int {
    return when (icon) {
        GestorDashboardIcon.Projects -> R.drawable.ic_folder_24
        GestorDashboardIcon.Warning -> R.drawable.ic_warning_24
        GestorDashboardIcon.Pending -> R.drawable.ic_tasks_24
        GestorDashboardIcon.Completed -> R.drawable.ic_check_circle_24
    }
}

private enum class GestorDashboardIcon {
    Completed,
    Projects,
    Pending,
    Warning
}
