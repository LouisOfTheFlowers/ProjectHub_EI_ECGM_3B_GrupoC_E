package com.example.projecthub.uiscreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projecthub.R
import com.example.projecthub.ui.theme.ProjectHubColors

internal val AuthBackground = ProjectHubColors.LightBackground
internal val AuthAccent = ProjectHubColors.Accent
internal val AuthAccentSoft = ProjectHubColors.AccentSoft
internal val AuthText = ProjectHubColors.LightInk

@Composable
internal fun AuthHeader(subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.projecthub_logo),
            contentDescription = "ProjectHub",
            modifier = Modifier.size(180.dp)
        )

        Text(
            text = "ProjectHub",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = AuthText
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.titleLarge,
            color = AuthAccent
        )
    }
}

@Composable
internal fun authTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = AuthText,
        unfocusedTextColor = AuthText,
        disabledTextColor = AuthText.copy(alpha = 0.55f),
        focusedBorderColor = AuthAccent,
        focusedLabelColor = AuthAccent,
        cursorColor = AuthAccent,
        unfocusedBorderColor = ProjectHubColors.BorderSoft,
        unfocusedLabelColor = ProjectHubColors.Muted,
        focusedPlaceholderColor = ProjectHubColors.Muted,
        unfocusedPlaceholderColor = ProjectHubColors.Muted
    )
}

@Composable
internal fun appTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        focusedBorderColor = AuthAccent,
        focusedLabelColor = AuthAccent,
        cursorColor = AuthAccent,
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
internal fun authButtonColors(): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = AuthAccent,
        contentColor = Color.White,
        disabledContainerColor = ProjectHubColors.BorderSoft,
        disabledContentColor = Color.White
    )
}
