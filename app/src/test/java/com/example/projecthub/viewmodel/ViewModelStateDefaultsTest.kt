package com.example.projecthub.viewmodel

import com.example.projecthub.viewmodel.admin.AdminProjectsState
import com.example.projecthub.viewmodel.admin.AdminReportExportType
import com.example.projecthub.viewmodel.admin.AdminReportSummary
import com.example.projecthub.viewmodel.admin.AdminReportsState
import com.example.projecthub.viewmodel.admin.AdminTasksState
import com.example.projecthub.viewmodel.gestor.GestorTaskInfoState
import com.example.projecthub.viewmodel.gestor.GestorTaskStatusFilter
import com.example.projecthub.viewmodel.gestor.GestorTasksState
import com.example.projecthub.viewmodel.utilizador.UtilizadorProjectsState
import com.example.projecthub.viewmodel.utilizador.UtilizadorTasksState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewModelStateDefaultsTest {

    @Test
    fun adminTasksState_startsLoadingWithAllFilterAndEmptyLists() {
        val state = AdminTasksState()

        assertTrue(state.isLoading)
        assertEquals(0, state.totalTasks)
        assertEquals(0, state.completedTasks)
        assertTrue(state.projectGroups.isEmpty())
        assertEquals(com.example.projecthub.viewmodel.admin.AdminTaskStatusFilter.All, state.selectedStatus)
        assertEquals("", state.searchQuery)
        assertNull(state.errorMessage)
    }

    @Test
    fun adminProjectsState_startsLoadingWithDefaultFilters() {
        val state = AdminProjectsState()

        assertTrue(state.isLoading)
        assertEquals("Todos", state.selectedStatus)
        assertEquals("Todos", state.selectedCoordinator)
        assertTrue(state.projects.isEmpty())
        assertTrue(state.visibleProjects.isEmpty())
        assertFalse(state.isCreating)
        assertNull(state.errorMessage)
    }

    @Test
    fun gestorTasksState_startsLoadingWithAllFilterAndEmptyDetail() {
        val state = GestorTasksState()

        assertTrue(state.isLoading)
        assertEquals(GestorTaskStatusFilter.All, state.selectedStatus)
        assertTrue(state.projectGroups.isEmpty())
        assertTrue(state.expandedProjectIds.isEmpty())
        assertEquals(GestorTaskInfoState(), state.detailState)
        assertFalse(state.isCreating)
    }

    @Test
    fun utilizadorStates_startNotLoadingAndWithoutError() {
        val tasksState = UtilizadorTasksState()
        val projectsState = UtilizadorProjectsState()

        assertFalse(tasksState.isLoading)
        assertFalse(projectsState.isLoading)
        assertTrue(tasksState.tasks.isEmpty())
        assertTrue(projectsState.projects.isEmpty())
        assertNull(tasksState.errorMessage)
        assertNull(projectsState.errorMessage)
    }

    @Test
    fun profileState_startsIdleAndOnline() {
        val state = ProfileState()

        assertNull(state.user)
        assertFalse(state.isSaving)
        assertFalse(state.isSendingEmailCode)
        assertFalse(state.emailCodeSent)
        assertFalse(state.isDeleting)
        assertFalse(state.isOffline)
        assertNull(state.message)
        assertNull(state.errorMessage)
    }

    @Test
    fun adminReportSummary_startsWithZeroValues() {
        val summary = AdminReportSummary()

        assertEquals(0, summary.totalUsers)
        assertEquals(0, summary.totalProjects)
        assertEquals(0, summary.totalTasks)
        assertEquals(0, summary.completedTasks)
        assertEquals(0, summary.delayedTasks)
        assertEquals(0, summary.averageCompletion)
        assertEquals(0f, summary.totalHours)
    }

    @Test
    fun adminReportsState_startsLoadingWithEmptyCardsAndNoErrors() {
        val state = AdminReportsState()

        assertTrue(state.isLoading)
        assertTrue(state.cards.isEmpty())
        assertEquals(AdminReportSummary(), state.summary)
        assertNull(state.errorMessage)
        assertNull(state.exportErrorMessage)
    }

    @Test
    fun adminReportExportTypes_keepExpectedTitlesAndFilePrefixes() {
        assertEquals("Utilizadores", AdminReportExportType.Users.title)
        assertEquals("utilizadores", AdminReportExportType.Users.filePrefix)
        assertEquals("Projetos", AdminReportExportType.Projects.title)
        assertEquals("projetos", AdminReportExportType.Projects.filePrefix)
        assertEquals("Tarefas", AdminReportExportType.Tasks.title)
        assertEquals("tarefas", AdminReportExportType.Tasks.filePrefix)
    }
}
