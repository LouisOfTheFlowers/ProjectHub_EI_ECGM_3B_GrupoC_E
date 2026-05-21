package com.example.projecthub.uiscreens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.R

enum class GestorSection {
    Dashboard,
    Projects,
    Tasks,
    Team,
    Reports,
    Settings,
    Profile
}

private val GestorScaffoldAccent = AuthAccent

@Composable
fun GestorScaffold(
    selectedSection: GestorSection,
    onNavigate: (GestorSection) -> Unit,
    profilePhotoUri: String?,
    profileName: String?,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    var isSidebarOpen by remember { mutableStateOf(false) }
    val openSidebar = { isSidebarOpen = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(5.dp, GestorScaffoldAccent)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            GestorTopBar(
                onMenuClick = openSidebar,
                profilePhotoUri = profilePhotoUri,
                profileName = profileName,
                onProfileClick = { onNavigate(GestorSection.Profile) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                content()
            }
        }

        if (isSidebarOpen) {
            GestorSidebarOverlay(
                selectedSection = selectedSection,
                onDismiss = { isSidebarOpen = false },
                onNavigate = { section ->
                    onNavigate(section)
                    isSidebarOpen = false
                },
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun GestorTopBar(
    onMenuClick: () -> Unit,
    profilePhotoUri: String?,
    profileName: String?,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GestorScaffoldAccent)
            .statusBarsPadding()
            .height(62.dp)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onMenuClick),
            contentAlignment = Alignment.Center
        ) {
            GestorMenuIcon(color = Color.White)
        }

        Text(
            text = "Project Hub",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 21.sp
        )

        TopBarProfilePhoto(
            photoUri = profilePhotoUri,
            name = profileName,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(40.dp)
                .clickable(onClick = onProfileClick)
                .padding(4.dp)
        )
    }
}

@Composable
private fun GestorSidebarOverlay(
    selectedSection: GestorSection,
    onDismiss: () -> Unit,
    onNavigate: (GestorSection) -> Unit,
    onLogout: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        GestorSidebar(
            selectedSection = selectedSection,
            onNavigate = onNavigate,
            onLogout = onLogout
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(onClick = onDismiss)
        )
    }
}

@Composable
private fun GestorSidebar(
    selectedSection: GestorSection,
    onNavigate: (GestorSection) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(284.dp)
            .fillMaxHeight()
            .background(Color(0xFF0F1724))
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.projecthub_logo),
                contentDescription = "Project Hub",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(5.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Project Hub",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Gestor",
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        GestorSidebarItem(
            label = "Dashboard",
            icon = GestorSidebarIcon.Dashboard,
            selected = selectedSection == GestorSection.Dashboard,
            onClick = { onNavigate(GestorSection.Dashboard) }
        )
        GestorSidebarItem(
            label = "Meus Projetos",
            icon = GestorSidebarIcon.Projects,
            selected = selectedSection == GestorSection.Projects,
            onClick = { onNavigate(GestorSection.Projects) }
        )
        GestorSidebarItem(
            label = "Gestão de Tarefas",
            icon = GestorSidebarIcon.Tasks,
            selected = selectedSection == GestorSection.Tasks,
            onClick = { onNavigate(GestorSection.Tasks) }
        )
        GestorSidebarItem(
            label = "Minha Equipa",
            icon = GestorSidebarIcon.Team,
            selected = selectedSection == GestorSection.Team,
            onClick = { onNavigate(GestorSection.Team) }
        )
        GestorSidebarItem(
            label = "Relatórios de Projeto",
            icon = GestorSidebarIcon.Reports,
            selected = selectedSection == GestorSection.Reports,
            onClick = { onNavigate(GestorSection.Reports) }
        )
        GestorSidebarItem(
            label = "Definições",
            icon = GestorSidebarIcon.Settings,
            selected = selectedSection == GestorSection.Settings,
            onClick = { onNavigate(GestorSection.Settings) }
        )

        Spacer(modifier = Modifier.weight(1f))

        GestorSidebarItem(
            label = "Sair",
            icon = GestorSidebarIcon.Logout,
            onClick = onLogout
        )
    }
}

@Composable
private fun GestorSidebarItem(
    label: String,
    icon: GestorSidebarIcon,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFF1E293B) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GestorSidebarItemIcon(icon = icon, color = Color.White)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
}

private enum class GestorSidebarIcon {
    Dashboard,
    Projects,
    Tasks,
    Team,
    Reports,
    Settings,
    Logout
}

@Composable
private fun GestorSidebarItemIcon(
    icon: GestorSidebarIcon,
    color: Color
) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)

        when (icon) {
            GestorSidebarIcon.Dashboard -> {
                val cell = size.width * 0.26f
                listOf(
                    Offset(size.width * 0.18f, size.height * 0.18f),
                    Offset(size.width * 0.56f, size.height * 0.18f),
                    Offset(size.width * 0.18f, size.height * 0.56f),
                    Offset(size.width * 0.56f, size.height * 0.56f)
                ).forEach { topLeft ->
                    drawRoundRect(
                        color = color,
                        topLeft = topLeft,
                        size = Size(cell, cell),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                        style = stroke
                    )
                }
            }

            GestorSidebarIcon.Projects -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.14f, size.height * 0.28f),
                    size = Size(size.width * 0.72f, size.height * 0.5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = stroke
                )
                drawLine(color = color, start = Offset(size.width * 0.26f, size.height * 0.28f), end = Offset(size.width * 0.34f, size.height * 0.16f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(size.width * 0.34f, size.height * 0.16f), end = Offset(size.width * 0.5f, size.height * 0.16f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            GestorSidebarIcon.Tasks -> {
                listOf(0.28f, 0.5f, 0.72f).forEach { y ->
                    drawCircle(color = color, radius = 1.8.dp.toPx(), center = Offset(size.width * 0.2f, size.height * y))
                    drawLine(color = color, start = Offset(size.width * 0.34f, size.height * y), end = Offset(size.width * 0.82f, size.height * y), strokeWidth = stroke.width, cap = StrokeCap.Round)
                }
            }

            GestorSidebarIcon.Team -> {
                drawCircle(color = color, radius = size.width * 0.12f, center = Offset(size.width * 0.42f, size.height * 0.34f), style = stroke)
                drawCircle(color = color, radius = size.width * 0.1f, center = Offset(size.width * 0.68f, size.height * 0.42f), style = stroke)
                drawArc(color = color, startAngle = 200f, sweepAngle = 140f, useCenter = false, topLeft = Offset(size.width * 0.22f, size.height * 0.5f), size = Size(size.width * 0.38f, size.height * 0.28f), style = stroke)
                drawArc(color = color, startAngle = 215f, sweepAngle = 110f, useCenter = false, topLeft = Offset(size.width * 0.54f, size.height * 0.58f), size = Size(size.width * 0.3f, size.height * 0.2f), style = stroke)
            }

            GestorSidebarIcon.Reports -> {
                listOf(0.26f, 0.48f, 0.7f).forEachIndexed { index, x ->
                    val barHeight = listOf(0.28f, 0.48f, 0.66f)[index]
                    drawLine(color = color, start = Offset(size.width * x, size.height * 0.82f), end = Offset(size.width * x, size.height * (0.82f - barHeight)), strokeWidth = stroke.width, cap = StrokeCap.Round)
                }
                drawLine(color = color, start = Offset(size.width * 0.14f, size.height * 0.84f), end = Offset(size.width * 0.86f, size.height * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            GestorSidebarIcon.Settings -> {
                drawCircle(color = color, radius = size.width * 0.18f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
                listOf(0f, 60f, 120f, 180f, 240f, 300f).forEach { degrees ->
                    val radians = Math.toRadians(degrees.toDouble())
                    val inner = Offset(
                        x = size.width * (0.5f + kotlin.math.cos(radians).toFloat() * 0.28f),
                        y = size.height * (0.5f + kotlin.math.sin(radians).toFloat() * 0.28f)
                    )
                    val outer = Offset(
                        x = size.width * (0.5f + kotlin.math.cos(radians).toFloat() * 0.38f),
                        y = size.height * (0.5f + kotlin.math.sin(radians).toFloat() * 0.38f)
                    )
                    drawLine(color = color, start = inner, end = outer, strokeWidth = stroke.width, cap = StrokeCap.Round)
                }
            }

            GestorSidebarIcon.Logout -> {
                drawLine(color = color, start = Offset(size.width * 0.2f, size.height * 0.2f), end = Offset(size.width * 0.2f, size.height * 0.8f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(size.width * 0.2f, size.height * 0.2f), end = Offset(size.width * 0.48f, size.height * 0.2f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(size.width * 0.2f, size.height * 0.8f), end = Offset(size.width * 0.48f, size.height * 0.8f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(size.width * 0.42f, size.height * 0.5f), end = Offset(size.width * 0.82f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(size.width * 0.68f, size.height * 0.34f), end = Offset(size.width * 0.84f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(size.width * 0.68f, size.height * 0.66f), end = Offset(size.width * 0.84f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun GestorMenuIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier.size(28.dp)) {
        val strokeWidth = 2.6.dp.toPx()
        val startX = size.width * 0.18f
        val endX = size.width * 0.82f

        listOf(0.3f, 0.5f, 0.7f).forEach { yPosition ->
            drawLine(
                color = color,
                start = Offset(startX, size.height * yPosition),
                end = Offset(endX, size.height * yPosition),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
