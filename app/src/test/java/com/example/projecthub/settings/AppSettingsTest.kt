package com.example.projecthub.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSettingsTest {

    @Test
    fun appLanguageFromCode_returnsMatchingLanguageOrPortugueseFallback() {
        assertEquals(AppLanguage.English, AppLanguage.fromCode("en"))
        assertEquals(AppLanguage.Spanish, AppLanguage.fromCode("es"))
        assertEquals(AppLanguage.Portuguese, AppLanguage.fromCode(null))
        assertEquals(AppLanguage.Portuguese, AppLanguage.fromCode("unknown"))
    }

    @Test
    fun appThemeModeFromCode_returnsMatchingThemeOrLightFallback() {
        assertEquals(AppThemeMode.Dark, AppThemeMode.fromCode("dark"))
        assertEquals(AppThemeMode.System, AppThemeMode.fromCode("system"))
        assertEquals(AppThemeMode.Light, AppThemeMode.fromCode(null))
        assertEquals(AppThemeMode.Light, AppThemeMode.fromCode("unknown"))
    }

    @Test
    fun appDateFormatFromCode_returnsMatchingFormatOrDayMonthYearFallback() {
        assertEquals(AppDateFormat.YearMonthDay, AppDateFormat.fromCode("ymd"))
        assertEquals(AppDateFormat.MonthDayYear, AppDateFormat.fromCode("mdy"))
        assertEquals(AppDateFormat.DayMonthYear, AppDateFormat.fromCode(null))
        assertEquals(AppDateFormat.DayMonthYear, AppDateFormat.fromCode("unknown"))
    }
}
