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
    onNavigate: (String) -> Unit = {},
    viewModel: UtilizadorDashboardViewModel = viewModel()
) {
    val state = viewModel.state

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

            AppRoutes.UserProjects -> UtilizadorProjectsSection(state = state)

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
    Column {
        Text(
            text = "Utilizador Dashboard",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Resumo das tuas tarefas atribuídas",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun UtilizadorDashboardContent(state: UtilizadorDashboardState) {
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
            UserMetricCard(
                label = "Estado da Dashboard",
                value = "Erro",
                accent = UtilizadorRed,
                detail = state.errorMessage,
                icon = UserDashboardIcon.Warning
            )
        }

        else -> {
            UserMetricCard(
                label = "Tarefas em progresso",
                value = state.inProgressTasks.toString(),
                accent = UtilizadorBlue,
                detail = "Tarefas que já começaram",
                icon = UserDashboardIcon.Progress
            )
            Spacer(modifier = Modifier.height(12.dp))
            UserMetricCard(
                label = "Tarefas concluídas",
                value = state.completedTasks.toString(),
                accent = UtilizadorGreen,
                detail = "Tarefas finalizadas por ti",
                icon = UserDashboardIcon.Completed
            )
            Spacer(modifier = Modifier.height(12.dp))
            UserMetricCard(
                label = "Tarefas pendentes",
                value = state.pendingTasks.toString(),
                accent = UtilizadorOrange,
                detail = "Tarefas ainda por iniciar",
                icon = UserDashboardIcon.Pending
            )
            Spacer(modifier = Modifier.height(12.dp))
            UserMetricCard(
                label = "Tarefas em atraso",
                value = state.lateTasks.toString(),
                accent = UtilizadorRed,
                detail = "Tarefas cujo prazo já passou",
                icon = UserDashboardIcon.Late
            )
        }
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

@Composable
private fun UserMetricCard(
    label: String,
    value: String,
    accent: Color,
    detail: String,
    icon: UserDashboardIcon
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
                    UserMetricIcon(icon = icon, color = accent)
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
private fun UserMetricIcon(
    icon: UserDashboardIcon,
    color: Color
) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)

        when (icon) {
            UserDashboardIcon.Progress -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.18f, size.height * 0.2f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.6f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = stroke
                )
                drawLine(color = color, start = Offset(size.width * 0.34f, size.height * 0.42f), end = Offset(size.width * 0.7f, size.height * 0.42f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(size.width * 0.34f, size.height * 0.6f), end = Offset(size.width * 0.58f, size.height * 0.6f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            UserDashboardIcon.Completed -> {
                drawCircle(color = color, style = stroke)
                drawLine(color = color, start = Offset(size.width * 0.28f, size.height * 0.52f), end = Offset(size.width * 0.44f, size.height * 0.68f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(size.width * 0.44f, size.height * 0.68f), end = Offset(size.width * 0.74f, size.height * 0.34f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            UserDashboardIcon.Pending -> {
                drawCircle(color = color, radius = size.width * 0.36f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
                drawLine(color = color, start = Offset(size.width * 0.5f, size.height * 0.28f), end = Offset(size.width * 0.5f, size.height * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(size.width * 0.5f, size.height * 0.52f), end = Offset(size.width * 0.64f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            UserDashboardIcon.Late,
            UserDashboardIcon.Warning -> {
                drawLine(color = color, start = Offset(size.width * 0.5f, size.height * 0.16f), end = Offset(size.width * 0.12f, size.height * 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(size.width * 0.5f, size.height * 0.16f), end = Offset(size.width * 0.88f, size.height * 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(size.width * 0.12f, size.height * 0.82f), end = Offset(size.width * 0.88f, size.height * 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(size.width * 0.5f, size.height * 0.38f), end = Offset(size.width * 0.5f, size.height * 0.58f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(color = color, radius = 1.5.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.7f))
            }
        }
    }
}
