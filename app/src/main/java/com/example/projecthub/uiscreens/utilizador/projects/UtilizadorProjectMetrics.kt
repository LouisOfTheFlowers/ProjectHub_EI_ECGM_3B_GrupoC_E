package com.example.projecthub.uiscreens.utilizador.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.AppBackButton
import com.example.projecthub.uiscreens.AppMessageCard
import com.example.projecthub.uiscreens.AppOutlinedActionButton
import com.example.projecthub.uiscreens.AppStatusChip
import com.example.projecthub.uiscreens.AuthAccent
import com.example.projecthub.uiscreens.isLandscapeLayout
import com.example.projecthub.viewmodel.utilizador.UtilizadorProjectItem
import com.example.projecthub.viewmodel.utilizador.UtilizadorProjectsState
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun ProjectMeta(
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
internal fun ProjectMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Column {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
internal fun HistoryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppOutlinedActionButton(
        text = text,
        onClick = onClick,
        modifier = modifier.height(50.dp)
    )
}

