package com.example.projecthub.uiscreens.admin

import com.example.projecthub.uiscreens.*

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.R
import com.example.projecthub.viewmodel.AdminReportCard
import com.example.projecthub.viewmodel.AdminReportExport
import com.example.projecthub.viewmodel.AdminReportExportType
import com.example.projecthub.viewmodel.AdminReportSummary
import com.example.projecthub.viewmodel.AdminReportsState
import com.example.projecthub.viewmodel.AdminReportsViewModel
import com.example.projecthub.ui.theme.ProjectHubColors

private val ReportsAccent = AuthAccent
private val ReportsGreen = ProjectHubColors.SuccessDark
private val ReportsOrange = ProjectHubColors.Warning
private val ReportsRed = ProjectHubColors.DangerDark
private val ReportsBlue = ProjectHubColors.Info

@Composable
fun AdminReportsScreen(
    viewModel: AdminReportsViewModel = viewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val language = currentAppSettings().language
    val context = LocalContext.current
    var pendingExport by remember { mutableStateOf<AdminReportExport?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            val export = pendingExport

            if (uri != null && export != null) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(export.content.toByteArray(Charsets.UTF_8))
                    } ?: error(language.t("reports.openFileError"))

                    Toast.makeText(
                        context,
                        language.t("reports.exported").format(export.label.lowercase()),
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    viewModel.setExportError(
                        e.message ?: language.t("reports.exportError")
                    )
                }
            }

            pendingExport = null
        }
    )

    Column {
        ReportsHeader()
        Spacer(modifier = Modifier.height(18.dp))

        when {
            state.isLoading -> ReportsLoading()

            state.errorMessage != null -> {
                val errorMessage = state.errorMessage.orEmpty()
                ReportsError(
                    message = errorMessage,
                    onRetry = viewModel::loadReports
                )
            }

            else -> ReportsContent(
                state = state,
                onExport = { type ->
                    val export = viewModel.buildExport(type) ?: return@ReportsContent
                    pendingExport = export
                    exportLauncher.launch(export.fileName)
                }
            )
        }
    }
}

@Composable
private fun ReportsHeader() {
    val language = currentAppSettings().language
    Column {
        Text(
            text = language.t("reports.adminTitle"),
            color = ProjectHubColors.Ink,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        Text(
            text = language.t("reports.adminSubtitle"),
            color = ProjectHubColors.Muted,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ReportsLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = ReportsAccent)
    }
}

@Composable
private fun ReportsError(
    message: String,
    onRetry: () -> Unit
) {
    val language = currentAppSettings().language
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = message,
                color = ReportsRed,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = rememberSoundClick(onRetry)) {
                Text(
                    text = language.t("common.retry"),
                    color = ReportsAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ReportsContent(
    state: AdminReportsState,
    onExport: (AdminReportExportType) -> Unit
) {
    val language = currentAppSettings().language
    ReportSummaryGrid(summary = state.summary)

    Spacer(modifier = Modifier.height(16.dp))

    state.exportErrorMessage?.let { message ->
        Text(
            text = message,
            color = ReportsRed,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    Text(
        text = language.t("reports.availableExports"),
        color = ProjectHubColors.Ink,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp
    )

    Spacer(modifier = Modifier.height(12.dp))

    state.cards.forEach { card ->
        ReportExportCard(
            card = card,
            onExport = { onExport(card.type) }
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun ReportSummaryGrid(summary: AdminReportSummary) {
    val language = currentAppSettings().language
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryCard(
                label = language.t("reports.users"),
                value = summary.totalUsers.toString(),
                accent = ReportsAccent,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = language.t("reports.projects"),
                value = summary.totalProjects.toString(),
                accent = ReportsBlue,
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryCard(
                label = language.t("reports.tasks"),
                value = summary.totalTasks.toString(),
                accent = ReportsOrange,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = language.t("common.progress"),
                value = "${summary.averageCompletion}%",
                accent = ReportsGreen,
                modifier = Modifier.weight(1f)
            )
        }

        SummaryCard(
            label = language.t("reports.registeredHours"),
            value = "${summary.totalHours.formatOneDecimal()} h",
            accent = ReportsRed,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val language = currentAppSettings().language
    Card(
        modifier = modifier.height(86.dp),
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
            Text(
                text = label,
                color = ProjectHubColors.Muted,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
            Text(
                text = value,
                color = accent,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 25.sp
            )
        }
    }
}

@Composable
private fun ReportExportCard(
    card: AdminReportCard,
    onExport: () -> Unit
) {
    val language = currentAppSettings().language
    val accent = when (card.type) {
        AdminReportExportType.Users -> ReportsAccent
        AdminReportExportType.Projects -> ReportsBlue
        AdminReportExportType.Tasks -> ReportsOrange
    }
    val exportClick = rememberSoundClick(onExport)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    ReportTypeIcon(type = card.type, color = accent)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.title,
                        color = ProjectHubColors.Ink,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.description,
                        color = ProjectHubColors.Muted,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportChip(text = "${card.rows} ${language.t("common.rows")}", color = accent)
                ReportChip(text = card.primaryMetric, color = ReportsGreen)
                ReportChip(text = card.secondaryMetric, color = ProjectHubColors.Muted)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = exportClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                ExportIcon(color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = language.t("common.exportCsv"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ReportChip(
    text: String,
    color: Color
) {
    Text(
        text = text,
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
private fun ReportTypeIcon(
    type: AdminReportExportType,
    color: Color
) {
    val iconRes = when (type) {
        AdminReportExportType.Users -> R.drawable.ic_group_24
        AdminReportExportType.Projects -> R.drawable.ic_folder_24
        AdminReportExportType.Tasks -> R.drawable.ic_tasks_24
    }

    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun ExportIcon(color: Color) {
    Icon(
        painter = painterResource(R.drawable.ic_download_24),
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(18.dp)
    )
}

private fun Float.formatOneDecimal(): String {
    return String.format(java.util.Locale.US, "%.1f", this)
}
