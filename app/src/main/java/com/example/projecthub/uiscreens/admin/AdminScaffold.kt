package com.example.projecthub.uiscreens.admin

import com.example.projecthub.uiscreens.*

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.projecthub.R
import com.example.projecthub.navigation.AppRoutes
import com.example.projecthub.navigation.SidebarDestination
import com.example.projecthub.remote.supabase.models.NotificationDto
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.components.AppNotificationsMenu

private val AdminScaffoldAccent = AuthAccent

@Composable
fun AdminScaffold(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    profilePhotoUri: String?,
    profileName: String?,
    notifications: List<NotificationDto> = emptyList(),
    unreadNotificationsCount: Int = 0,
    notificationsLoading: Boolean = false,
    notificationsError: String? = null,
    onNotificationClick: (NotificationDto) -> Unit = {},
    onMarkAllNotificationsRead: () -> Unit = {},
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    var isSidebarOpen by remember { mutableStateOf(false) }
    val openSidebar = rememberSoundClick { isSidebarOpen = true }
    val layout = appResponsiveLayout()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AdminMainContent(
            modifier = Modifier.fillMaxSize(),
            showMenu = true,
            topBarHeight = layout.topBarHeight,
            contentPadding = layout.contentPadding,
            onMenuClick = openSidebar,
            profilePhotoUri = profilePhotoUri,
            profileName = profileName,
            notifications = notifications,
            unreadNotificationsCount = unreadNotificationsCount,
            notificationsLoading = notificationsLoading,
            notificationsError = notificationsError,
            onNotificationClick = onNotificationClick,
            onMarkAllNotificationsRead = onMarkAllNotificationsRead,
            onProfileClick = { onNavigate(AppRoutes.AdminProfile) },
            content = content
        )

        if (isSidebarOpen) {
            SidebarOverlay(
                selectedSection = selectedRoute,
                sidebarWidth = layout.sidebarWidth,
                onDismiss = { isSidebarOpen = false },
                onNavigate = { section ->
                    onNavigate(section)
                    isSidebarOpen = false
                },
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun AdminMainContent(
    modifier: Modifier,
    showMenu: Boolean,
    topBarHeight: androidx.compose.ui.unit.Dp,
    contentPadding: PaddingValues,
    onMenuClick: () -> Unit,
    profilePhotoUri: String?,
    profileName: String?,
    notifications: List<NotificationDto>,
    unreadNotificationsCount: Int,
    notificationsLoading: Boolean,
    notificationsError: String?,
    onNotificationClick: (NotificationDto) -> Unit,
    onMarkAllNotificationsRead: () -> Unit,
    onProfileClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .border(5.dp, ProjectHubColors.HeaderBackground)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AdminTopBar(
            showMenu = showMenu,
            height = topBarHeight,
            onMenuClick = onMenuClick,
            profilePhotoUri = profilePhotoUri,
            profileName = profileName,
            notifications = notifications,
            unreadNotificationsCount = unreadNotificationsCount,
            notificationsLoading = notificationsLoading,
            notificationsError = notificationsError,
            onNotificationClick = onNotificationClick,
            onMarkAllNotificationsRead = onMarkAllNotificationsRead,
            onProfileClick = onProfileClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = contentPadding
        ) {
            item {
                content()
            }
        }
    }
}

@Composable
private fun AdminTopBar(
    showMenu: Boolean,
    height: androidx.compose.ui.unit.Dp,
    onMenuClick: () -> Unit,
    profilePhotoUri: String?,
    profileName: String?,
    notifications: List<NotificationDto>,
    unreadNotificationsCount: Int,
    notificationsLoading: Boolean,
    notificationsError: String?,
    onNotificationClick: (NotificationDto) -> Unit,
    onMarkAllNotificationsRead: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ProjectHubColors.HeaderBackground)
            .statusBarsPadding()
            .height(height)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showMenu) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onMenuClick),
                contentAlignment = Alignment.Center
            ) {
                MenuIcon(color = ProjectHubColors.HeaderContent)
            }
        }

        Text(
            text = "Project Hub",
            color = ProjectHubColors.HeaderContent,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 21.sp
        )

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppNotificationsMenu(
                notifications = notifications,
                unreadCount = unreadNotificationsCount,
                isLoading = notificationsLoading,
                errorMessage = notificationsError,
                onNotificationClick = onNotificationClick,
                onMarkAllRead = onMarkAllNotificationsRead
            )

            Spacer(modifier = Modifier.width(8.dp))

            TopBarProfilePhoto(
                photoUri = profilePhotoUri,
                name = profileName,
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onProfileClick)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun SidebarOverlay(
    selectedSection: String,
    sidebarWidth: androidx.compose.ui.unit.Dp,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val dismiss = rememberSoundClick(onDismiss)
    Row(modifier = Modifier.fillMaxSize()) {
        AdminSidebar(
            selectedSection = selectedSection,
            onNavigate = onNavigate,
            onLogout = onLogout,
            sidebarWidth = sidebarWidth
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(onClick = dismiss)
        )
    }
}

@Composable
private fun AdminSidebar(
    selectedSection: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    sidebarWidth: androidx.compose.ui.unit.Dp = 284.dp
) {
    val language = currentAppSettings().language
    val isLandscape = isLandscapeLayout()
    val destinations = listOf(
        SidebarDestination(AppRoutes.AdminDashboard, language.t("sidebar.dashboard"), SidebarIcon.Dashboard),
        SidebarDestination(AppRoutes.AdminProjects, language.t("sidebar.projects"), SidebarIcon.Projects),
        SidebarDestination(AppRoutes.AdminTasks, language.t("sidebar.tasks"), SidebarIcon.Tasks),
        SidebarDestination(AppRoutes.AdminTeams, language.t("sidebar.teams"), SidebarIcon.Teams),
        SidebarDestination(AppRoutes.AdminReports, language.t("sidebar.reports"), SidebarIcon.Reports),
        SidebarDestination(AppRoutes.AdminSettings, language.t("sidebar.settings"), SidebarIcon.Settings)
    )
    Column(
        modifier = Modifier
            .width(sidebarWidth)
            .fillMaxHeight()
            .background(ProjectHubColors.SidebarBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = if (isLandscape) 12.dp else 22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.projecthub_logo),
                contentDescription = "Project Hub",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ProjectHubColors.LightSurface)
                    .padding(5.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Project Hub",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Text(
                    text = language.t("role.admin"),
                    color = ProjectHubColors.SidebarMutedText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 12.dp else 24.dp))

        destinations.forEach { destination ->
            SidebarItem(
                destination.label,
                destination.icon,
                selected = selectedSection == destination.route,
                onClick = { onNavigate(destination.route) }
            )
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 12.dp else 24.dp))

        SidebarItem(language.t("sidebar.logout"), SidebarIcon.Logout, onClick = onLogout)
    }
}

@Composable
private fun SidebarItem(
    label: String,
    icon: SidebarIcon,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val click = rememberSoundClick(onClick)
    val itemHeight = if (isLandscapeLayout()) 46.dp else 52.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) ProjectHubColors.SidebarSelected else Color.Transparent)
            .clickable(onClick = click)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SidebarItemIcon(icon = icon, color = Color.White)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
    }

    Spacer(modifier = Modifier.height(if (isLandscapeLayout()) 4.dp else 8.dp))
}

private enum class SidebarIcon {
    Dashboard,
    Projects,
    Tasks,
    Teams,
    Reports,
    Settings,
    Logout
}

@Composable
private fun SidebarItemIcon(
    icon: SidebarIcon,
    color: Color
) {
    val iconRes = when (icon) {
        SidebarIcon.Dashboard -> R.drawable.ic_dashboard_24
        SidebarIcon.Projects -> R.drawable.ic_folder_24
        SidebarIcon.Teams -> R.drawable.ic_group_24
        SidebarIcon.Tasks -> R.drawable.ic_tasks_24
        SidebarIcon.Logout -> R.drawable.ic_logout_24
        SidebarIcon.Settings -> R.drawable.ic_settings_24
        SidebarIcon.Reports -> R.drawable.ic_bar_chart_24
    }

    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}
@Composable
private fun MenuIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Icon(
        painter = painterResource(R.drawable.ic_menu_24),
        contentDescription = null,
        tint = color,
        modifier = modifier.size(28.dp)
    )
}

