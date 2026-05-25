package com.example.projecthub.uiscreens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.ui.theme.ProjectHubColors

private data class IntroSlide(
    val title: String,
    val description: String,
    val visual: IntroVisual
)

private enum class IntroVisual {
    Welcome,
    Projects,
    Tasks,
    Team,
    Reports,
    Progress,
    Settings
}

@Composable
fun IntroSliderScreen(
    role: String,
    onFinished: () -> Unit
) {
    val slides = remember(role) { slidesForRole(role) }
    var currentIndex by remember(slides) { mutableIntStateOf(0) }
    val slide = slides[currentIndex]
    val isLast = currentIndex == slides.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProjectHubColors.LightBackground)
            .padding(horizontal = 28.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onFinished) {
                Text(
                    text = "Saltar",
                    color = ProjectHubColors.Ink,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        IntroIllustration(
            visual = slide.visual,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = slide.title,
                    color = ProjectHubColors.Accent,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 23.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = slide.description,
                    color = ProjectHubColors.Muted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                IntroDots(
                    total = slides.size,
                    selectedIndex = currentIndex
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (isLast) {
                            onFinished()
                        } else {
                            currentIndex += 1
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProjectHubColors.SuccessDark,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isLast) "Começar Agora  ->" else "Seguinte  ->",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroIllustration(
    visual: IntroVisual,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            val cardWidth = size.width * 0.74f
            val cardHeight = size.height * 0.72f
            val left = (size.width - cardWidth) / 2f
            val top = size.height * 0.12f

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(cardWidth, cardHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx(), 18.dp.toPx())
            )

            drawRoundRect(
                color = ProjectHubColors.Accent.copy(alpha = 0.08f),
                topLeft = Offset(left + 18.dp.toPx(), top + 18.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(cardWidth - 36.dp.toPx(), cardHeight - 36.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx(), 14.dp.toPx())
            )

            val accent = ProjectHubColors.Accent
            val green = ProjectHubColors.SuccessDark
            val orange = ProjectHubColors.Warning
            val blue = ProjectHubColors.Info
            val stroke = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)

            when (visual) {
                IntroVisual.Welcome -> {
                    drawCircle(accent, radius = 28.dp.toPx(), center = Offset(size.width * 0.5f, top + 58.dp.toPx()))
                    drawLine(accent, Offset(size.width * 0.39f, top + 122.dp.toPx()), Offset(size.width * 0.61f, top + 122.dp.toPx()), strokeWidth = stroke.width, cap = StrokeCap.Round)
                    drawLine(green, Offset(size.width * 0.43f, top + 154.dp.toPx()), Offset(size.width * 0.57f, top + 154.dp.toPx()), strokeWidth = stroke.width, cap = StrokeCap.Round)
                }

                IntroVisual.Projects -> {
                    repeat(3) { index ->
                        val y = top + 48.dp.toPx() + index * 46.dp.toPx()
                        drawRoundRect(
                            color = listOf(accent, green, orange)[index].copy(alpha = 0.18f),
                            topLeft = Offset(left + 42.dp.toPx(), y),
                            size = androidx.compose.ui.geometry.Size(cardWidth - 84.dp.toPx(), 26.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }
                }

                IntroVisual.Tasks -> {
                    repeat(4) { index ->
                        val y = top + 40.dp.toPx() + index * 34.dp.toPx()
                        drawCircle(if (index < 2) green else orange, 7.dp.toPx(), Offset(left + 58.dp.toPx(), y + 8.dp.toPx()))
                        drawLine(ProjectHubColors.Slate, Offset(left + 80.dp.toPx(), y + 8.dp.toPx()), Offset(left + cardWidth - 58.dp.toPx(), y + 8.dp.toPx()), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                    }
                }

                IntroVisual.Team -> {
                    listOf(0.36f, 0.5f, 0.64f).forEachIndexed { index, x ->
                        drawCircle(listOf(accent, green, blue)[index], 22.dp.toPx(), Offset(size.width * x, top + 82.dp.toPx()))
                        drawLine(ProjectHubColors.Slate, Offset(size.width * x - 24.dp.toPx(), top + 128.dp.toPx()), Offset(size.width * x + 24.dp.toPx(), top + 128.dp.toPx()), strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
                    }
                }

                IntroVisual.Reports -> {
                    listOf(0.32f to 74f, 0.46f to 116f, 0.6f to 92f, 0.74f to 145f).forEachIndexed { index, item ->
                        val x = size.width * item.first
                        drawRoundRect(
                            color = listOf(accent, blue, orange, green)[index],
                            topLeft = Offset(x, top + cardHeight - item.second.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(22.dp.toPx(), item.second.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }

                IntroVisual.Progress -> {
                    drawArc(green, startAngle = 145f, sweepAngle = 250f, useCenter = false, topLeft = Offset(size.width * 0.34f, top + 44.dp.toPx()), size = androidx.compose.ui.geometry.Size(110.dp.toPx(), 110.dp.toPx()), style = stroke)
                    val path = Path().apply {
                        moveTo(size.width * 0.43f, top + 100.dp.toPx())
                        lineTo(size.width * 0.48f, top + 118.dp.toPx())
                        lineTo(size.width * 0.58f, top + 82.dp.toPx())
                    }
                    drawPath(path, color = green, style = stroke)
                }

                IntroVisual.Settings -> {
                    repeat(3) { index ->
                        val y = top + 52.dp.toPx() + index * 42.dp.toPx()
                        drawLine(ProjectHubColors.Slate, Offset(left + 54.dp.toPx(), y), Offset(left + cardWidth - 54.dp.toPx(), y), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                        drawCircle(listOf(accent, green, orange)[index], 12.dp.toPx(), Offset(left + 92.dp.toPx() + index * 54.dp.toPx(), y))
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroDots(
    total: Int,
    selectedIndex: Int
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(width = if (index == selectedIndex) 22.dp else 7.dp, height = 7.dp)
                    .clip(CircleShape)
                    .background(if (index == selectedIndex) ProjectHubColors.SuccessDark else ProjectHubColors.BorderSoft)
            )
        }
    }
}

private fun slidesForRole(role: String): List<IntroSlide> {
    return when (role.trim().uppercase()) {
        "ADMIN" -> listOf(
            IntroSlide(
                title = "Bem-vindo ao painel Admin",
                description = "Acompanha o estado geral da plataforma, utilizadores ativos e evolução dos projetos.",
                visual = IntroVisual.Welcome
            ),
            IntroSlide(
                title = "Organiza projetos",
                description = "Cria projetos, define datas, escolhe gestores e acompanha o progresso de cada equipa.",
                visual = IntroVisual.Projects
            ),
            IntroSlide(
                title = "Gere tarefas e equipas",
                description = "Cria tarefas por projeto, associa utilizadores e ajusta roles quando a equipa muda.",
                visual = IntroVisual.Team
            ),
            IntroSlide(
                title = "Exporta estatísticas",
                description = "Consulta relatórios executivos e exporta dados por utilizador, projeto ou tarefa.",
                visual = IntroVisual.Reports
            )
        )

        "GESTOR" -> listOf(
            IntroSlide(
                title = "Bem-vindo à gestão",
                description = "Aqui vês apenas os projetos onde foste associado como gestor.",
                visual = IntroVisual.Welcome
            ),
            IntroSlide(
                title = "Controla os teus projetos",
                description = "Acompanha prazos, progresso, membros associados e detalhes de cada projeto.",
                visual = IntroVisual.Projects
            ),
            IntroSlide(
                title = "Distribui tarefas",
                description = "Cria, edita e acompanha tarefas dos teus projetos, associando os utilizadores certos.",
                visual = IntroVisual.Tasks
            ),
            IntroSlide(
                title = "Avalia e exporta",
                description = "Consulta a tua equipa, vê tarefas concluídas, médias de avaliação e relatórios filtrados.",
                visual = IntroVisual.Reports
            )
        )

        else -> listOf(
            IntroSlide(
                title = "Bem-vindo ao Project Hub",
                description = "Acompanha os projetos e tarefas em que participas de forma simples e centralizada.",
                visual = IntroVisual.Welcome
            ),
            IntroSlide(
                title = "Segue as tuas tarefas",
                description = "Consulta tarefas atribuídas, datas, estado atual e o projeto a que pertencem.",
                visual = IntroVisual.Tasks
            ),
            IntroSlide(
                title = "Regista o teu progresso",
                description = "Adiciona observações, tempo gasto e evidências para documentar o trabalho concluído.",
                visual = IntroVisual.Progress
            )
        )
    }
}
