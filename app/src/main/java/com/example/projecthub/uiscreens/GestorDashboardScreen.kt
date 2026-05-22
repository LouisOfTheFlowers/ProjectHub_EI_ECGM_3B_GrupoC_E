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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.projecthub.navigation.AppRoutes
import com.example.projecthub.remote.supabase.models.UserDto
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
    onNavigate: (String) -> Unit = {},
    viewModel: GestorDashboardViewModel = viewModel()
) {
    val state = viewModel.state

    LaunchedEffect(gestorId) {
        viewModel.loadDashboard(gestorId)
    }

    GestorScaffold(
        selectedRoute = selectedRoute,
        onNavigate = onNavigate,
        profilePhotoUri = currentUser?.foto,
        profileName = currentUser?.nome,
        onLogout = onLogout
    ) {
        when (selectedRoute) {
            AppRoutes.GestorDashboard -> {
                DashboardHeader()
                Spacer(modifier = Modifier.height(22.dp))
                DashboardContent(state = state)
            }

            AppRoutes.GestorProjects -> GestorProjectsScreen(gestorId = gestorId)

            AppRoutes.GestorTasks -> GestorTasksScreen(gestorId = gestorId)

            AppRoutes.GestorTeam -> GestorTeamScreen(gestorId = gestorId)

            AppRoutes.GestorReports -> GestorReportsScreen(gestorId = gestorId)

            AppRoutes.GestorSettings -> SettingsScreen()

            AppRoutes.GestorProfile -> ProfileScreen(
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Gestor Dashboard",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun DashboardContent(state: GestorDashboardState) {
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
            MetricCard(
                label = "Estado da Dashboard",
                value = "Erro",
                accent = GestorRed,
                detail = state.errorMessage,
                icon = GestorDashboardIcon.Warning
            )
        }

        else -> {
            MetricCard(
                label = "Projetos do gestor",
                value = state.totalProjects.toString(),
                accent = GestorOrange,
                detail = "Projetos atribuídos ao gestor",
                icon = GestorDashboardIcon.Projects
            )
            Spacer(modifier = Modifier.height(12.dp))
            MetricCard(
                label = "Tarefas concluídas",
                value = state.completedTasks.toString(),
                accent = GestorAccent,
                detail = "Tarefas finalizadas nos seus projetos",
                icon = GestorDashboardIcon.Completed
            )
            Spacer(modifier = Modifier.height(12.dp))
            MetricCard(
                label = "Tarefas em progresso",
                value = state.inProgressTasks.toString(),
                accent = GestorRed,
                detail = "Tarefas atualmente em desenvolvimento",
                icon = GestorDashboardIcon.Pending
            )
        }
    }
}

private enum class GestorDashboardIcon {
    Completed,
    Projects,
    Pending,
    Warning
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    accent: Color,
    detail: String,
    icon: GestorDashboardIcon
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
    icon: GestorDashboardIcon,
    color: Color
) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)

        when (icon) {
            GestorDashboardIcon.Completed -> {
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

            GestorDashboardIcon.Projects -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.14f, size.height * 0.28f),
                    size = Size(size.width * 0.72f, size.height * 0.5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = stroke
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.26f, size.height * 0.28f),
                    end = Offset(size.width * 0.34f, size.height * 0.16f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.34f, size.height * 0.16f),
                    end = Offset(size.width * 0.5f, size.height * 0.16f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }

            GestorDashboardIcon.Pending -> {
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

            GestorDashboardIcon.Warning -> {
                drawLine(color = color, start = Offset(size.width * 0.5f, size.height * 0.16f), end = Offset(size.width * 0.12f, size.height * 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(size.width * 0.5f, size.height * 0.16f), end = Offset(size.width * 0.88f, size.height * 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(size.width * 0.12f, size.height * 0.82f), end = Offset(size.width * 0.88f, size.height * 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(size.width * 0.5f, size.height * 0.38f), end = Offset(size.width * 0.5f, size.height * 0.58f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(color = color, radius = 1.5.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.7f))
            }
        }
    }
}
