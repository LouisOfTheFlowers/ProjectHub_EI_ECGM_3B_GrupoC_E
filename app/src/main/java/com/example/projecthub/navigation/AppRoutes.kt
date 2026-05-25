package com.example.projecthub.navigation

object AppRoutes {
    const val Login = "login"
    const val Register = "register"
    const val Intro = "intro"
    const val ResetPassword = "reset-password"

    const val AdminDashboard = "admin/dashboard"
    const val AdminProjects = "admin/projects"
    const val AdminTasks = "admin/tasks"
    const val AdminTeams = "admin/teams"
    const val AdminReports = "admin/reports"
    const val AdminSettings = "admin/settings"
    const val AdminProfile = "admin/profile"

    const val GestorDashboard = "gestor/dashboard"
    const val GestorProjects = "gestor/projects"
    const val GestorTasks = "gestor/tasks"
    const val GestorTeam = "gestor/team"
    const val GestorReports = "gestor/reports"
    const val GestorSettings = "gestor/settings"
    const val GestorProfile = "gestor/profile"

    const val UserDashboard = "user/dashboard"
    const val UserTasks = "user/tasks"
    const val UserProjects = "user/projects"
    const val UserSettings = "user/settings"
    const val UserProfile = "user/profile"
    const val UserTaskObservations = "user/tasks/{taskId}/observations"
    const val UserProjectHistory = "user/projects/{projectId}/history"

    fun userTaskObservations(taskId: Int): String {
        return "user/tasks/$taskId/observations"
    }

    fun userProjectHistory(projectId: Int): String {
        return "user/projects/$projectId/history"
    }

    fun homeForRole(role: String?): String {
        return when {
            role.equals("GESTOR", ignoreCase = true) -> GestorDashboard
            role.equals("UTILIZADOR", ignoreCase = true) -> UserDashboard
            else -> AdminDashboard
        }
    }
}
