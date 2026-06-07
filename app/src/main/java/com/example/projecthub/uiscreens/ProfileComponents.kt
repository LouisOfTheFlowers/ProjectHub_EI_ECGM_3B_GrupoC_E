package com.example.projecthub.uiscreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.projecthub.R
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors

private val ProfilePhotoAccent = AuthAccent

@Composable
internal fun ProfilePhoto(
    photoUri: String?,
    name: String,
    modifier: Modifier = Modifier
) {
    val language = currentAppSettings().language
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(ProfilePhotoAccent.copy(alpha = 0.12f))
            .border(3.dp, ProfilePhotoAccent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        ProfilePhotoImage(
            photoUri = photoUri,
            contentDescription = language.t("profile.photoDescription"),
            modifier = Modifier.fillMaxSize(),
            fallback = {
                Text(
                    text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "G",
                    color = ProfilePhotoAccent,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 42.sp
                )
            }
        )
    }
}

@Composable
internal fun TopBarProfilePhoto(
    photoUri: String?,
    name: String?,
    modifier: Modifier = Modifier
) {
    val language = currentAppSettings().language

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, ProjectHubColors.HeaderContent.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        ProfilePhotoImage(
            photoUri = photoUri,
            contentDescription = language.t("profile.title"),
            modifier = Modifier.fillMaxSize(),
            fallback = {
                if (!name.isNullOrBlank()) {
                    Text(
                        text = name.first().uppercaseChar().toString(),
                        color = ProfilePhotoAccent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.projecthub_logo),
                        contentDescription = language.t("profile.title"),
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        )
    }
}

@Composable
private fun ProfilePhotoImage(
    photoUri: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit
) {
    var hasLoadError by remember(photoUri) { mutableStateOf(false) }

    if (photoUri.isNullOrBlank() || hasLoadError) {
        fallback()
    } else {
        AsyncImage(
            model = photoUri,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            onError = { hasLoadError = true }
        )
    }
}

@Composable
internal fun AccountActionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
internal fun ProfileMessageCard(
    message: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Text(
            text = message,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}
