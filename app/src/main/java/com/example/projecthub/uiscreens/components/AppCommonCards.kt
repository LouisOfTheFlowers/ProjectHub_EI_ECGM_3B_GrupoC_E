package com.example.projecthub.uiscreens.components

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.R
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors

@Composable
fun AppSurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        content()
    }
}

@Composable
fun AppDashboardMetricCard(
    metric: AppDashboardMetric,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(108.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(metric.accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(metric.iconRes),
                        contentDescription = null,
                        tint = metric.accent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                Column {
                    Text(
                        text = metric.label,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = metric.detail,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = metric.value,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 36.sp
            )
        }
    }
}

@Composable
fun AppCompactStatCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
    heightDp: Int = 82
) {
    Card(
        modifier = modifier.height(heightDp.dp),
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
            Text(text = title, color = ProjectHubColors.Muted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(text = value, color = accent, fontWeight = FontWeight.ExtraBold, fontSize = 27.sp)
        }
    }
}

@Composable
fun AppInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = ProjectHubColors.Muted, fontSize = 12.sp)
        Text(value, color = ProjectHubColors.Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun AppDetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, color = ProjectHubColors.Muted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(value, color = ProjectHubColors.Slate, fontSize = 14.sp)
    }
}

@Composable
fun AppMessageCard(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    titleSize: Int = 16
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = ProjectHubColors.Ink, fontWeight = FontWeight.ExtraBold, fontSize = titleSize.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(detail, color = ProjectHubColors.Muted, fontSize = 13.sp)
        }
    }
}

@Composable
fun AppOfflineState(
    modifier: Modifier = Modifier
) {
    val language = currentAppSettings().language

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(ProjectHubColors.Warning.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_wifi_off_24),
                    contentDescription = language.t("offline.title"),
                    tint = ProjectHubColors.Warning,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = language.t("offline.title"),
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 21.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = language.t("offline.detail"),
                color = ProjectHubColors.Muted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun AppFormCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AppSurfaceCard(modifier = modifier) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 25.sp
            )
            Box(modifier = Modifier.height(18.dp))
            content()
        }
    }
}

