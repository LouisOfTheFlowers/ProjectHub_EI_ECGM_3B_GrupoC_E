package com.example.projecthub.uiscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.projecthub.R
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors

@Composable
fun AppStatusChip(
    text: String,
    backgroundColor: Color = statusColorForLabel(text).copy(alpha = 0.14f),
    contentColor: Color = statusColorForLabel(text),
    modifier: Modifier = Modifier
) {
    val language = currentAppSettings().language
    val displayText = text.toTranslatedStatusLabel(language)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(contentColor)
        )
        Box(modifier = Modifier.size(6.dp))
        Text(
            text = displayText,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun String.toTranslatedStatusLabel(language: com.example.projecthub.settings.AppLanguage): String {
    return when (trim().lowercase()) {
        "concluido", "concluÃ­do", "concluida", "concluÃ­da", "completo", "completa", "completado", "completada", "finalizado", "finalizada" ->
            language.t("common.completed")
        "em progresso", "em_progresso", "emprogresso", "a decorrer", "a_decorrer", "in progress", "in_progress", "inprogress" ->
            language.t("common.inProgress")
        "pendente", "pendentes", "pending", "por iniciar", "por_iniciar" ->
            language.t("common.pending")
        "atrasado", "atrasada", "atrasados", "atrasadas", "delayed" ->
            language.t("common.delayed")
        "ativo", "actvo", "activo", "active" ->
            language.t("status.active")
        "inativo", "inactvo", "inactivo", "inactive" ->
            language.t("status.inactive")
        else -> this
    }
}


fun statusColorForLabel(label: String): Color {
    return when {
        label.contains("concl", ignoreCase = true) -> ProjectHubColors.Success
        label.contains("progres", ignoreCase = true) -> ProjectHubColors.InfoLight
        label.contains("atras", ignoreCase = true) -> ProjectHubColors.Danger
        label.contains("pend", ignoreCase = true) -> ProjectHubColors.Warning
        label.contains("ativo", ignoreCase = true) -> ProjectHubColors.Success
        label.contains("active", ignoreCase = true) -> ProjectHubColors.Success
        label.contains("inativo", ignoreCase = true) -> ProjectHubColors.Muted
        label.contains("inactive", ignoreCase = true) -> ProjectHubColors.Muted
        else -> ProjectHubColors.Warning
    }
}

