package com.example.projecthub.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppRoutesTest {

    @Test
    fun userTaskObservations_buildsRouteWithTaskId() {
        assertEquals("user/tasks/42/observations", AppRoutes.userTaskObservations(42))
    }

    @Test
    fun userProjectHistory_buildsRouteWithProjectId() {
        assertEquals("user/projects/7/history", AppRoutes.userProjectHistory(7))
    }

    @Test
    fun homeForRole_returnsCorrectHomeRoute() {
        assertEquals(AppRoutes.AdminDashboard, AppRoutes.homeForRole(null))
        assertEquals(AppRoutes.AdminDashboard, AppRoutes.homeForRole("ADMIN"))
        assertEquals(AppRoutes.GestorDashboard, AppRoutes.homeForRole("gestor"))
        assertEquals(AppRoutes.UserDashboard, AppRoutes.homeForRole("UTILIZADOR"))
    }
}
