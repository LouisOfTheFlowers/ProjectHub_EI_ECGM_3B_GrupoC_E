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
internal fun TaskObservationsPage(
    item: UtilizadorTaskItem,
    isSaving: Boolean,
    onBack: () -> Unit,
    onAddObservation: () -> Unit
) {
    val language = currentAppSettings().language
    val task = item.task

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        _root_ide_package_.com.example.projecthub.uiscreens.AppBackButton(
            text = language.t("user.tasks.back"),
            onClick = onBack,
            enabled = !isSaving
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.titulo,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = language.t("user.tasks.observationsTitle"),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            StatusPill(status = task.status)
        }

        _root_ide_package_.com.example.projecthub.uiscreens.AppObservationsButton(
            text = if (isSaving) language.t("common.saving") else language.t("user.tasks.addObservation"),
            onClick = onAddObservation,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        )

        if (item.observations.isEmpty()) {
            TaskMessageCard(
                title = language.t("user.tasks.noObservationsTitle"),
                detail = language.t("user.tasks.noObservationsDetail")
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(item.observations) { observation ->
                    ObservationCard(observation = observation)
                }
            }
        }
    }
}

@Composable
private fun ObservationCard(observation: UtilizadorTaskObservation) {
    _root_ide_package_.com.example.projecthub.uiscreens.AppObservationCard(
        observation = _root_ide_package_.com.example.projecthub.uiscreens.AppObservationUiModel(
            text = observation.observation.texto,
            date = observation.record.data.toUiDate(),
            completionPercent = observation.record.taxa_conclusao,
            photoUrls = observation.photos.map { it.foto_url }
        )
    )
}

