package com.example.projecthub.uiscreens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.R
import com.example.projecthub.remote.supabase.models.UserDto
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.viewmodel.ProfileViewModel

private val ProfileAccent = AuthAccent
private val ProfileGreen = ProjectHubColors.SuccessDark
private val ProfileRed = ProjectHubColors.DangerDark

@Composable
fun ProfileScreen(
    user: UserDto?,
    onUserUpdated: (UserDto) -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state = viewModel.state
    val language = currentAppSettings().language
    val context = LocalContext.current
    var isEditingEmail by remember { mutableStateOf(false) }
    var isEditingPassword by remember { mutableStateOf(false) }
    var isConfirmingDelete by remember { mutableStateOf(false) }
    var emailInput by remember(user?.email) { mutableStateOf(user?.email.orEmpty()) }
    var emailCode by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var successDialogMessage by remember { mutableStateOf<String?>(null) }
    val currentUser = state.user
    val isAdminUser = currentUser?.role.equals("ADMIN", ignoreCase = true)

    LaunchedEffect(user?.id, user?.foto) {
        viewModel.setUser(user)
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                viewModel.updatePhoto(uri.toString(), onUserUpdated)
            }
        }
    )

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        successDialogMessage = message

        if (message.contains("Palavra-passe", ignoreCase = true)) {
            oldPassword = ""
            newPassword = ""
            confirmPassword = ""
            isEditingPassword = false
        }

        if (message.contains("Email", ignoreCase = true)) {
            isEditingEmail = false
            emailCode = ""
        }
    }

    successDialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = {
                successDialogMessage = null
                viewModel.clearMessage()
            },
            title = {
                Text(
                    text = language.t("profile.changeComplete"),
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                TextButton(
                    onClick = rememberSoundClick {
                        successDialogMessage = null
                        viewModel.clearMessage()
                    }
                ) {
                    Text(language.t("common.ok"), color = ProfileAccent, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (isEditingPassword) {
        AlertDialog(
            onDismissRequest = {
                oldPassword = ""
                newPassword = ""
                confirmPassword = ""
                isEditingPassword = false
            },
            title = {
                Text(
                    text = language.t("profile.changePassword"),
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(modifier = Modifier.responsiveDialogBody()) {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text(language.t("profile.oldPassword")) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text(language.t("profile.newPassword")) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(language.t("profile.confirmNewPassword")) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = rememberSoundClick {
                        viewModel.updatePassword(oldPassword, newPassword, confirmPassword)
                    },
                    enabled = !state.isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfileAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (state.isSaving) language.t("common.saving") else language.t("common.save"),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = rememberSoundClick {
                        oldPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                        isEditingPassword = false
                    },
                    enabled = !state.isSaving,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(language.t("common.cancel"))
                }
            }
        )
    }

    if (isEditingEmail && currentUser != null) {
        AlertDialog(
            onDismissRequest = {
                emailInput = currentUser.email
                emailCode = ""
                isEditingEmail = false
            },
            title = {
                Text(
                    text = language.t("profile.changeEmail"),
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(modifier = Modifier.responsiveDialogBody()) {
                    Text(
                        text = language.t("profile.currentEmail").format(currentUser.email),
                        color = ProjectHubColors.Muted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = rememberSoundClick { viewModel.sendEmailChangeCode() },
                        enabled = !state.isSendingEmailCode && !state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (state.emailCodeSent) {
                                language.t("profile.resendCode")
                            } else if (state.isSendingEmailCode) {
                                language.t("profile.sending")
                            } else {
                                language.t("profile.sendCode")
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = emailCode,
                        onValueChange = { emailCode = it },
                        label = { Text(language.t("profile.receivedCode")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text(language.t("profile.newEmail")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = rememberSoundClick {
                        viewModel.updateEmail(emailInput, emailCode, onUserUpdated)
                    },
                    enabled = !state.isSaving && state.emailCodeSent,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfileAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (state.isSaving) language.t("common.saving") else language.t("common.save"),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = rememberSoundClick {
                        emailInput = currentUser.email
                        emailCode = ""
                        isEditingEmail = false
                    },
                    enabled = !state.isSaving,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(language.t("common.cancel"))
                }
            }
        )
    }

    if (isConfirmingDelete && !isAdminUser) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isDeleting) {
                    isConfirmingDelete = false
                }
            },
            title = {
                Text(
                    text = language.t("profile.deleteAccount"),
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Text(language.t("profile.deleteQuestion"))
            },
            confirmButton = {
                Button(
                    onClick = rememberSoundClick { viewModel.deleteAccount(onAccountDeleted) },
                    enabled = !state.isDeleting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfileRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (state.isDeleting) language.t("profile.deleting") else language.t("common.delete"),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = rememberSoundClick { isConfirmingDelete = false },
                    enabled = !state.isDeleting,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(language.t("common.cancel"))
                }
            }
        )
    }

    Column {
        Text(
            text = language.t("profile.title"),
            color = ProjectHubColors.Ink,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        Text(
            text = language.t("profile.subtitle"),
            color = ProjectHubColors.Muted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (currentUser == null) {
            ProfileMessageCard(language.t("profile.loadError"), ProfileRed)
            return@Column
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfilePhoto(
                    photoUri = currentUser.foto,
                    name = currentUser.nome,
                    modifier = Modifier.size(118.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = currentUser.nome,
                    color = ProjectHubColors.Ink,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
                Text(
                    text = "@${currentUser.username}",
                    color = ProjectHubColors.Muted,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = rememberSoundClick { photoPicker.launch(arrayOf("image/*")) },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfileAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = language.t("profile.changePhoto"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!currentUser.foto.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = rememberSoundClick {
                            viewModel.updatePhoto(null, onUserUpdated)
                        },
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = language.t("profile.removePhoto"),
                            color = ProfileRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        state.errorMessage?.let {
            ProfileMessageCard(message = it, color = ProfileRed)
            Spacer(modifier = Modifier.height(12.dp))
        }

        AccountActionCard(title = language.t("profile.emailAssociated")) {
            Text(
                text = currentUser.email,
                color = ProjectHubColors.Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = rememberSoundClick { isEditingEmail = true },
                enabled = !state.isSaving && !state.isDeleting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(language.t("profile.changeEmail"), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AccountActionCard(title = language.t("profile.security")) {
            Text(
                text = language.t("profile.updatePasswordHint"),
                color = ProjectHubColors.Muted,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = rememberSoundClick { isEditingPassword = true },
                enabled = !state.isSaving && !state.isDeleting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(language.t("profile.changePassword"), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!isAdminUser) {
            AccountActionCard(title = language.t("profile.deleteAccount")) {
                Text(
                    text = language.t("profile.deleteHint"),
                    color = ProjectHubColors.Muted,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = rememberSoundClick { isConfirmingDelete = true },
                    enabled = !state.isSaving && !state.isDeleting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = language.t("profile.deleteAccount"),
                        color = ProfileRed,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfilePhoto(
    photoUri: String?,
    name: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(ProfileAccent.copy(alpha = 0.12f))
            .border(3.dp, ProfileAccent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = rememberProfileImageBitmap(photoUri)

        when {
            bitmap != null -> Image(
                bitmap = bitmap,
                contentDescription = currentAppSettings().language.t("profile.photoDescription"),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            else -> Text(
                text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "G",
                color = ProfileAccent,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 42.sp
            )
        }
    }
}

@Composable
internal fun TopBarProfilePhoto(
    photoUri: String?,
    name: String?,
    modifier: Modifier = Modifier
) {
    val bitmap = rememberProfileImageBitmap(photoUri)

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, ProjectHubColors.HeaderContent.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            bitmap != null -> Image(
                bitmap = bitmap,
                contentDescription = currentAppSettings().language.t("profile.title"),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            !name.isNullOrBlank() -> Text(
                text = name.first().uppercaseChar().toString(),
                color = ProfileAccent,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            )

            else -> Image(
                painter = painterResource(id = R.drawable.projecthub_logo),
                contentDescription = currentAppSettings().language.t("profile.title"),
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
private fun rememberProfileImageBitmap(photoUri: String?): androidx.compose.ui.graphics.ImageBitmap? {
    val context = LocalContext.current

    return androidx.compose.runtime.remember(photoUri) {
        if (photoUri.isNullOrBlank()) {
            null
        } else {
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(photoUri))?.use { input ->
                    android.graphics.BitmapFactory.decodeStream(input)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }
}

@Composable
private fun AccountActionCard(
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
private fun ProfileMessageCard(
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
