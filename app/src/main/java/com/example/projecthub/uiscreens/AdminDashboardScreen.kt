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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.remote.supabase.models.UserDto
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.AdminDashboardState
import com.example.projecthub.viewmodel.AdminDashboardViewModel

private val AdminAccent = AuthAccent
private val AdminInk = Color(0xFF111827)
private val AdminMuted = Color(0xFF6B7280)
private val AdminOrange = Color(0xFFF97316)
private val AdminRed = Color(0xFFEF4444)

@Composable
fun AdminDashboardScreen(
    currentUser: UserDto?,
    onUserUpdated: (UserDto) -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminDashboardViewModel = viewModel()
) {
    val state = viewModel.state
    var currentSection by remember { mutableStateOf(AdminSection.Dashboard) }
    val language = currentAppSettings().language

    AdminScaffold(
        selectedSection = currentSection,
        onNavigate = { currentSection = it },
        profilePhotoUri = currentUser?.foto,
        profileName = currentUser?.nome,
        onLogout = onLogout
    ) {
        when (currentSection) {
            AdminSection.Dashboard -> {
                DashboardHeader(language = language)
                Spacer(modifier = Modifier.height(22.dp))
                DashboardContent(state = state, language = language)
            }

            AdminSection.Projects -> AdminProjectsScreen()

            AdminSection.Tasks -> AdminTasksScreen()

            AdminSection.Teams -> AdminTeamsScreen()

            AdminSection.Reports -> AdminReportsScreen()

            AdminSection.Settings -> SettingsScreen()

            AdminSection.Profile -> ProfileScreen(
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
            MetricCard(
                label = language.t("dashboard.activeUsers"),
                value = state.activeUsers.toString(),
                accent = AdminOrange,
                detail = language.t("dashboard.activeUsersDetail"),
                icon = DashboardIcon.Users
            )
            Spacer(modifier = Modifier.height(12.dp))
            MetricCard(
                label = language.t("dashboard.completedProjects"),
                value = state.completedProjects.toString(),
                accent = AdminAccent,
                detail = language.t("dashboard.completedProjectsDetail"),
                icon = DashboardIcon.Completed
            )
            Spacer(modifier = Modifier.height(12.dp))
            MetricCard(
                label = language.t("dashboard.pendingProjects"),
                value = state.pendingProjects.toString(),
                accent = AdminRed,
                detail = language.t("dashboard.pendingProjectsDetail"),
                icon = DashboardIcon.Pending
            )
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
    icon: DashboardIcon
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)

        when (icon) {
            DashboardIcon.Completed -> {
                drawCircle(color = color, style = stroke)
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.28f, size.height * 0.52f),
                    end = Offset(size.width * 0.44f, size.height * 0.68f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.44f, size.height * 0.68f),
                    end = Offset(size.width * 0.74f, size.height * 0.34f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }

            DashboardIcon.Users -> {
                drawCircle(
                    color = color,
                    radius = size.width * 0.16f,
                    center = Offset(size.width * 0.42f, size.height * 0.36f),
                    style = stroke
                )
                drawArc(
                    color = color,
                    startAngle = 205f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.2f, size.height * 0.48f),
                    size = Size(size.width * 0.45f, size.height * 0.34f),
                    style = stroke
                )
                drawCircle(
                    color = color,
                    radius = size.width * 0.12f,
                    center = Offset(size.width * 0.66f, size.height * 0.42f),
                    style = stroke
                )
                drawArc(
                    color = color,
                    startAngle = 215f,
                    sweepAngle = 110f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.52f, size.height * 0.55f),
                    size = Size(size.width * 0.34f, size.height * 0.24f),
                    style = stroke
                )
            }

            DashboardIcon.Pending -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.22f, size.height * 0.2f),
                    size = Size(size.width * 0.56f, size.height * 0.64f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = stroke
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.35f, size.height * 0.4f),
                    end = Offset(size.width * 0.65f, size.height * 0.4f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.35f, size.height * 0.58f),
                    end = Offset(size.width * 0.58f, size.height * 0.58f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }

            DashboardIcon.Warning -> {
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.5f, size.height * 0.16f),
                    end = Offset(size.width * 0.12f, size.height * 0.82f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.5f, size.height * 0.16f),
                    end = Offset(size.width * 0.88f, size.height * 0.82f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.12f, size.height * 0.82f),
                    end = Offset(size.width * 0.88f, size.height * 0.82f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.5f, size.height * 0.38f),
                    end = Offset(size.width * 0.5f, size.height * 0.58f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = color,
                    radius = 1.5.dp.toPx(),
                    center = Offset(size.width * 0.5f, size.height * 0.7f)
                )
            }
        }
    }
}
