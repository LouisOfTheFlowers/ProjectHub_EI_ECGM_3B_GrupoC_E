package com.example.projecthub.uiscreens.gestor.tasks

import org.junit.Assert.assertEquals
import org.junit.Test

class GestorTasksUiSharedTest {

    @Test
    fun toInputDateText_convertsIsoDateToInputDate() {
        assertEquals("04/06/2026", "2026-06-04".toInputDateText())
    }

    @Test
    fun toInputDateText_returnsBlankForDash() {
        assertEquals("", "-".toInputDateText())
    }

    @Test
    fun toInputDateText_keepsAlreadyFormattedOrInvalidText() {
        assertEquals("04/06/2026", "04/06/2026".toInputDateText())
        assertEquals("sem-data", "sem-data".toInputDateText())
    }
}
