package com.example.projecthub.uiscreens.utilizador.projects

import androidx.compose.runtime.Composable
import com.example.projecthub.uiscreens.components.AppMessageCard
import com.example.projecthub.uiscreens.components.AppStatusChip
import com.example.projecthub.viewmodel.utilizador.UtilizadorProjectItem
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun ProjectStatusPill(status: String) {
    AppStatusChip(text = status)
}

@Composable
internal fun ProjectMessageCard(
    title: String,
    detail: String
) {
    AppMessageCard(title = title, detail = detail)
}

internal fun projectProgress(item: UtilizadorProjectItem): String {
    if (item.tasksCount == 0) return "0%"
    return "${((item.completedTasks.toFloat() / item.tasksCount.toFloat()) * 100).toInt()}%"
}

internal fun String?.toUiDate(): String {
    val date = this?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: return "-"
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
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

