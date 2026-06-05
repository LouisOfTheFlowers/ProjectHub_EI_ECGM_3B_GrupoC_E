package com.example.projecthub

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projecthub.navigation.AppRoutes
import com.example.projecthub.remote.supabase.models.NotificationDto
import com.example.projecthub.settings.AppNotificationHelper
import com.example.projecthub.settings.AppSettingsProvider
import com.example.projecthub.settings.AppThemeMode
import com.example.projecthub.ui.theme.ProjectHubTheme
import com.example.projecthub.uiscreens.admin.AdminDashboardScreen
import com.example.projecthub.uiscreens.EmailConfirmedScreen
import com.example.projecthub.uiscreens.gestor.GestorDashboardScreen
import com.example.projecthub.uiscreens.IntroSliderScreen
import com.example.projecthub.uiscreens.LoginScreen
import com.example.projecthub.uiscreens.RegisterScreen
import com.example.projecthub.uiscreens.ResetPasswordScreen
import com.example.projecthub.uiscreens.utilizador.UtilizadorDashboardScreen
import com.example.projecthub.viewmodel.AuthViewModel
import com.example.projecthub.viewmodel.NotificationsState
import com.example.projecthub.viewmodel.NotificationsViewModel
import com.example.projecthub.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    private var passwordRecoveryIntent by mutableStateOf<Intent?>(null)
    private var emailConfirmationIntent by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        passwordRecoveryIntent = intent.takeIf { it.isPasswordRecoveryIntent() }
        emailConfirmationIntent = intent.takeIf { it.isEmailConfirmationIntent() }

        window.setBackgroundDrawable(ColorDrawable(AndroidColor.rgb(232, 249, 252)))
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.BLACK),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.BLACK)
        )
        window.attributes = window.attributes.apply {
            alpha = 1f
        }

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (settings.themeMode) {
                AppThemeMode.Light -> false
                AppThemeMode.Dark -> true
                AppThemeMode.System -> systemDark
            }

            LaunchedEffect(settings.notificationsEnabled) {
                if (settings.notificationsEnabled) {
                    AppNotificationHelper.createChannels(this@MainActivity)
                    requestNotificationPermissionIfNeeded()
                }
            }

            ProjectHubTheme(
                darkTheme = darkTheme,
                dynamicColor = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppSettingsProvider(settings = settings) {
                        val authViewModel: AuthViewModel = viewModel()
                        val authState by authViewModel.state.collectAsStateWithLifecycle()
                        val notificationsViewModel: NotificationsViewModel = viewModel()
                        val notificationsState by notificationsViewModel.state.collectAsStateWithLifecycle()
                        val notificationsStateHolder = rememberUpdatedState(notificationsState)
                        val authStateHolder = rememberUpdatedState(authState)
                        val navController = rememberNavController()
                        val hasInternet = rememberHasInternet()
                        val hasInternetState = rememberUpdatedState(hasInternet)

                        LaunchedEffect(authState.currentUser?.id, hasInternet) {
                            if (hasInternet) {
                                notificationsViewModel.loadNotifications(authState.currentUser?.id)
                            }
                        }

                        LaunchedEffect(Unit) {
                            if (passwordRecoveryIntent != null || emailConfirmationIntent != null) {
                                return@LaunchedEffect
                            }

                            authViewModel.restoreSession { restored ->
                                if (restored) {
                                    navigateAfterAuthentication(authViewModel, navController)
                                }
                            }
                        }

                        LaunchedEffect(passwordRecoveryIntent) {
                            val recoveryIntent = passwordRecoveryIntent ?: return@LaunchedEffect

                            authViewModel.handlePasswordRecoveryDeepLink(recoveryIntent) {
                                navController.navigate(AppRoutes.ResetPassword) {
                                    popUpTo(AppRoutes.Login) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = if (passwordRecoveryIntent != null) {
                                AppRoutes.ResetPassword
                            } else if (emailConfirmationIntent != null) {
                                AppRoutes.EmailConfirmed
                            } else {
                                AppRoutes.Login
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable(AppRoutes.Login) {
                                LoginScreen(
                                    authViewModel = authViewModel,
                                    onGoToRegister = {
                                        navController.navigate(AppRoutes.Register) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onLoginSuccess = {
                                        navigateAfterAuthentication(authViewModel, navController)
                                    }
                                )
                            }

                            composable(AppRoutes.Register) {
                                RegisterScreen(
                                    authViewModel = authViewModel,
                                    onGoToLogin = {
                                        navController.navigate(AppRoutes.Login) {
                                            popUpTo(AppRoutes.Register) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }

                            composable(AppRoutes.EmailConfirmed) {
                                EmailConfirmedScreen(
                                    onGoToLogin = {
                                        emailConfirmationIntent = null
                                        navController.navigate(AppRoutes.Login) {
                                            popUpTo(AppRoutes.EmailConfirmed) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }

                            composable(AppRoutes.ResetPassword) {
                                ResetPasswordScreen(
                                    authViewModel = authViewModel,
                                    onPasswordChanged = {
                                        passwordRecoveryIntent = null
                                        navController.navigate(AppRoutes.Login) {
                                            popUpTo(AppRoutes.ResetPassword) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }

                            composable(AppRoutes.Intro) {
                                IntroSliderScreen(
                                    role = authState.currentUser?.role.orEmpty(),
                                    onFinished = {
                                        authViewModel.markIntroSeenForCurrentUser {
                                            navController.navigate(AppRoutes.homeForRole(authViewModel.currentUser?.role)) {
                                                popUpTo(AppRoutes.Intro) { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                )
                            }

                            adminRoute(
                                route = AppRoutes.AdminDashboard,
                                selectedRoute = AppRoutes.AdminDashboard,
                                authViewModel = authViewModel,
                                navController = navController,
                                hasInternetState = hasInternetState,
                                notificationsState = notificationsStateHolder,
                                onNotificationClick = { notification ->
                                    notificationsViewModel.markAsRead(notification.id)
                                    navigateFromNotification(notification, navController)
                                },
                                onMarkAllNotificationsRead = {
                                    notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id)
                                }
                            )
                            adminRoute(AppRoutes.AdminProjects, AppRoutes.AdminProjects, authViewModel, navController, hasInternetState, notificationsStateHolder, { notification ->
                                notificationsViewModel.markAsRead(notification.id)
                                navigateFromNotification(notification, navController)
                            }, { notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id) })
                            adminRoute(AppRoutes.AdminTasks, AppRoutes.AdminTasks, authViewModel, navController, hasInternetState, notificationsStateHolder, { notification ->
                                notificationsViewModel.markAsRead(notification.id)
                                navigateFromNotification(notification, navController)
                            }, { notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id) })
                            adminRoute(AppRoutes.AdminTeams, AppRoutes.AdminTeams, authViewModel, navController, hasInternetState, notificationsStateHolder, { notification ->
                                notificationsViewModel.markAsRead(notification.id)
                                navigateFromNotification(notification, navController)
                            }, { notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id) })
                            adminRoute(AppRoutes.AdminReports, AppRoutes.AdminReports, authViewModel, navController, hasInternetState, notificationsStateHolder, { notification ->
                                notificationsViewModel.markAsRead(notification.id)
                                navigateFromNotification(notification, navController)
                            }, { notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id) })
                            adminRoute(AppRoutes.AdminSettings, AppRoutes.AdminSettings, authViewModel, navController, hasInternetState, notificationsStateHolder, { notification ->
                                notificationsViewModel.markAsRead(notification.id)
                                navigateFromNotification(notification, navController)
                            }, { notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id) })
                            adminRoute(AppRoutes.AdminProfile, AppRoutes.AdminProfile, authViewModel, navController, hasInternetState, notificationsStateHolder, { notification ->
                                notificationsViewModel.markAsRead(notification.id)
                                navigateFromNotification(notification, navController)
                            }, { notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id) })

                            gestorRoute(
                                route = AppRoutes.GestorDashboard,
                                selectedRoute = AppRoutes.GestorDashboard,
                                authViewModel = authViewModel,
                                navController = navController,
                                hasInternetState = hasInternetState,
                                notificationsState = notificationsStateHolder,
                                onNotificationClick = { notification ->
                                    notificationsViewModel.markAsRead(notification.id)
                                    navigateFromNotification(notification, navController)
                                },
                                onMarkAllNotificationsRead = {
                                    notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id)
                                }
                            )
                            gestorRoute(AppRoutes.GestorProjects, AppRoutes.GestorProjects, authViewModel, navController, hasInternetState, notificationsStateHolder, { notification ->
                                notificationsViewModel.markAsRead(notification.id)
                                navigateFromNotification(notification, navController)
                            }, { notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id) })
                            gestorRoute(AppRoutes.GestorTasks, AppRoutes.GestorTasks, authViewModel, navController, hasInternetState, notificationsStateHolder, { notification ->
                                notificationsViewModel.markAsRead(notification.id)
                                navigateFromNotification(notification, navController)
                            }, { notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id) })
                            gestorRoute(AppRoutes.GestorTeam, AppRoutes.GestorTeam, authViewModel, navController, hasInternetState, notificationsStateHolder, { notification ->
                                notificationsViewModel.markAsRead(notification.id)
                                navigateFromNotification(notification, navController)
                            }, { notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id) })
                            gestorRoute(AppRoutes.GestorReports, AppRoutes.GestorReports, authViewModel, navController, hasInternetState, notificationsStateHolder, { notification ->
                                notificationsViewModel.markAsRead(notification.id)
                                navigateFromNotification(notification, navController)
                            }, { notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id) })
                            gestorRoute(AppRoutes.GestorSettings, AppRoutes.GestorSettings, authViewModel, navController, hasInternetState, notificationsStateHolder, { notification ->
                                notificationsViewModel.markAsRead(notification.id)
                                navigateFromNotification(notification, navController)
                            }, { notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id) })
                            gestorRoute(AppRoutes.GestorProfile, AppRoutes.GestorProfile, authViewModel, navController, hasInternetState, notificationsStateHolder, { notification ->
                                notificationsViewModel.markAsRead(notification.id)
                                navigateFromNotification(notification, navController)
                            }, { notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id) })

                            userRoute(
                                route = AppRoutes.UserDashboard,
                                selectedRoute = AppRoutes.UserDashboard,
                                authViewModel = authViewModel,
                                navController = navController,
                                hasInternetState = hasInternetState,
                                notificationsState = notificationsStateHolder,
                                onNotificationClick = { notification ->
                                    notificationsViewModel.markAsRead(notification.id)
                                    navigateFromNotification(notification, navController)
                                },
                                onMarkAllNotificationsRead = {
                                    notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id)
                                }
                            )
                            userRoute(AppRoutes.UserTasks, AppRoutes.UserTasks, authViewModel, navController, hasInternetState, notificationsStateHolder, { notification ->
                                notificationsViewModel.markAsRead(notification.id)
                                navigateFromNotification(notification, navController)
                            }, { notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id) })
                            userRoute(AppRoutes.UserProjects, AppRoutes.UserProjects, authViewModel, navController, hasInternetState, notificationsStateHolder, { notification ->
                                notificationsViewModel.markAsRead(notification.id)
                                navigateFromNotification(notification, navController)
                            }, { notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id) })
                            userRoute(AppRoutes.UserSettings, AppRoutes.UserSettings, authViewModel, navController, hasInternetState, notificationsStateHolder, { notification ->
                                notificationsViewModel.markAsRead(notification.id)
                                navigateFromNotification(notification, navController)
                            }, { notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id) })
                            userRoute(AppRoutes.UserProfile, AppRoutes.UserProfile, authViewModel, navController, hasInternetState, notificationsStateHolder, { notification ->
                                notificationsViewModel.markAsRead(notification.id)
                                navigateFromNotification(notification, navController)
                            }, { notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id) })

                            composable(
                                route = AppRoutes.UserTaskObservations,
                                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val currentHasInternet = hasInternetState.value
                                UtilizadorDashboardScreen(
                                    userId = authState.currentUser?.id,
                                    currentUser = authState.currentUser,
                                    onUserUpdated = authViewModel::updateCurrentUser,
                                    onLogout = { logoutAndGoToLogin(authViewModel, navController) },
                                    selectedRoute = AppRoutes.UserTasks,
                                    hasInternet = currentHasInternet,
                                    notifications = notificationsStateHolder.value.notifications,
                                    unreadNotificationsCount = notificationsStateHolder.value.unreadCount,
                                    notificationsLoading = notificationsStateHolder.value.isLoading,
                                    notificationsError = notificationsStateHolder.value.errorMessage,
                                    onNotificationClick = { notification ->
                                        notificationsViewModel.markAsRead(notification.id)
                                        navigateFromNotification(notification, navController)
                                    },
                                    onMarkAllNotificationsRead = {
                                        notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id)
                                    },
                                    taskObservationsId = if (currentHasInternet) backStackEntry.arguments?.getInt("taskId") else null,
                                    onNavigate = { section ->
                                        navController.navigate(section) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = AppRoutes.UserProjectHistory,
                                arguments = listOf(navArgument("projectId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val currentHasInternet = hasInternetState.value
                                UtilizadorDashboardScreen(
                                    userId = authState.currentUser?.id,
                                    currentUser = authState.currentUser,
                                    onUserUpdated = authViewModel::updateCurrentUser,
                                    onLogout = { logoutAndGoToLogin(authViewModel, navController) },
                                    selectedRoute = AppRoutes.UserProjects,
                                    hasInternet = currentHasInternet,
                                    notifications = notificationsStateHolder.value.notifications,
                                    unreadNotificationsCount = notificationsStateHolder.value.unreadCount,
                                    notificationsLoading = notificationsStateHolder.value.isLoading,
                                    notificationsError = notificationsStateHolder.value.errorMessage,
                                    onNotificationClick = { notification ->
                                        notificationsViewModel.markAsRead(notification.id)
                                        navigateFromNotification(notification, navController)
                                    },
                                    onMarkAllNotificationsRead = {
                                        notificationsViewModel.markAllAsRead(authStateHolder.value.currentUser?.id)
                                    },
                                    projectHistoryId = if (currentHasInternet) backStackEntry.arguments?.getInt("projectId") else null,
                                    onNavigate = { section ->
                                        navController.navigate(section) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (intent.isPasswordRecoveryIntent()) {
            passwordRecoveryIntent = intent
        }

        if (intent.isEmailConfirmationIntent()) {
            emailConfirmationIntent = intent
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (AppNotificationHelper.canPostNotifications(this)) return

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            42
        )
    }

    private fun Intent.isPasswordRecoveryIntent(): Boolean {
        val data = data ?: return false
        return data.scheme == "projecthub" && data.host == "reset-password"
    }

    private fun Intent.isEmailConfirmationIntent(): Boolean {
        val data = data ?: return false
        return data.scheme == "projecthub" && data.host == "confirm-email"
    }
}

private fun navigateAfterAuthentication(
    authViewModel: AuthViewModel,
    navController: NavHostController
) {
    authViewModel.shouldShowIntroForCurrentUser { shouldShowIntro ->
        val destination = if (shouldShowIntro) {
            AppRoutes.Intro
        } else {
            AppRoutes.homeForRole(authViewModel.currentUser?.role)
        }

        navController.navigate(destination) {
            popUpTo(AppRoutes.Login) { inclusive = true }
            launchSingleTop = true
        }
    }
}

private fun navigateFromNotification(
    notification: NotificationDto,
    navController: NavHostController
) {
    val route = notification.relatedRoute?.takeIf { it.isNotBlank() } ?: return
    runCatching {
        navController.navigate(route) {
            launchSingleTop = true
        }
    }
}

@androidx.compose.runtime.Composable
private fun rememberHasInternet(): Boolean {
    val context = LocalContext.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    var hasInternet by remember { mutableStateOf(context.hasValidatedInternet()) }

    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val callback = object : ConnectivityManager.NetworkCallback() {
            private fun refresh() {
                mainHandler.post {
                    hasInternet = context.hasValidatedInternet()
                }
            }

            override fun onAvailable(network: Network) {
                refresh()
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                refresh()
            }

            override fun onLost(network: Network) {
                refresh()
            }
        }

        hasInternet = context.hasValidatedInternet()
        runCatching { connectivityManager.registerDefaultNetworkCallback(callback) }

        onDispose {
            runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        }
    }

    return hasInternet
}

private fun Context.hasValidatedInternet(): Boolean {
    val connectivityManager = getSystemService(ConnectivityManager::class.java)
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

private fun NavGraphBuilder.adminRoute(
    route: String,
    selectedRoute: String,
    authViewModel: AuthViewModel,
    navController: NavHostController,
    hasInternetState: State<Boolean>,
    notificationsState: State<NotificationsState>,
    onNotificationClick: (NotificationDto) -> Unit,
    onMarkAllNotificationsRead: () -> Unit
) {
    composable(route) {
        val authState by authViewModel.state.collectAsStateWithLifecycle()
        val hasInternet = hasInternetState.value
        val currentNotificationsState = notificationsState.value

        AdminDashboardScreen(
            currentUser = authState.currentUser,
            onUserUpdated = authViewModel::updateCurrentUser,
            onLogout = { logoutAndGoToLogin(authViewModel, navController) },
            selectedRoute = selectedRoute,
            hasInternet = hasInternet,
            notifications = currentNotificationsState.notifications,
            unreadNotificationsCount = currentNotificationsState.unreadCount,
            notificationsLoading = currentNotificationsState.isLoading,
            notificationsError = currentNotificationsState.errorMessage,
            onNotificationClick = onNotificationClick,
            onMarkAllNotificationsRead = onMarkAllNotificationsRead,
            onNavigate = { section ->
                navController.navigate(section) {
                    launchSingleTop = true
                }
            }
        )
    }
}

private fun NavGraphBuilder.gestorRoute(
    route: String,
    selectedRoute: String,
    authViewModel: AuthViewModel,
    navController: NavHostController,
    hasInternetState: State<Boolean>,
    notificationsState: State<NotificationsState>,
    onNotificationClick: (NotificationDto) -> Unit,
    onMarkAllNotificationsRead: () -> Unit
) {
    composable(route) {
        val authState by authViewModel.state.collectAsStateWithLifecycle()
        val hasInternet = hasInternetState.value
        val currentNotificationsState = notificationsState.value

        GestorDashboardScreen(
            gestorId = authState.currentUser?.id,
            currentUser = authState.currentUser,
            onUserUpdated = authViewModel::updateCurrentUser,
            onLogout = { logoutAndGoToLogin(authViewModel, navController) },
            selectedRoute = selectedRoute,
            hasInternet = hasInternet,
            notifications = currentNotificationsState.notifications,
            unreadNotificationsCount = currentNotificationsState.unreadCount,
            notificationsLoading = currentNotificationsState.isLoading,
            notificationsError = currentNotificationsState.errorMessage,
            onNotificationClick = onNotificationClick,
            onMarkAllNotificationsRead = onMarkAllNotificationsRead,
            onNavigate = { section ->
                navController.navigate(section) {
                    launchSingleTop = true
                }
            }
        )
    }
}

private fun NavGraphBuilder.userRoute(
    route: String,
    selectedRoute: String,
    authViewModel: AuthViewModel,
    navController: NavHostController,
    hasInternetState: State<Boolean>,
    notificationsState: State<NotificationsState>,
    onNotificationClick: (NotificationDto) -> Unit,
    onMarkAllNotificationsRead: () -> Unit
) {
    composable(route) {
        val authState by authViewModel.state.collectAsStateWithLifecycle()
        val hasInternet = hasInternetState.value
        val currentNotificationsState = notificationsState.value

        UtilizadorDashboardScreen(
            userId = authState.currentUser?.id,
            currentUser = authState.currentUser,
            onUserUpdated = authViewModel::updateCurrentUser,
            onLogout = { logoutAndGoToLogin(authViewModel, navController) },
            selectedRoute = selectedRoute,
            hasInternet = hasInternet,
            notifications = currentNotificationsState.notifications,
            unreadNotificationsCount = currentNotificationsState.unreadCount,
            notificationsLoading = currentNotificationsState.isLoading,
            notificationsError = currentNotificationsState.errorMessage,
            onNotificationClick = onNotificationClick,
            onMarkAllNotificationsRead = onMarkAllNotificationsRead,
            onNavigate = { section ->
                navController.navigate(section) {
                    launchSingleTop = true
                }
            }
        )
    }
}

private fun logoutAndGoToLogin(
    authViewModel: AuthViewModel,
    navController: NavHostController
) {
    authViewModel.logout {
        navController.navigate(AppRoutes.Login) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }
}
