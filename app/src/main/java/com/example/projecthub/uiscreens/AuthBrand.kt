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

internal val AuthBackground = Color(0xFFE8F9FC)
internal val AuthAccent = Color(0xFF0A7C91)
internal val AuthAccentSoft = Color(0xFF48B765)
internal val AuthText = Color(0xFF12323A)

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
        focusedBorderColor = AuthAccent,
        focusedLabelColor = AuthAccent,
        cursorColor = AuthAccent,
        unfocusedBorderColor = Color(0xFF80AEB7),
        unfocusedLabelColor = Color(0xFF557A83)
    )
}

@Composable
internal fun authButtonColors(): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = AuthAccent,
        contentColor = Color.White,
        disabledContainerColor = Color(0xFF9FCBD1),
        disabledContentColor = Color.White
    )
}
