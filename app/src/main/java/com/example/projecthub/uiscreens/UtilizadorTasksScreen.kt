package com.example.projecthub.uiscreens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.viewmodel.UtilizadorDashboardState
import com.example.projecthub.viewmodel.UtilizadorTaskObservation
import com.example.projecthub.viewmodel.UtilizadorTaskItem
import coil.compose.AsyncImage
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun UtilizadorTasksSection(
    state: UtilizadorDashboardState,
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
                    CircularProgressIndicator(color = AuthAccent)
                }
            }

            item != null -> TaskObservationsPage(
                item = item,
                isSaving = state.isSaving,
                onBack = onBack,
                onAddObservation = { addObservationTask = item.task }
            )

            else -> Column {
                OutlinedButton(
                    onClick = rememberSoundClick(onBack),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(language.t("user.tasks.back"), color = AuthAccent, fontWeight = FontWeight.Bold)
                }
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
                    CircularProgressIndicator(color = AuthAccent)
                }
            }

            state.tasks.isEmpty() -> {
                TaskMessageCard(
                    title = language.t("user.tasks.emptyTitle"),
                    detail = language.t("user.tasks.emptyDetail")
                )
            }

            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.tasks.forEach { item ->
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

@Composable
private fun UserTaskCard(
    item: UtilizadorTaskItem,
    isSaving: Boolean,
    onOpenObservations: () -> Unit,
    onComplete: () -> Unit
) {
    val language = currentAppSettings().language
    val task = item.task
    val completed = task.status.isCompletedStatus()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.titulo,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (task.descricao?.isNotBlank() == true) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.descricao,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                }

                StatusPill(status = task.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TaskMeta(label = language.t("user.tasks.project"), value = item.projectName.ifBlank { "-" }, modifier = Modifier.weight(1f))
                TaskMeta(label = language.t("common.start"), value = task.data_inicio.toUiDate(), modifier = Modifier.weight(1f))
                TaskMeta(label = language.t("common.end"), value = task.data_fim.toUiDate(), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.recordsCount} ${language.t("tasks.records").lowercase()}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = rememberSoundClick(onOpenObservations),
                        enabled = !isSaving,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(language.t("user.tasks.observations"))
                    }

                    Button(
                        onClick = rememberSoundClick(onComplete),
                        enabled = !isSaving && !completed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProjectHubColors.Success,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (completed) language.t("user.tasks.completed") else language.t("user.tasks.complete"))
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskObservationsPage(
    item: UtilizadorTaskItem,
    isSaving: Boolean,
    onBack: () -> Unit,
    onAddObservation: () -> Unit
) {
    val language = currentAppSettings().language
    val task = item.task

    Column {
        OutlinedButton(
            onClick = rememberSoundClick(onBack),
            enabled = !isSaving,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(language.t("user.tasks.back"), color = AuthAccent, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(18.dp))

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

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = rememberSoundClick(onAddObservation),
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = AuthAccent,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(if (isSaving) language.t("common.saving") else language.t("user.tasks.addObservation"))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (item.observations.isEmpty()) {
            TaskMessageCard(
                title = language.t("user.tasks.noObservationsTitle"),
                detail = language.t("user.tasks.noObservationsDetail")
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item.observations.forEach { observation ->
                    ObservationCard(observation = observation)
                }
            }
        }
    }
}

@Composable
private fun ObservationCard(observation: UtilizadorTaskObservation) {
    val language = currentAppSettings().language
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = observation.observation.texto,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TaskMeta(label = language.t("common.date"), value = observation.record.data.toUiDate(), modifier = Modifier.weight(1f))
                TaskMeta(label = language.t("user.tasks.completion"), value = "${observation.record.taxa_conclusao}%", modifier = Modifier.weight(1f))
                TaskMeta(label = language.t("common.photos"), value = observation.photos.size.toString(), modifier = Modifier.weight(1f))
            }
            observation.photos.firstOrNull()?.let { photo ->
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = photo.foto_url,
                    contentDescription = language.t("profile.photoDescription"),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

@Composable
private fun AddObservationDialog(
    task: TarefaDto,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    val language = currentAppSettings().language
    val context = LocalContext.current
    var text by remember(task.id) { mutableStateOf("") }
    var photoUri by remember(task.id) { mutableStateOf<String?>(null) }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                photoUri = uri.toString()
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = language.t("user.tasks.addObservation"),
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
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(language.t("user.tasks.observationText")) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = rememberSoundClick { photoPicker.launch(arrayOf("image/*")) },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (photoUri == null) language.t("user.tasks.addPhoto") else language.t("user.tasks.changePhoto"))
                }
                if (photoUri != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = language.t("user.tasks.photoSelected"),
                        color = ProjectHubColors.SuccessDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = rememberSoundClick { onSave(text, photoUri) },
                enabled = !isSaving && text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuthAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isSaving) language.t("common.saving") else language.t("common.save"))
            }
        },
        dismissButton = {
            TextButton(
                onClick = rememberSoundClick(onDismiss),
                enabled = !isSaving
            ) {
                Text(language.t("common.cancel"), color = AuthAccent)
            }
        }
    )
}

@Composable
private fun CompleteTaskDialog(
    task: TarefaDto,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    val language = currentAppSettings().language
    var date by remember(task.id) { mutableStateOf(LocalDate.now().toString()) }
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
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text(language.t("user.tasks.completionDate")) },
                    placeholder = { Text(language.t("user.tasks.datePlaceholder")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(language.t("user.tasks.completionLocation")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text(language.t("user.tasks.spentHours")) },
                    placeholder = { Text(language.t("user.tasks.hoursExample")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = rememberSoundClick { onSave(date, location, hours) },
                enabled = !isSaving && date.isNotBlank() && location.isNotBlank() && hours.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProjectHubColors.Success,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isSaving) language.t("common.completing") else language.t("user.tasks.complete"))
            }
        },
        dismissButton = {
            TextButton(
                onClick = rememberSoundClick(onDismiss),
                enabled = !isSaving
            ) {
                Text(language.t("common.cancel"), color = AuthAccent)
            }
        }
    )
}

@Composable
private fun TaskMeta(
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
private fun StatusPill(status: String) {
    val color = when {
        status.isCompletedStatus() -> ProjectHubColors.Success
        status.normalizedStatus() == "PENDENTE" -> ProjectHubColors.Warning
        else -> ProjectHubColors.InfoLight
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = status,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun TaskMessageCard(
    title: String,
    detail: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = detail,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                fontSize = 13.sp
            )
        }
    }
}

private fun String?.toUiDate(): String {
    val date = this?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: return "-"
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

private fun String.isCompletedStatus(): Boolean {
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
