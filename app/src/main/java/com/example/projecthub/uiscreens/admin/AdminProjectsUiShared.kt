package com.example.projecthub.uiscreens.admin

import com.example.projecthub.uiscreens.*

import androidx.compose.runtime.Composable
import com.example.projecthub.ui.theme.ProjectHubColors
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

internal val ProjectsAccent = AuthAccent
internal val ProjectsRed = ProjectHubColors.Danger
internal val ProjectsGreen = ProjectHubColors.Success

@Composable
internal fun ProjectInfoRow(label: String, value: String) {
    AppInfoRow(label = label, value = value)
}

internal fun String.toProjectLocalDateOrNull(): LocalDate? {
    val trimmed = trim()

    if (trimmed.isBlank() || trimmed == "-") {
        return null
    }

    return try {
        LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (_: DateTimeParseException) {
        runCatching { LocalDate.parse(trimmed.take(10)) }.getOrNull()
    }
}

internal fun String.toAdminProjectDisplayDate(pattern: String = "dd/MM/yyyy"): String {
    val date = toProjectLocalDateOrNull() ?: return this

    return date.format(DateTimeFormatter.ofPattern(pattern))
}

internal fun LocalDate.toAdminProjectEpochMillis(): Long {
    return atStartOfDay()
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()
}

internal enum class ProjectStatIcon {
    Completed,
    Trend,
    Clock
}
