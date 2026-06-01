package com.example.projecthub

import android.Manifest
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.example.projecthub.settings.AppNotificationHelper
import com.example.projecthub.settings.AppSettingsProvider
import com.example.projecthub.settings.AppThemeMode
import com.example.projecthub.ui.theme.ProjectHubTheme
import com.example.projecthub.uiscreens.AdminDashboardScreen
import com.example.projecthub.uiscreens.EmailConfirmedScreen
import com.example.projecthub.uiscreens.GestorDashboardScreen
import com.example.projecthub.uiscreens.IntroSliderScreen
import com.example.projecthub.uiscreens.LoginScreen
import com.example.projecthub.uiscreens.RegisterScreen
import com.example.projecthub.uiscreens.ResetPasswordScreen
import com.example.projecthub.uiscreens.UtilizadorDashboardScreen
import com.example.projecthub.viewmodel.AuthViewModel
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
                        val navController = rememberNavController()

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
                                navController = navController
                            )
                            adminRoute(AppRoutes.AdminProjects, AppRoutes.AdminProjects, authViewModel, navController)
                            adminRoute(AppRoutes.AdminTasks, AppRoutes.AdminTasks, authViewModel, navController)
                            adminRoute(AppRoutes.AdminTeams, AppRoutes.AdminTeams, authViewModel, navController)
                            adminRoute(AppRoutes.AdminReports, AppRoutes.AdminReports, authViewModel, navController)
                            adminRoute(AppRoutes.AdminSettings, AppRoutes.AdminSettings, authViewModel, navController)
                            adminRoute(AppRoutes.AdminProfile, AppRoutes.AdminProfile, authViewModel, navController)

                            gestorRoute(
                                route = AppRoutes.GestorDashboard,
                                selectedRoute = AppRoutes.GestorDashboard,
                                authViewModel = authViewModel,
                                navController = navController
                            )
                            gestorRoute(AppRoutes.GestorProjects, AppRoutes.GestorProjects, authViewModel, navController)
                            gestorRoute(AppRoutes.GestorTasks, AppRoutes.GestorTasks, authViewModel, navController)
                            gestorRoute(AppRoutes.GestorTeam, AppRoutes.GestorTeam, authViewModel, navController)
                            gestorRoute(AppRoutes.GestorReports, AppRoutes.GestorReports, authViewModel, navController)
                            gestorRoute(AppRoutes.GestorSettings, AppRoutes.GestorSettings, authViewModel, navController)
                            gestorRoute(AppRoutes.GestorProfile, AppRoutes.GestorProfile, authViewModel, navController)

                            userRoute(
                                route = AppRoutes.UserDashboard,
                                selectedRoute = AppRoutes.UserDashboard,
                                authViewModel = authViewModel,
                                navController = navController
                            )
                            userRoute(AppRoutes.UserTasks, AppRoutes.UserTasks, authViewModel, navController)
                            userRoute(AppRoutes.UserProjects, AppRoutes.UserProjects, authViewModel, navController)
                            userRoute(AppRoutes.UserSettings, AppRoutes.UserSettings, authViewModel, navController)
                            userRoute(AppRoutes.UserProfile, AppRoutes.UserProfile, authViewModel, navController)

                            composable(
                                route = AppRoutes.UserTaskObservations,
                                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                UtilizadorDashboardScreen(
                                    userId = authState.currentUser?.id,
                                    currentUser = authState.currentUser,
                                    onUserUpdated = authViewModel::updateCurrentUser,
                                    onLogout = { logoutAndGoToLogin(authViewModel, navController) },
                                    selectedRoute = AppRoutes.UserTasks,
                                    taskObservationsId = backStackEntry.arguments?.getInt("taskId"),
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
                                UtilizadorDashboardScreen(
                                    userId = authState.currentUser?.id,
                                    currentUser = authState.currentUser,
                                    onUserUpdated = authViewModel::updateCurrentUser,
                                    onLogout = { logoutAndGoToLogin(authViewModel, navController) },
                                    selectedRoute = AppRoutes.UserProjects,
                                    projectHistoryId = backStackEntry.arguments?.getInt("projectId"),
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

private fun NavGraphBuilder.adminRoute(
    route: String,
    selectedRoute: String,
    authViewModel: AuthViewModel,
    navController: NavHostController
) {
    composable(route) {
        val authState by authViewModel.state.collectAsStateWithLifecycle()
        AdminDashboardScreen(
            currentUser = authState.currentUser,
            onUserUpdated = authViewModel::updateCurrentUser,
            onLogout = { logoutAndGoToLogin(authViewModel, navController) },
            selectedRoute = selectedRoute,
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
    navController: NavHostController
) {
    composable(route) {
        val authState by authViewModel.state.collectAsStateWithLifecycle()
        GestorDashboardScreen(
            gestorId = authState.currentUser?.id,
            currentUser = authState.currentUser,
            onUserUpdated = authViewModel::updateCurrentUser,
            onLogout = { logoutAndGoToLogin(authViewModel, navController) },
            selectedRoute = selectedRoute,
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
    navController: NavHostController
) {
    composable(route) {
        val authState by authViewModel.state.collectAsStateWithLifecycle()
        UtilizadorDashboardScreen(
            userId = authState.currentUser?.id,
            currentUser = authState.currentUser,
            onUserUpdated = authViewModel::updateCurrentUser,
            onLogout = { logoutAndGoToLogin(authViewModel, navController) },
            selectedRoute = selectedRoute,
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
