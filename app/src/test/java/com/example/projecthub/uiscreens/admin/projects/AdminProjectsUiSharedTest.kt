package com.example.projecthub.uiscreens.admin.projects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class AdminProjectsUiSharedTest {

    @Test
    fun toProjectLocalDateOrNull_parsesPortugueseDisplayDate() {
        assertEquals(LocalDate.of(2026, 6, 4), "04/06/2026".toProjectLocalDateOrNull())
    }

    @Test
    fun toProjectLocalDateOrNull_parsesIsoDateAndIgnoresTimeSuffix() {
        assertEquals(LocalDate.of(2026, 6, 4), "2026-06-04".toProjectLocalDateOrNull())
        assertEquals(LocalDate.of(2026, 6, 4), "2026-06-04T13:45:00".toProjectLocalDateOrNull())
    }

    @Test
    fun toProjectLocalDateOrNull_returnsNullForBlankDashOrInvalidDate() {
        assertNull("".toProjectLocalDateOrNull())
        assertNull("-".toProjectLocalDateOrNull())
        assertNull("sem-data".toProjectLocalDateOrNull())
    }

    @Test
    fun toAdminProjectDisplayDate_formatsDateWithProvidedPattern() {
        assertEquals("04/06/2026", "2026-06-04".toAdminProjectDisplayDate())
        assertEquals("2026-06-04", "04/06/2026".toAdminProjectDisplayDate("yyyy-MM-dd"))
    }

    @Test
    fun toAdminProjectDisplayDate_keepsInvalidDateText() {
        assertEquals("sem-data", "sem-data".toAdminProjectDisplayDate())
    }

    @Test
    fun toAdminProjectEpochMillis_usesStartOfDayUtc() {
        assertEquals(
            Instant.parse("2026-06-04T00:00:00Z").toEpochMilli(),
            LocalDate.of(2026, 6, 4).toAdminProjectEpochMillis()
        )
    }
}
