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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.R
import com.example.projecthub.navigation.AppRoutes
import com.example.projecthub.remote.supabase.models.UserDto
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
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
    onNavigate: (String) -> Unit = {},
    viewModel: AdminDashboardViewModel = viewModel()
) {
    val state = viewModel.state
    val language = currentAppSettings().language

    AdminScaffold(
        selectedRoute = selectedRoute,
        onNavigate = onNavigate,
        profilePhotoUri = currentUser?.foto,
        profileName = currentUser?.nome,
        onLogout = onLogout
    ) {
        when (selectedRoute) {
            AppRoutes.AdminDashboard -> {
                DashboardHeader(language = language)
                Spacer(modifier = Modifier.height(22.dp))
                DashboardContent(state = state, language = language)
            }

            AppRoutes.AdminProjects -> AdminProjectsScreen()

            AppRoutes.AdminTasks -> AdminTasksScreen()

            AppRoutes.AdminTeams -> AdminTeamsScreen()

            AppRoutes.AdminReports -> AdminReportsScreen()

            AppRoutes.AdminSettings -> SettingsScreen()

            AppRoutes.AdminProfile -> ProfileScreen(
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
            MetricCard(
                label = language.t("dashboard.state"),
                value = language.t("dashboard.error"),
                accent = AdminRed,
                detail = state.errorMessage,
                icon = DashboardIcon.Warning
            )
        }

        else -> {
            val cards: @Composable (Modifier) -> Unit = { modifier ->
                MetricCard(
                    label = language.t("dashboard.activeUsers"),
                    value = state.activeUsers.toString(),
                    accent = AdminOrange,
                    detail = language.t("dashboard.activeUsersDetail"),
                    icon = DashboardIcon.Users,
                    modifier = modifier
                )
                if (!isLandscape) Spacer(modifier = Modifier.height(12.dp))
                MetricCard(
                    label = language.t("dashboard.completedProjects"),
                    value = state.completedProjects.toString(),
                    accent = AdminAccent,
                    detail = language.t("dashboard.completedProjectsDetail"),
                    icon = DashboardIcon.Completed,
                    modifier = modifier
                )
                if (!isLandscape) Spacer(modifier = Modifier.height(12.dp))
                MetricCard(
                    label = language.t("dashboard.pendingProjects"),
                    value = state.pendingProjects.toString(),
                    accent = AdminRed,
                    detail = language.t("dashboard.pendingProjectsDetail"),
                    icon = DashboardIcon.Pending,
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

private enum class DashboardIcon {
    Completed,
    Users,
    Pending,
    Warning
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    accent: Color,
    detail: String,
    icon: DashboardIcon,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(108.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    MetricIcon(icon = icon, color = accent)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = detail,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        fontSize = 13.sp
                    )
                }
            }

            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 36.sp
            )
        }
    }
}


@Composable
private fun MetricIcon(
    icon: DashboardIcon,
    color: Color
) {
    val iconRes = when (icon) {
        DashboardIcon.Users -> R.drawable.ic_group_24
        DashboardIcon.Warning -> R.drawable.ic_warning_24
        DashboardIcon.Pending -> R.drawable.ic_tasks_24
        DashboardIcon.Completed -> R.drawable.ic_check_circle_24
    }

    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}
