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

data class AppObservationUiModel(
    val text: String,
    val userName: String? = null,
    val date: String? = null,
    val completionPercent: Int? = null,
    val spentHours: Float? = null,
    val location: String? = null,
    val photoUrls: List<String> = emptyList()
)

data class AppDashboardMetric(
    val label: String,
    val value: String,
    val accent: Color,
    val detail: String,
    val iconRes: Int
)

@Composable
fun AppSurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        content()
    }
}

@Composable
fun AppDashboardMetricCard(
    metric: AppDashboardMetric,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(108.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(metric.accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(metric.iconRes),
                        contentDescription = null,
                        tint = metric.accent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                Column {
                    Text(
                        text = metric.label,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = metric.detail,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = metric.value,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 36.sp
            )
        }
    }
}

@Composable
fun AppCompactStatCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
    heightDp: Int = 82
) {
    Card(
        modifier = modifier.height(heightDp.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, color = ProjectHubColors.Muted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(text = value, color = accent, fontWeight = FontWeight.ExtraBold, fontSize = 27.sp)
        }
    }
}

@Composable
fun AppInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = ProjectHubColors.Muted, fontSize = 12.sp)
        Text(value, color = ProjectHubColors.Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun AppDetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, color = ProjectHubColors.Muted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(value, color = ProjectHubColors.Slate, fontSize = 14.sp)
    }
}

@Composable
fun AppMessageCard(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    titleSize: Int = 16
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = ProjectHubColors.Ink, fontWeight = FontWeight.ExtraBold, fontSize = titleSize.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(detail, color = ProjectHubColors.Muted, fontSize = 13.sp)
        }
    }
}

@Composable
fun AppOfflineState(
    modifier: Modifier = Modifier
) {
    val language = currentAppSettings().language

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(ProjectHubColors.Warning.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_wifi_off_24),
                    contentDescription = language.t("offline.title"),
                    tint = ProjectHubColors.Warning,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = language.t("offline.title"),
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 21.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = language.t("offline.detail"),
                color = ProjectHubColors.Muted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun <T> AppDropdownField(
    selected: T?,
    options: List<T>,
    label: (T?) -> String,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ProjectHubColors.LightSurface)
                .clickable(enabled = enabled && options.isNotEmpty(), onClick = rememberSoundClick { expanded = true })
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label(selected),
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            AppExpandIcon(expanded = expanded)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ProjectHubColors.LightSurface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(label(option), color = ProjectHubColors.Ink) },
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    }
                )
            }
        }
    }
}

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
        "concluido", "concluído", "concluida", "concluída", "completo", "completa", "completado", "completada", "finalizado", "finalizada" ->
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

@Composable
fun AppFormCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AppSurfaceCard(modifier = modifier) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 25.sp
            )
            Box(modifier = Modifier.height(18.dp))
            content()
        }
    }
}

@Composable
fun AppFormLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = ProjectHubColors.Ink,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        modifier = modifier.padding(bottom = 6.dp)
    )
}

@Composable
fun AppBackButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = rememberSoundClick(onClick),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = ProjectHubColors.Ink),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = AuthAccent
) {
    Button(
        onClick = rememberSoundClick(onClick),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AppFilledActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = AuthAccent
) {
    Button(
        onClick = rememberSoundClick(onClick),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = rememberSoundClick(onClick),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = ProjectHubColors.Disabled,
            contentColor = ProjectHubColors.Ink
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AppOutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = rememberSoundClick(onClick),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = AuthAccent,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AppObservationsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    count: Int? = null,
    compact: Boolean = false
) {
    val label = count?.let { "$text ($it)" } ?: text
    OutlinedButton(
        onClick = rememberSoundClick(onClick),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(if (compact) 36.dp else 44.dp)
    ) {
        Text(
            text = label,
            color = AuthAccent,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 12.sp else 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AppMoreInfoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    compact: Boolean = false
) {
    OutlinedButton(
        onClick = rememberSoundClick(onClick),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(if (compact) 34.dp else 44.dp)
    ) {
        Text(
            text = text,
            color = AuthAccent,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 12.sp else 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AppObservationCard(
    observation: AppObservationUiModel,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showPhotoPreview: Boolean = true
) {
    val language = currentAppSettings().language
    val clickableModifier = onClick?.let { Modifier.clickable(onClick = rememberSoundClick(it)) } ?: Modifier

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = observation.text,
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                observation.userName?.let {
                    AppObservationMeta(language.t("common.user"), it, Modifier.weight(1f))
                }
                observation.date?.let {
                    AppObservationMeta(language.t("common.date"), it, Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                observation.completionPercent?.let {
                    AppObservationMeta(language.t("user.tasks.completion"), "$it%", Modifier.weight(1f))
                }
                AppObservationMeta(language.t("common.photos"), observation.photoUrls.size.toString(), Modifier.weight(1f))
                observation.spentHours?.let {
                    AppObservationMeta(language.t("common.hours"), "$it h", Modifier.weight(1f))
                }
            }

            observation.location?.takeIf { it.isNotBlank() }?.let { location ->
                Spacer(modifier = Modifier.height(8.dp))
                AppObservationMeta(language.t("common.location"), location, Modifier.fillMaxWidth())
            }

            if (showPhotoPreview) {
                observation.photoUrls.firstOrNull()?.let { photoUrl ->
                    Spacer(modifier = Modifier.height(10.dp))
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = language.t("profile.photoDescription"),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ProjectHubColors.SurfaceSoft)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppObservationMeta(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, color = ProjectHubColors.Muted, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(value, color = ProjectHubColors.Slate, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun AppDialogConfirmButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = rememberSoundClick(onClick),
        enabled = enabled,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = AuthAccent,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AppDialogCancelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = rememberSoundClick(onClick),
        enabled = enabled,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = ProjectHubColors.Muted,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AppActionIconButton(
    icon: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val containerColor = if (enabled) color.copy(alpha = 0.12f) else ProjectHubColors.Disabled
    val contentColor = if (enabled) color else ProjectHubColors.Muted

    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .then(
                if (enabled) {
                    Modifier.clickable(onClick = rememberSoundClick(onClick))
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            color = contentColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            maxLines = 1
        )
    }
}

@Composable
fun AppExpandIcon(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = ProjectHubColors.Muted
) {
    Icon(
        painter = painterResource(
            id = if (expanded) R.drawable.ic_expand_less_24 else R.drawable.ic_expand_more_24
        ),
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(18.dp)
    )
}

@Composable
fun AppActionIconButton(
    painter: Painter,
    contentDescription: String?,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val containerColor = if (enabled) color.copy(alpha = 0.12f) else ProjectHubColors.Disabled
    val contentColor = if (enabled) color else ProjectHubColors.Muted

    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .then(
                if (enabled) {
                    Modifier.clickable(onClick = rememberSoundClick(onClick))
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    colors: TextFieldColors? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = true,
        colors = colors ?: appTextFieldColors(),
        modifier = modifier.fillMaxWidth()
    )
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

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        colors = appTextFieldColors()
    )
}

@Composable
fun AppTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        colors = appTextFieldColors()
    )
}
