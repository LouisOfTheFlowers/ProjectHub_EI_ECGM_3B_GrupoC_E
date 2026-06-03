package com.example.projecthub.uiscreens.admin.teams

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.admin.AdminTeamUserItem
import com.example.projecthub.uiscreens.components.AppDialogCancelButton
import com.example.projecthub.uiscreens.components.AppDialogConfirmButton

@Composable
internal fun AdminTeamRemovalDialog(
    user: AdminTeamUserItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val language = currentAppSettings().language

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(language.t("teams.removeTitle")) },
        text = { Text(language.t("teams.removeQuestion").format(user.name)) },
        confirmButton = {
            AppDialogConfirmButton(
                text = language.t("common.delete"),
                onClick = onConfirm
            )
        },
        dismissButton = {
            AppDialogCancelButton(
                text = language.t("common.cancel"),
                onClick = onDismiss
            )
        }
    )
}

