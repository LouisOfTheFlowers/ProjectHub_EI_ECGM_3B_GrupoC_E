package com.example.projecthub.uiscreens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.rememberSoundClick

@Composable
fun AppObservationCard(
    observation: AppObservationUiModel,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showPhotoPreview: Boolean = true
) {
    val language = currentAppSettings().language
    val clickableModifier = onClick?.let { Modifier.clickable(onClick = rememberSoundClick(it)) } ?: Modifier

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = observation.text,
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                observation.userName?.let {
                    AppObservationMeta(language.t("common.user"), it, Modifier.weight(1f))
                }
                observation.date?.let {
                    AppObservationMeta(language.t("common.date"), it, Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                observation.completionPercent?.let {
                    AppObservationMeta(language.t("user.tasks.completion"), "$it%", Modifier.weight(1f))
                }
                AppObservationMeta(language.t("common.photos"), observation.photoUrls.size.toString(), Modifier.weight(1f))
                observation.spentHours?.let {
                    AppObservationMeta(language.t("common.hours"), "$it h", Modifier.weight(1f))
                }
            }

            observation.location?.takeIf { it.isNotBlank() }?.let { location ->
                Spacer(modifier = Modifier.height(8.dp))
                AppObservationMeta(language.t("common.location"), location, Modifier.fillMaxWidth())
            }

            if (showPhotoPreview) {
                observation.photoUrls.firstOrNull()?.let { photoUrl ->
                    Spacer(modifier = Modifier.height(10.dp))
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = language.t("profile.photoDescription"),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ProjectHubColors.SurfaceSoft)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppObservationMeta(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, color = ProjectHubColors.Muted, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(value, color = ProjectHubColors.Slate, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

