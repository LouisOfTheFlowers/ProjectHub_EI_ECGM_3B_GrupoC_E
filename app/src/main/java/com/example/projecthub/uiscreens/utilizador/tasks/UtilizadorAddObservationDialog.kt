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
internal fun AddObservationDialog(
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
                _root_ide_package_.com.example.projecthub.uiscreens.AppTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = language.t("user.tasks.observationText"),
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
            _root_ide_package_.com.example.projecthub.uiscreens.AppDialogConfirmButton(
                text = if (isSaving) language.t("common.saving") else language.t("common.save"),
                onClick = { onSave(text, photoUri) },
                enabled = !isSaving && text.isNotBlank()
            )
        },
        dismissButton = {
            _root_ide_package_.com.example.projecthub.uiscreens.AppDialogCancelButton(
                text = language.t("common.cancel"),
                onClick = onDismiss,
                enabled = !isSaving
            )
        }
    )
}

