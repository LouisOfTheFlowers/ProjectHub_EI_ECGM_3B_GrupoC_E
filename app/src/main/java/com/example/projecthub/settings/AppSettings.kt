package com.example.projecthub.settings

data class AppSettings(
    val language: AppLanguage = AppLanguage.Portuguese,
    val themeMode: AppThemeMode = AppThemeMode.Light,
    val dateFormat: AppDateFormat = AppDateFormat.DayMonthYear,
    val notificationsEnabled: Boolean = true,
    val soundsEnabled: Boolean = true
)

enum class AppLanguage(val code: String, val label: String) {
    Portuguese("pt", "Português"),
    English("en", "English"),
    Spanish("es", "Español");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: Portuguese
    }
}

enum class AppThemeMode(val code: String, val label: String) {
    Light("light", "Claro"),
    Dark("dark", "Escuro"),
    System("system", "Sistema");

    companion object {
        fun fromCode(code: String?): AppThemeMode =
            entries.firstOrNull { it.code == code } ?: Light
    }
}

enum class AppDateFormat(val code: String, val label: String, val pattern: String) {
    DayMonthYear("dmy", "DD/MM/AAAA", "dd/MM/yyyy"),
    YearMonthDay("ymd", "AAAA-MM-DD", "yyyy-MM-dd"),
    MonthDayYear("mdy", "MM/DD/AAAA", "MM/dd/yyyy");

    companion object {
        fun fromCode(code: String?): AppDateFormat =
            entries.firstOrNull { it.code == code } ?: DayMonthYear
    }
}
