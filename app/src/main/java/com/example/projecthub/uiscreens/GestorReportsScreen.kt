package com.example.projecthub.uiscreens

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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.GestorReportCard
import com.example.projecthub.viewmodel.GestorReportExport
import com.example.projecthub.viewmodel.GestorReportExportType
import com.example.projecthub.viewmodel.GestorReportSummary
import com.example.projecthub.viewmodel.GestorReportsState
import com.example.projecthub.viewmodel.GestorReportsViewModel

private val GestorReportsAccent = AuthAccent
private val GestorReportsInk = Color(0xFF111827)
private val GestorReportsMuted = Color(0xFF6B7280)
private val GestorReportsGreen = Color(0xFF16A34A)
private val GestorReportsOrange = Color(0xFFF97316)
private val GestorReportsRed = Color(0xFFDC2626)
private val GestorReportsBlue = Color(0xFF2563EB)

@Composable
fun GestorReportsScreen(
    gestorId: Int?,
    viewModel: GestorReportsViewModel = viewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current
    var pendingExport by remember { mutableStateOf<GestorReportExport?>(null) }

    LaunchedEffect(gestorId) {
        viewModel.loadReports(gestorId)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            val export = pendingExport

            if (uri != null && export != null) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(export.content.toByteArray(Charsets.UTF_8))
                    } ?: error("Nao foi possivel abrir o ficheiro selecionado.")

                    Toast.makeText(
                        context,
                        "Relatorio de ${export.label.lowercase()} exportado.",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    viewModel.setExportError(
                        e.message ?: "Nao foi possivel exportar o relatorio."
                    )
                }
            }

            pendingExport = null
        }
    )

    Column {
        GestorReportsHeader()
        Spacer(modifier = Modifier.height(18.dp))

        when {
            state.isLoading -> GestorReportsLoading()

            state.errorMessage != null -> GestorReportsError(
                message = state.errorMessage,
                onRetry = { viewModel.loadReports(gestorId) }
            )

            else -> GestorReportsContent(
                state = state,
                onExport = { type ->
                    val export = viewModel.buildExport(type) ?: return@GestorReportsContent
                    pendingExport = export
                    exportLauncher.launch(export.fileName)
                }
            )
        }
    }
}

@Composable
private fun GestorReportsHeader() {
    Column {
        Text(
            text = "Relatorios de Projeto",
            color = GestorReportsInk,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        Text(
            text = "Exporta estatisticas dos teus projetos, equipa e tarefas.",
            color = GestorReportsMuted,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun GestorReportsLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = GestorReportsAccent)
    }
}

@Composable
private fun GestorReportsError(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = message,
                color = GestorReportsRed,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = rememberSoundClick(onRetry)) {
                Text(
                    text = "Tentar novamente",
                    color = GestorReportsAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GestorReportsContent(
    state: GestorReportsState,
    onExport: (GestorReportExportType) -> Unit
) {
    GestorReportSummaryGrid(summary = state.summary)

    Spacer(modifier = Modifier.height(16.dp))

    state.exportErrorMessage?.let { message ->
        Text(
            text = message,
            color = GestorReportsRed,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    Text(
        text = "Exportacoes disponiveis",
        color = GestorReportsInk,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp
    )

    Spacer(modifier = Modifier.height(12.dp))

    state.cards.forEach { card ->
        GestorReportExportCard(
            card = card,
            onExport = { onExport(card.type) }
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun GestorReportSummaryGrid(summary: GestorReportSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GestorSummaryCard(
                label = "Equipa",
                value = summary.totalUsers.toString(),
                accent = GestorReportsAccent,
                modifier = Modifier.weight(1f)
            )
            GestorSummaryCard(
                label = "Projetos",
                value = summary.totalProjects.toString(),
                accent = GestorReportsBlue,
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GestorSummaryCard(
                label = "Tarefas",
                value = summary.totalTasks.toString(),
                accent = GestorReportsOrange,
                modifier = Modifier.weight(1f)
            )
            GestorSummaryCard(
                label = "Progresso",
                value = "${summary.averageCompletion}%",
                accent = GestorReportsGreen,
                modifier = Modifier.weight(1f)
            )
        }

        GestorSummaryCard(
            label = "Horas registadas",
            value = "${summary.totalHours.formatOneDecimal()} h",
            accent = GestorReportsRed,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun GestorSummaryCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(86.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                color = GestorReportsMuted,
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
private fun GestorReportExportCard(
    card: GestorReportCard,
    onExport: () -> Unit
) {
    val language = currentAppSettings().language
    val accent = when (card.type) {
        GestorReportExportType.Users -> GestorReportsAccent
        GestorReportExportType.Projects -> GestorReportsBlue
        GestorReportExportType.Tasks -> GestorReportsOrange
    }
    val exportClick = rememberSoundClick(onExport)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    GestorReportTypeIcon(type = card.type, color = accent)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.title,
                        color = GestorReportsInk,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.description,
                        color = GestorReportsMuted,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GestorReportChip(text = "${card.rows} linhas", color = accent)
                GestorReportChip(text = card.primaryMetric, color = GestorReportsGreen)
                GestorReportChip(text = card.secondaryMetric, color = GestorReportsMuted)
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
                GestorExportIcon(color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Exportar CSV",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun GestorReportChip(
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
private fun GestorReportTypeIcon(
    type: GestorReportExportType,
    color: Color
) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)

        when (type) {
            GestorReportExportType.Users -> {
                drawCircle(
                    color = color,
                    radius = size.width * 0.14f,
                    center = Offset(size.width * 0.38f, size.height * 0.34f),
                    style = stroke
                )
                drawCircle(
                    color = color,
                    radius = size.width * 0.12f,
                    center = Offset(size.width * 0.66f, size.height * 0.42f),
                    style = stroke
                )
                drawArc(
                    color = color,
                    startAngle = 205f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.14f, size.height * 0.5f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.45f, size.height * 0.28f),
                    style = stroke
                )
                drawArc(
                    color = color,
                    startAngle = 215f,
                    sweepAngle = 110f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.5f, size.height * 0.58f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.34f, size.height * 0.2f),
                    style = stroke
                )
            }

            GestorReportExportType.Projects -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.18f, size.height * 0.24f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.54f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = stroke
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.3f, size.height * 0.42f),
                    end = Offset(size.width * 0.7f, size.height * 0.42f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.3f, size.height * 0.6f),
                    end = Offset(size.width * 0.58f, size.height * 0.6f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }

            GestorReportExportType.Tasks -> {
                listOf(0.3f, 0.5f, 0.7f).forEach { y ->
                    drawCircle(
                        color = color,
                        radius = 1.8.dp.toPx(),
                        center = Offset(size.width * 0.2f, size.height * y)
                    )
                    drawLine(
                        color = color,
                        start = Offset(size.width * 0.34f, size.height * y),
                        end = Offset(size.width * 0.82f, size.height * y),
                        strokeWidth = stroke.width,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
private fun GestorExportIcon(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val strokeWidth = 2.2.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.18f),
            end = Offset(size.width * 0.5f, size.height * 0.62f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.32f, size.height * 0.46f),
            end = Offset(size.width * 0.5f, size.height * 0.64f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.68f, size.height * 0.46f),
            end = Offset(size.width * 0.5f, size.height * 0.64f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.22f, size.height * 0.82f),
            end = Offset(size.width * 0.78f, size.height * 0.82f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun Float.formatOneDecimal(): String {
    return String.format(java.util.Locale.US, "%.1f", this)
}
