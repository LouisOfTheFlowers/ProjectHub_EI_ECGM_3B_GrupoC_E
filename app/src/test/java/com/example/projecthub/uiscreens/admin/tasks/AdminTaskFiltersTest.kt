package com.example.projecthub.uiscreens.admin.tasks

import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.viewmodel.admin.AdminTaskStatusFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class AdminTaskFiltersTest {

    @Test
    fun translatedLabel_returnsEnglishLabels() {
        assertEquals("All", AdminTaskStatusFilter.All.translatedLabel(AppLanguage.English))
        assertEquals("Pending", AdminTaskStatusFilter.Pending.translatedLabel(AppLanguage.English))
        assertEquals("Completed", AdminTaskStatusFilter.Completed.translatedLabel(AppLanguage.English))
    }

    @Test
    fun translatedLabel_returnsPortugueseLabels() {
        assertEquals("Todas", AdminTaskStatusFilter.All.translatedLabel(AppLanguage.Portuguese))
        assertEquals("Pendentes", AdminTaskStatusFilter.Pending.translatedLabel(AppLanguage.Portuguese))
        assertEquals("Conclu\u00eddas", AdminTaskStatusFilter.Completed.translatedLabel(AppLanguage.Portuguese))
    }
}
