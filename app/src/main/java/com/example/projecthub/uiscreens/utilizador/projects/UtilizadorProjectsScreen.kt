package com.example.projecthub.uiscreens.utilizador.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.uiscreens.components.AppBackButton
import com.example.projecthub.uiscreens.AuthAccent
import com.example.projecthub.viewmodel.utilizador.UtilizadorProjectsState

@Composable
fun UtilizadorProjectsSection(
    state: UtilizadorProjectsState,
    projectHistoryId: Int?,
    onOpenHistory: (Int) -> Unit,
    onBack: () -> Unit
) {
    val language = currentAppSettings().language
    if (projectHistoryId != null) {
        val item = state.projects.firstOrNull { it.project.id == projectHistoryId }
        when {
            state.isLoading || (state.projects.isEmpty() && state.errorMessage == null) -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AuthAccent)
                }
            }

            item != null -> ProjectTaskHistoryPage(
                item = item,
                onBack = onBack
            )

            else -> Column {
                AppBackButton(
                    text = language.t("user.projects.back"),
                    onClick = onBack
                )
                Spacer(modifier = Modifier.height(18.dp))
                ProjectMessageCard(
                    title = language.t("user.projects.notFoundTitle"),
                    detail = language.t("user.projects.notFoundDetail")
                )
            }
        }
        return
    }

    Column {
        Text(
            text = language.t("user.projects.title"),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = language.t("user.projects.subtitle"),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(18.dp))

        if (state.errorMessage != null) {
            ProjectMessageCard(
                title = language.t("user.projects.stateTitle"),
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

            state.projects.isEmpty() -> {
                ProjectMessageCard(
                    title = language.t("user.projects.emptyTitle"),
                    detail = language.t("user.projects.emptyDetail")
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
                        items = state.projects,
                        key = { it.project.id ?: it.project.nome }
                    ) { item ->
                        UserProjectCard(
                            item = item,
                            onClick = { item.project.id?.let(onOpenHistory) }
                        )
                    }
                }
            }
        }
    }

}
