package com.example.projecthub.uiscreens.admin

import com.example.projecthub.uiscreens.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.AdminProjectManager
import com.example.projecthub.viewmodel.AdminProjectsState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.projecthub.ui.theme.ProjectHubColors

private val AddProjectAccent = _root_ide_package_.com.example.projecthub.uiscreens.AuthAccent
private val AddProjectRed = ProjectHubColors.Danger

@Composable
fun AdminAddProjectScreen(
    state: AdminProjectsState,
    onBack: () -> Unit,
    onCreate: (String, String, String, String, Int?) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var selectedManager by remember { mutableStateOf<AdminProjectManager?>(null) }
    var activeDateField by remember { mutableStateOf<DateField?>(null) }
    val language = currentAppSettings().language
    Column {
        _root_ide_package_.com.example.projecthub.uiscreens.AppBackButton(
            text = language.t("addProject.back"),
            onClick = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        _root_ide_package_.com.example.projecthub.uiscreens.AppFormCard(title = language.t("addProject.title")) {
            _root_ide_package_.com.example.projecthub.uiscreens.AppFormLabel(language.t("addProject.name"))
            _root_ide_package_.com.example.projecthub.uiscreens.AppTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text
                ),
                placeholder = language.t("addProject.namePlaceholder")
            )
            Spacer(modifier = Modifier.height(14.dp))

            _root_ide_package_.com.example.projecthub.uiscreens.AppFormLabel(language.t("projects.description"))
            _root_ide_package_.com.example.projecthub.uiscreens.AppTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text
                ),
                placeholder = language.t("addProject.descriptionPlaceholder")
            )
            Spacer(modifier = Modifier.height(14.dp))

            _root_ide_package_.com.example.projecthub.uiscreens.AppFormLabel(language.t("addProject.startDate"))
            DatePickerField(
                value = startDate,
                onClick = rememberSoundClick { activeDateField = DateField.Start }
            )

            Spacer(modifier = Modifier.height(14.dp))

            _root_ide_package_.com.example.projecthub.uiscreens.AppFormLabel(language.t("addProject.endDate"))
            DatePickerField(
                value = endDate,
                onClick = rememberSoundClick { activeDateField = DateField.End }
            )

            Spacer(modifier = Modifier.height(14.dp))

            _root_ide_package_.com.example.projecthub.uiscreens.AppFormLabel(language.t("addProject.manager"))
            ManagerDropdown(
                selectedManager = selectedManager,
                managers = state.managers,
                onManagerSelected = { selectedManager = it }
            )

            if (state.createErrorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.createErrorMessage,
                    color = AddProjectRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            _root_ide_package_.com.example.projecthub.uiscreens.AppPrimaryButton(
                text = language.t("addProject.submit"),
                onClick = {
                    onCreate(name.text, description.text, startDate, endDate, selectedManager?.id)
                },
                enabled = !state.isCreating,
                isLoading = state.isCreating,
                containerColor = AddProjectAccent
            )

            Spacer(modifier = Modifier.height(10.dp))

            _root_ide_package_.com.example.projecthub.uiscreens.AppSecondaryButton(
                text = language.t("common.cancel"),
                onClick = onBack,
                enabled = !state.isCreating
            )
        }
    }

    if (activeDateField != null) {
        ProjectDatePickerDialog(
            onDismiss = { activeDateField = null },
            onDateSelected = { formattedDate ->
                when (activeDateField) {
                    DateField.Start -> startDate = formattedDate
                    DateField.End -> endDate = formattedDate
                    null -> Unit
                }
                activeDateField = null
            }
        )
    }
}


private enum class DateField {
    Start,
    End
}

@Composable
private fun DatePickerField(
    value: String,
    onClick: () -> Unit
) {
    val click = rememberSoundClick(onClick)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, ProjectHubColors.Border, RoundedCornerShape(8.dp))
            .clickable(onClick = click)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = value.ifBlank { "dd/mm/aaaa" },
            color = if (value.isBlank()) ProjectHubColors.Muted else ProjectHubColors.Ink,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
        Text(
            text = "ðŸ“…",
            fontSize = 18.sp
        )
    }
}

@Composable
private fun ProjectDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    val language = currentAppSettings().language

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        onDateSelected(selectedMillis.toProjectDateText())
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(language.t("common.ok"))
            }
        },
        dismissButton = {
            TextButton(onClick = rememberSoundClick(onDismiss)) {
                Text(language.t("common.cancel"))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun Long.toProjectDateText(): String {
    val date = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        color = ProjectHubColors.Ink,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun ManagerDropdown(
    selectedManager: AdminProjectManager?,
    managers: List<AdminProjectManager>,
    onManagerSelected: (AdminProjectManager) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val language = currentAppSettings().language
    val openClick = rememberSoundClick { expanded = true }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, ProjectHubColors.Border, RoundedCornerShape(8.dp))
                .clickable(enabled = managers.isNotEmpty(), onClick = openClick)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedManager?.name ?: language.t("addProject.managerPlaceholder"),
                color = if (selectedManager == null) ProjectHubColors.Muted else ProjectHubColors.Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            _root_ide_package_.com.example.projecthub.uiscreens.AppExpandIcon(expanded = expanded)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ProjectHubColors.LightSurface)
        ) {
            managers.forEach { manager ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = manager.name,
                            color = ProjectHubColors.Ink,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        expanded = false
                        onManagerSelected(manager)
                    }
                )
            }
        }
    }
}
