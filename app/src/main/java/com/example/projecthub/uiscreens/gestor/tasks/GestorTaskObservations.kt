package com.example.projecthub.uiscreens.gestor.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.components.AppObservationCard
import com.example.projecthub.uiscreens.components.AppObservationUiModel
import com.example.projecthub.viewmodel.gestor.GestorTaskInfoObservation

@Composable
internal fun TaskObservationsSection(
    observations: List<GestorTaskInfoObservation>
) {
    val language = currentAppSettings().language

    Text(
        text = language.t("user.tasks.observations"),
        color = ProjectHubColors.Ink,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp
    )
    Spacer(modifier = Modifier.height(10.dp))

    if (observations.isEmpty()) {
        TaskInfoMessageCard(
            title = language.t("user.tasks.noObservationsTitle"),
            detail = language.t("user.tasks.noObservationsDetail")
        )
    } else {
        LazyColumn(
            modifier = Modifier.heightIn(max = 620.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = observations,
                key = { it.id ?: it.text.hashCode() }
            ) { observation ->
                TaskObservationRow(observation = observation)
            }
        }
    }
}


@Composable
private fun TaskObservationRow(observation: GestorTaskInfoObservation) {
    AppObservationCard(
        observation = AppObservationUiModel(
            text = observation.text,
            userName = observation.userName,
            date = observation.date,
            spentHours = observation.spentHours,
            photoUrls = observation.photoUrls
        )
    )
}

