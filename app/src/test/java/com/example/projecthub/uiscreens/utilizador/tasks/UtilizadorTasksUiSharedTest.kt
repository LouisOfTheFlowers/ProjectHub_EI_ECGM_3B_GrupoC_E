package com.example.projecthub.uiscreens.utilizador.tasks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class UtilizadorTasksUiSharedTest {

    @Test
    fun toUiDate_formatsIsoDateToPortugueseDisplayDate() {
        assertEquals("04/06/2026", "2026-06-04".toUiDate())
    }

    @Test
    fun toUiDate_returnsDashForNullOrInvalidDate() {
        assertEquals("-", (null as String?).toUiDate())
        assertEquals("-", "sem-data".toUiDate())
    }

    @Test
    fun toDisplayDate_formatsValidDateAndKeepsInvalidText() {
        assertEquals("04/06/2026", "2026-06-04".toDisplayDate())
        assertEquals("sem-data", "sem-data".toDisplayDate())
    }

    @Test
    fun toLocalDateOrNull_parsesIsoDateAndReturnsNullForInvalidDate() {
        assertEquals(LocalDate.of(2026, 6, 4), "2026-06-04".toLocalDateOrNull())
        assertNull("sem-data".toLocalDateOrNull())
    }

    @Test
    fun toEpochMillis_usesStartOfDayUtc() {
        assertEquals(
            Instant.parse("2026-06-04T00:00:00Z").toEpochMilli(),
            LocalDate.of(2026, 6, 4).toEpochMillis()
        )
    }

    @Test
    fun isCompletedStatus_acceptsDifferentCompletedLabels() {
        assertTrue("Concluido".isCompletedStatus())
        assertTrue("Conclu\u00eddo".isCompletedStatus())
        assertTrue("finalizado".isCompletedStatus())
        assertTrue("COMPLETADO".isCompletedStatus())
    }

    @Test
    fun isCompletedStatus_rejectsPendingOrInProgressLabels() {
        assertFalse("PENDENTE".isCompletedStatus())
        assertFalse("em progresso".isCompletedStatus())
    }
}
