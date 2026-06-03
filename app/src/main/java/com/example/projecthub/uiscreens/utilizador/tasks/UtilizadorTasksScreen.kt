package com.example.projecthub.uiscreens.utilizador.tasks

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.viewmodel.utilizador.UtilizadorTaskObservation
import com.example.projecthub.viewmodel.utilizador.UtilizadorTaskItem
import com.example.projecthub.viewmodel.utilizador.UtilizadorTasksState
import java.text.Normalizer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun UtilizadorTasksSection(
    state: UtilizadorTasksState,
    taskObservationsId: Int?,
    onOpenObservations: (Int) -> Unit,
    onBack: () -> Unit,
    onAddObservation: (Int?, String, String?) -> Unit,
    onCompleteTask: (Int?, String, String, String) -> Unit
) {
    val language = currentAppSettings().language
    var addObservationTask by remember { mutableStateOf<TarefaDto?>(null) }
    var completeTask by remember { mutableStateOf<TarefaDto?>(null) }

    if (taskObservationsId != null) {
        val item = state.tasks.firstOrNull { it.task.id == taskObservationsId }
        when {
            state.isLoading || (state.tasks.isEmpty() && state.errorMessage == null) -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = _root_ide_package_.com.example.projecthub.uiscreens.AuthAccent)
                }
            }

            item != null -> TaskObservationsPage(
                item = item,
                isSaving = state.isSaving,
                onBack = onBack,
                onAddObservation = { addObservationTask = item.task }
            )

            else -> Column {
                _root_ide_package_.com.example.projecthub.uiscreens.AppBackButton(
                    text = language.t("user.tasks.back"),
                    onClick = onBack
                )
                Spacer(modifier = Modifier.height(18.dp))
                TaskMessageCard(
                    title = language.t("user.tasks.notFoundTitle"),
                    detail = language.t("user.tasks.notFoundDetail")
                )
            }
        }

        addObservationTask?.let { task ->
            AddObservationDialog(
                task = task,
                isSaving = state.isSaving,
                onDismiss = { addObservationTask = null },
                onSave = { text, photoUri ->
                    onAddObservation(task.id, text, photoUri)
                    addObservationTask = null
                }
            )
        }
        return
    }

    Column {
        Text(
            text = language.t("user.tasks.title"),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = language.t("user.tasks.subtitle"),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(18.dp))

        if (state.errorMessage != null) {
            TaskMessageCard(
                title = language.t("user.tasks.stateTitle"),
                detail = state.errorMessage
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = _root_ide_package_.com.example.projecthub.uiscreens.AuthAccent)
                }
            }

            state.tasks.isEmpty() -> {
                TaskMessageCard(
                    title = language.t("user.tasks.emptyTitle"),
                    detail = language.t("user.tasks.emptyDetail")
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 720.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.tasks,
                        key = { it.task.id ?: it.task.titulo }
                    ) { item ->
                        UserTaskCard(
                            item = item,
                            isSaving = state.isSaving,
                            onOpenObservations = { item.task.id?.let(onOpenObservations) },
                            onComplete = { completeTask = item.task }
                        )
                    }
                }
            }
        }
    }

    completeTask?.let { task ->
        CompleteTaskDialog(
            task = task,
            isSaving = state.isSaving,
            onDismiss = { completeTask = null },
            onSave = { date, location, hours ->
                onCompleteTask(task.id, date, location, hours)
                completeTask = null
            }
        )
    }
}
