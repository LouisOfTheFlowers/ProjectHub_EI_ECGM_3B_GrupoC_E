package com.example.projecthub.uiscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.TextRange
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

private val AddProjectAccent = AuthAccent
private val AddProjectInk = ProjectHubColors.Ink
private val AddProjectMuted = ProjectHubColors.Muted
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
    val backClick = rememberSoundClick(onBack)
    val createClick = rememberSoundClick {
        onCreate(name.text, description.text, startDate, endDate, selectedManager?.id)
    }

    Column {
        TextButton(
            onClick = backClick,
            colors = ButtonDefaults.textButtonColors(contentColor = AddProjectInk)
        ) {
            Text(
                text = language.t("addProject.back"),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = language.t("addProject.title"),
                    color = AddProjectInk,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                FormLabel(language.t("addProject.name"))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text
                    ),
                    placeholder = { Text(language.t("addProject.namePlaceholder")) }
                )
                AccentCharacterRow(
                    onInsert = { character -> name = name.insertText(character) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                FormLabel(language.t("projects.description"))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text
                    ),
                    placeholder = { Text(language.t("addProject.descriptionPlaceholder")) }
                )
                AccentCharacterRow(
                    onInsert = { character -> description = description.insertText(character) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                FormLabel(language.t("addProject.startDate"))
                DatePickerField(
                    value = startDate,
                    onClick = rememberSoundClick { activeDateField = DateField.Start }
                )

                Spacer(modifier = Modifier.height(14.dp))

                FormLabel(language.t("addProject.endDate"))
                DatePickerField(
                    value = endDate,
                    onClick = rememberSoundClick { activeDateField = DateField.End }
                )

                Spacer(modifier = Modifier.height(14.dp))

                FormLabel(language.t("addProject.manager"))
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

                Button(
                    onClick = createClick,
                    enabled = !state.isCreating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AddProjectAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (state.isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = language.t("addProject.submit"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = backClick,
                    enabled = !state.isCreating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProjectHubColors.Disabled,
                        contentColor = AddProjectInk
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = language.t("common.cancel"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
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

@Composable
private fun AccentCharacterRow(onInsert: (String) -> Unit) {
    val characters = listOf("á", "à", "ã", "â", "é", "ê", "í", "ó", "õ", "ô", "ú", "ç")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        characters.forEach { character ->
            val click = rememberSoundClick { onInsert(character) }
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AddProjectAccent.copy(alpha = 0.12f))
                    .clickable(onClick = click)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = character,
                    color = AddProjectAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

private fun TextFieldValue.insertText(value: String): TextFieldValue {
    val start = minOf(selection.start, selection.end)
    val end = maxOf(selection.start, selection.end)
    val updatedText = text.replaceRange(start, end, value)
    val cursor = start + value.length

    return copy(
        text = updatedText,
        selection = TextRange(cursor)
    )
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
            color = if (value.isBlank()) AddProjectMuted else AddProjectInk,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
        Text(
            text = "📅",
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
                Text("OK")
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
        color = AddProjectInk,
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
                color = if (selectedManager == null) AddProjectMuted else AddProjectInk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                text = if (expanded) "^" else "v",
                color = AddProjectMuted,
                fontWeight = FontWeight.Bold
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            managers.forEach { manager ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = manager.name,
                            color = AddProjectInk,
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
