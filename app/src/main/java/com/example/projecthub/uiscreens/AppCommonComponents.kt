package com.example.projecthub.uiscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun AppStatusChip(
    text: String,
    backgroundColor: Color = statusColorForLabel(text).copy(alpha = 0.14f),
    contentColor: Color = statusColorForLabel(text),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(contentColor)
        )
        Box(modifier = Modifier.size(6.dp))
        Text(
            text = text,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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

@Composable
fun AppFormLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = ProjectHubColors.Ink,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        modifier = modifier.padding(bottom = 6.dp)
    )
}

@Composable
fun AppBackButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = rememberSoundClick(onClick),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = ProjectHubColors.Ink),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = AuthAccent
) {
    Button(
        onClick = rememberSoundClick(onClick),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = rememberSoundClick(onClick),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = ProjectHubColors.Disabled,
            contentColor = ProjectHubColors.Ink
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AppOutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = rememberSoundClick(onClick),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = AuthAccent,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AppDialogConfirmButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = rememberSoundClick(onClick),
        enabled = enabled,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = AuthAccent,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AppDialogCancelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = rememberSoundClick(onClick),
        enabled = enabled,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = ProjectHubColors.Muted,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AppActionIconButton(
    icon: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val containerColor = if (enabled) color.copy(alpha = 0.12f) else ProjectHubColors.Disabled
    val contentColor = if (enabled) color else ProjectHubColors.Muted

    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .then(
                if (enabled) {
                    Modifier.clickable(onClick = rememberSoundClick(onClick))
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            color = contentColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            maxLines = 1
        )
    }
}

@Composable
fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    colors: TextFieldColors? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = true,
        colors = colors ?: appTextFieldColors(),
        modifier = modifier.fillMaxWidth()
    )
}

fun statusColorForLabel(label: String): Color {
    return when {
        label.contains("concl", ignoreCase = true) -> ProjectHubColors.Success
        label.contains("progres", ignoreCase = true) -> ProjectHubColors.InfoLight
        label.contains("atras", ignoreCase = true) -> ProjectHubColors.Danger
        label.contains("pend", ignoreCase = true) -> ProjectHubColors.Warning
        label.contains("ativo", ignoreCase = true) -> ProjectHubColors.Success
        label.contains("active", ignoreCase = true) -> ProjectHubColors.Success
        label.contains("inativo", ignoreCase = true) -> ProjectHubColors.Muted
        label.contains("inactive", ignoreCase = true) -> ProjectHubColors.Muted
        else -> ProjectHubColors.Warning
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        colors = appTextFieldColors()
    )
}

@Composable
fun AppTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        colors = appTextFieldColors()
    )
}
