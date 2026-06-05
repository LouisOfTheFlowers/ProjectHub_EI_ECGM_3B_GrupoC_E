package com.example.projecthub.uiscreens.utilizador.projects

import com.example.projecthub.remote.supabase.models.ProjetoDto
import com.example.projecthub.viewmodel.utilizador.UtilizadorProjectItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilizadorProjectsUiSharedTest {

    @Test
    fun projectProgress_returnsZeroWhenProjectHasNoTasks() {
        assertEquals("0%", projectProgress(projectItem(tasksCount = 0, completedTasks = 0)))
    }

    @Test
    fun projectProgress_calculatesCompletedTaskPercentage() {
        assertEquals("40%", projectProgress(projectItem(tasksCount = 10, completedTasks = 4)))
        assertEquals("75%", projectProgress(projectItem(tasksCount = 4, completedTasks = 3)))
    }

    @Test
    fun toUiDate_formatsIsoDateAndIgnoresTimeSuffix() {
        assertEquals("04/06/2026", "2026-06-04".toUiDate())
        assertEquals("04/06/2026", "2026-06-04T12:30:00".toUiDate())
    }

    @Test
    fun toUiDate_returnsDashForNullOrInvalidDate() {
        assertEquals("-", (null as String?).toUiDate())
        assertEquals("-", "sem-data".toUiDate())
    }

    @Test
    fun isCompletedStatus_acceptsCompletedLabelsWithAccentsAndCase() {
        assertTrue("Concluido".isCompletedStatus())
        assertTrue("Conclu\u00edda".isCompletedStatus())
        assertTrue("FINALIZADA".isCompletedStatus())
    }

    @Test
    fun isCompletedStatus_rejectsNonCompletedLabels() {
        assertFalse("Pendente".isCompletedStatus())
        assertFalse("Em Progresso".isCompletedStatus())
    }

    private fun projectItem(
        tasksCount: Int,
        completedTasks: Int
    ): UtilizadorProjectItem {
        return UtilizadorProjectItem(
            project = ProjetoDto(id = 1, nome = "Projeto Teste"),
            tasksCount = tasksCount,
            completedTasks = completedTasks,
            lateTasks = 0,
            completedTaskHistory = emptyList()
        )
    }
}
