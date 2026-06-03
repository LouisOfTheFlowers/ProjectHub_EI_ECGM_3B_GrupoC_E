package com.example.projecthub.uiscreens.utilizador.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.uiscreens.components.AppMessageCard
import com.example.projecthub.uiscreens.components.AppStatusChip
import java.text.Normalizer
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
internal fun TaskMeta(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun StatusPill(status: String) {
    AppStatusChip(text = status)
}

@Composable
internal fun TaskMessageCard(
    title: String,
    detail: String
) {
    AppMessageCard(
        title = title,
        detail = detail
    )
}

internal fun String?.toUiDate(): String {
    val date = this?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: return "-"
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

internal fun String.toDisplayDate(): String {
    val date = toLocalDateOrNull() ?: return this
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

internal fun String?.toLocalDateOrNull(): LocalDate? {
    return this?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
}

internal fun LocalDate.toEpochMillis(): Long {
    return atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

internal fun String.isCompletedStatus(): Boolean {
    return normalizedStatus() in setOf(
        "CONCLUIDO",
        "CONCLUIDA",
        "COMPLETO",
        "COMPLETA",
        "COMPLETADO",
        "COMPLETADA",
        "FINALIZADO",
        "FINALIZADA"
    )
}

private fun String.normalizedStatus(): String {
    val withoutAccents = Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")

    return withoutAccents
        .trim()
        .replace(" ", "_")
        .replace("-", "_")
        .uppercase()
}

