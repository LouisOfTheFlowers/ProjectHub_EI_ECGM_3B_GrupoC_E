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
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.viewmodel.GestorProfileViewModel

private val ProfileAccent = AuthAccent
private val ProfileInk = Color(0xFF111827)
private val ProfileMuted = Color(0xFF6B7280)
private val ProfileGreen = Color(0xFF16A34A)
private val ProfileRed = Color(0xFFDC2626)

@Composable
fun ProfileScreen(
    user: UserDto?,
    onUserUpdated: (UserDto) -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: GestorProfileViewModel = viewModel()
) {
    val state = viewModel.state
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
                    text = "Alteração concluída",
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
                    Text("OK", color = ProfileAccent, fontWeight = FontWeight.Bold)
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
                    text = "Alterar palavra-passe",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Palavra-passe antiga") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nova palavra-passe") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar nova palavra-passe") },
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
                        text = if (state.isSaving) "A guardar..." else "Guardar",
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
                    Text("Cancelar")
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
                    text = "Alterar email",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Email atual: ${currentUser.email}",
                        color = ProfileMuted,
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
                                "Reenviar código"
                            } else if (state.isSendingEmailCode) {
                                "A enviar..."
                            } else {
                                "Enviar código para o email atual"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = emailCode,
                        onValueChange = { emailCode = it },
                        label = { Text("Código recebido") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Novo email") },
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
                        text = if (state.isSaving) "A guardar..." else "Guardar",
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
                    Text("Cancelar")
                }
            }
        )
    }

    if (isConfirmingDelete) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isDeleting) {
                    isConfirmingDelete = false
                }
            },
            title = {
                Text(
                    text = "Eliminar conta",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Text("Tens a certeza que queres eliminar a tua conta? Esta ação remove a conta do sistema e termina a sessão.")
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
                        text = if (state.isDeleting) "A eliminar..." else "Eliminar",
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
                    Text("Cancelar")
                }
            }
        )
    }

    Column {
        Text(
            text = "A minha conta",
            color = ProfileInk,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        Text(
            text = "Informações básicas do teu perfil.",
            color = ProfileMuted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (currentUser == null) {
            ProfileMessageCard("Não foi possível carregar a tua conta.", ProfileRed)
            return@Column
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    color = ProfileInk,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
                Text(
                    text = "@${currentUser.username}",
                    color = ProfileMuted,
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
                            text = "Alterar foto de perfil",
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
                            text = "Remover foto",
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

        AccountActionCard(title = "Email associado") {
            Text(
                text = currentUser.email,
                color = ProfileInk,
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
                Text("Alterar email", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AccountActionCard(title = "Seguranca") {
            Text(
                text = "Atualiza a tua palavra-passe de acesso.",
                color = ProfileMuted,
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
                Text("Alterar palavra-passe", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AccountActionCard(title = "Eliminar conta") {
            Text(
                text = "Esta ação remove a tua conta e termina a sessão.",
                color = ProfileMuted,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = rememberSoundClick { isConfirmingDelete = true },
                enabled = !state.isSaving && !state.isDeleting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Eliminar conta",
                    color = ProfileRed,
                    fontWeight = FontWeight.ExtraBold
                )
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
                contentDescription = "Foto de perfil",
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
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        when {
            bitmap != null -> Image(
                bitmap = bitmap,
                contentDescription = "A minha conta",
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
                contentDescription = "A minha conta",
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            color = ProfileInk,
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
