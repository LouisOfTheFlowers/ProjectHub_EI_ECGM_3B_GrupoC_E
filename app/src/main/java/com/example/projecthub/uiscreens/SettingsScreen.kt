package com.example.projecthub.uiscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.settings.AppDateFormat
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.settings.AppNotificationHelper
import com.example.projecthub.settings.AppSoundPlayer
import com.example.projecthub.settings.AppThemeMode
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.SettingsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val SettingsAccent = AuthAccent

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val language = settings.language
    val context = LocalContext.current
    val datePreview = remember(settings.dateFormat) {
        LocalDate.of(2026, 5, 19).format(DateTimeFormatter.ofPattern(settings.dateFormat.pattern))
    }

    fun playClick() {
        if (settings.soundsEnabled) {
            AppSoundPlayer.playClick()
        }
    }

    Column {
        SettingsHeader(language = language)
        Spacer(modifier = Modifier.height(18.dp))

        SettingsSummaryCard(
            language = language,
            themeMode = settings.themeMode,
            dateFormat = settings.dateFormat,
            datePreview = datePreview
        )

        Spacer(modifier = Modifier.height(14.dp))

        SettingsSection(title = language.t("settings.preferences")) {
            SettingsDropdownRow(
                title = language.t("settings.language"),
                description = language.t("settings.languageDescription"),
                value = settings.language,
                options = AppLanguage.entries,
                optionLabel = { it.label },
                onValueChange = {
                    playClick()
                    viewModel.setLanguage(it)
                }
            )
            SettingsDropdownRow(
                title = language.t("settings.theme"),
                description = language.t("settings.themeDescription"),
                value = settings.themeMode,
                options = AppThemeMode.entries,
                optionLabel = { it.labelFor(language) },
                onValueChange = {
                    playClick()
                    viewModel.setThemeMode(it)
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        SettingsSection(title = language.t("settings.display")) {
            SettingsDropdownRow(
                title = language.t("settings.dateFormat"),
                description = "${language.t("settings.dateFormatDescription")} $datePreview",
                value = settings.dateFormat,
                options = AppDateFormat.entries,
                optionLabel = { it.label },
                onValueChange = {
                    playClick()
                    viewModel.setDateFormat(it)
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        SettingsSection(title = language.t("settings.notifications")) {
            SettingsToggleRow(
                title = language.t("settings.notifications"),
                description = language.t("settings.notificationsDescription"),
                checked = settings.notificationsEnabled,
                onCheckedChange = {
                    playClick()
                    viewModel.setNotificationsEnabled(it)
                }
            )
            SettingsToggleRow(
                title = language.t("settings.sounds"),
                description = language.t("settings.soundsDescription"),
                checked = settings.soundsEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) AppSoundPlayer.playClick()
                    viewModel.setSoundsEnabled(enabled)
                }
            )

            if (settings.soundsEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { AppSoundPlayer.playClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = SettingsAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = language.t("settings.testSound"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (settings.notificationsEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        playClick()
                        AppNotificationHelper.showTestNotification(
                            context = context,
                            message = language.t("settings.testNotificationText")
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SettingsAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = language.t("settings.testNotification"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = language.t("settings.footer"),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun SettingsHeader(language: AppLanguage) {
    Column {
        Text(
            text = language.t("settings.title"),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        Text(
            text = language.t("settings.subtitle"),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SettingsSummaryCard(
    language: AppLanguage,
    themeMode: AppThemeMode,
    dateFormat: AppDateFormat,
    datePreview: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryItem(
                label = language.t("settings.language"),
                value = language.label,
                color = SettingsAccent,
                modifier = Modifier.weight(1f)
            )
            SummaryItem(
                label = language.t("settings.theme"),
                value = themeMode.labelFor(language),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            SummaryItem(
                label = dateFormat.label,
                value = datePreview,
                color = SettingsAccent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.take(1),
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                fontSize = 13.sp,
                lineHeight = 17.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SettingsAccent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SettingsDropdownRow(
    title: String,
    description: String,
    value: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onValueChange: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontSize = 13.sp,
            lineHeight = 17.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = optionLabel(value),
                onValueChange = {},
                readOnly = true,
                label = { Text(title) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = appTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionLabel(option)) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun AppThemeMode.labelFor(language: AppLanguage): String {
    return when (language) {
        AppLanguage.Portuguese,
        AppLanguage.English,
        AppLanguage.Spanish -> when (this) {
            AppThemeMode.Light -> language.t("theme.light")
            AppThemeMode.Dark -> language.t("theme.dark")
            AppThemeMode.System -> language.t("theme.system")
        }
    }
}
