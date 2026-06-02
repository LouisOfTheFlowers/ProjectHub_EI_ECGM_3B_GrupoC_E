package com.example.projecthub.uiscreens.admin

import com.example.projecthub.uiscreens.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.R
import com.example.projecthub.navigation.AppRoutes
import com.example.projecthub.remote.supabase.models.UserDto
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.admin.projects.AdminProjectsScreen
import com.example.projecthub.uiscreens.admin.reports.AdminReportsScreen
import com.example.projecthub.uiscreens.admin.tasks.AdminTasksScreen
import com.example.projecthub.uiscreens.admin.teams.AdminTeamsScreen
import com.example.projecthub.viewmodel.AdminDashboardState
import com.example.projecthub.viewmodel.AdminDashboardViewModel

private val AdminAccent = AuthAccent
private val AdminOrange = ProjectHubColors.Warning
private val AdminRed = ProjectHubColors.Danger

@Composable
fun AdminDashboardScreen(
    currentUser: UserDto?,
    onUserUpdated: (UserDto) -> Unit,
    onLogout: () -> Unit,
    selectedRoute: String = AppRoutes.AdminDashboard,
    hasInternet: Boolean = true,
    onNavigate: (String) -> Unit = {},
    viewModel: AdminDashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = currentAppSettings().language

    LaunchedEffect(selectedRoute, hasInternet) {
        if (hasInternet && selectedRoute == AppRoutes.AdminDashboard) {
            viewModel.loadDashboard()
        }
    }

    AdminScaffold(
        selectedRoute = selectedRoute,
        onNavigate = onNavigate,
        profilePhotoUri = currentUser?.foto,
        profileName = currentUser?.nome,
        onLogout = onLogout
    ) {
        when {
            !hasInternet && selectedRoute != AppRoutes.AdminProfile -> AppOfflineState()

            selectedRoute == AppRoutes.AdminDashboard -> {
                DashboardHeader(language = language)
                Spacer(modifier = Modifier.height(22.dp))
                DashboardContent(state = state, language = language)
            }

            selectedRoute == AppRoutes.AdminProjects -> AdminProjectsScreen()

            selectedRoute == AppRoutes.AdminTasks -> AdminTasksScreen()

            selectedRoute == AppRoutes.AdminTeams -> AdminTeamsScreen()

            selectedRoute == AppRoutes.AdminReports -> AdminReportsScreen()

            selectedRoute == AppRoutes.AdminSettings -> SettingsScreen()

            selectedRoute == AppRoutes.AdminProfile -> ProfileScreen(
                user = currentUser,
                onUserUpdated = onUserUpdated,
                onAccountDeleted = onLogout
            )
        }
    }
}

@Composable
private fun DashboardHeader(language: AppLanguage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = language.t("dashboard.title"),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: AdminDashboardState,
    language: AppLanguage
) {
    val isLandscape = isLandscapeLayout()

    when {
        state.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AdminAccent)
            }
        }

        state.errorMessage != null -> {
            AppDashboardMetricCard(
                metric = AppDashboardMetric(
                    label = language.t("dashboard.state"),
                    value = language.t("dashboard.error"),
                    accent = AdminRed,
                    detail = state.errorMessage.orEmpty(),
                    iconRes = dashboardIconRes(DashboardIcon.Warning)
                )
            )
        }

        else -> {
            val cards: @Composable (Modifier) -> Unit = { modifier ->
                AppDashboardMetricCard(
                    metric = AppDashboardMetric(
                        label = language.t("dashboard.activeUsers"),
                        value = state.activeUsers.toString(),
                        accent = AdminOrange,
                        detail = language.t("dashboard.activeUsersDetail"),
                        iconRes = dashboardIconRes(DashboardIcon.Users)
                    ),
                    modifier = modifier
                )
                if (!isLandscape) Spacer(modifier = Modifier.height(12.dp))
                AppDashboardMetricCard(
                    metric = AppDashboardMetric(
                        label = language.t("dashboard.completedProjects"),
                        value = state.completedProjects.toString(),
                        accent = AdminAccent,
                        detail = language.t("dashboard.completedProjectsDetail"),
                        iconRes = dashboardIconRes(DashboardIcon.Completed)
                    ),
                    modifier = modifier
                )
                if (!isLandscape) Spacer(modifier = Modifier.height(12.dp))
                AppDashboardMetricCard(
                    metric = AppDashboardMetric(
                        label = language.t("dashboard.pendingProjects"),
                        value = state.pendingProjects.toString(),
                        accent = AdminRed,
                        detail = language.t("dashboard.pendingProjectsDetail"),
                        iconRes = dashboardIconRes(DashboardIcon.Pending)
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

private fun dashboardIconRes(icon: DashboardIcon): Int {
    return when (icon) {
        DashboardIcon.Users -> R.drawable.ic_group_24
        DashboardIcon.Warning -> R.drawable.ic_warning_24
        DashboardIcon.Pending -> R.drawable.ic_tasks_24
        DashboardIcon.Completed -> R.drawable.ic_check_circle_24
    }
}

private enum class DashboardIcon {
    Completed,
    Users,
    Pending,
    Warning
}
