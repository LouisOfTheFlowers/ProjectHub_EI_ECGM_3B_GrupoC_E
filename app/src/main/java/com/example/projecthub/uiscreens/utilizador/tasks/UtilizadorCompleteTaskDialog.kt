package com.example.projecthub.uiscreens.utilizador.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.uiscreens.components.AppDialogCancelButton
import com.example.projecthub.uiscreens.components.AppDialogConfirmButton
import com.example.projecthub.uiscreens.components.AppTextField
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
internal fun CompleteTaskDialog(
    task: TarefaDto,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    val language = currentAppSettings().language
    val startDate = remember(task.id) { task.data_inicio.toLocalDateOrNull() }
    val minimumCompletionDate = remember(task.id) { startDate?.plusDays(1) }
    val initialDate = remember(task.id) {
        val today = LocalDate.now()
        when {
            minimumCompletionDate == null -> today
            today.isBefore(minimumCompletionDate) -> minimumCompletionDate
            else -> today
        }
    }
    var date by remember(task.id) { mutableStateOf(initialDate.toString()) }
    var location by remember(task.id) { mutableStateOf("") }
    var hours by remember(task.id) { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = language.t("user.tasks.completeTitle"),
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column {
                Text(
                    text = task.titulo,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                CompletionDatePickerField(
                    value = date,
                    label = language.t("user.tasks.completionDate"),
                    minDate = minimumCompletionDate,
                    onDateSelected = { date = it },
                    modifier = Modifier.fillMaxWidth()
                )
                if (startDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A data tem de ser posterior a ${startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = language.t("user.tasks.completionLocation"),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = language.t("user.tasks.spentHours"),
                    placeholder = language.t("user.tasks.hoursExample"),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            AppDialogConfirmButton(
                text = if (isSaving) language.t("common.completing") else language.t("user.tasks.complete"),
                onClick = { onSave(date, location, hours) },
                enabled = !isSaving && date.isNotBlank() && location.isNotBlank() && hours.isNotBlank()
            )
        },
        dismissButton = {
            AppDialogCancelButton(
                text = language.t("common.cancel"),
                onClick = onDismiss,
                enabled = !isSaving
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompletionDatePickerField(
    value: String,
    label: String,
    minDate: LocalDate?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDialogOpen by remember { mutableStateOf(false) }
    val language = currentAppSettings().language
    val selectedDate = value.toLocalDateOrNull()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.toEpochMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant
                    .ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()

                return minDate == null || !date.isBefore(minDate)
            }
        }
    )

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value.toDisplayDate(),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = true,
            singleLine = true,
            label = { Text(label) }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(onClick = rememberSoundClick { isDialogOpen = true })
        )
    }

    if (isDialogOpen) {
        DatePickerDialog(
            onDismissRequest = { isDialogOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selected = Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()

                            onDateSelected(selected.toString())
                        }

                        isDialogOpen = false
                    }
                ) {
                    Text(language.t("common.confirm"), color = _root_ide_package_.com.example.projecthub.uiscreens.AuthAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = rememberSoundClick { isDialogOpen = false }) {
                    Text(language.t("common.cancel"))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

