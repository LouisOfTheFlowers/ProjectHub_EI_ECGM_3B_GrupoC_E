package com.example.projecthub.uiscreens.admin.teams

import com.example.projecthub.uiscreens.*

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.rememberSoundClick
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.admin.AdminTeamProjectOption
import com.example.projecthub.viewmodel.admin.AdminTeamUserItem
import com.example.projecthub.viewmodel.admin.AdminTeamEditableRoles
import com.example.projecthub.viewmodel.admin.AdminTeamsState
import com.example.projecthub.viewmodel.admin.AdminTeamsViewModel
import com.example.projecthub.ui.theme.ProjectHubColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UserRoleEditor(
    user: AdminTeamUserItem,
    isUpdating: Boolean,
    isDeleting: Boolean,
    onRoleSelected: (AdminTeamUserItem, String) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val language = currentAppSettings().language
    val deleteClick = rememberSoundClick(onDelete)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (!isUpdating) {
                    expanded = !expanded
                }
            },
            modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                value = user.role.toRoleFilterLabel(language),
                onValueChange = {},
                readOnly = true,
                enabled = !isUpdating,
                label = { Text(language.t("teams.role")) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = appTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        enabled = !isUpdating
                    )
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                AdminTeamEditableRoles.forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role.toRoleFilterLabel(language)) },
                        onClick = {
                            onRoleSelected(user, role)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (isUpdating) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = TeamsAccent
            )
        }

        Button(
            onClick = deleteClick,
            enabled = !isUpdating && !isDeleting,
            colors = ButtonDefaults.buttonColors(
                containerColor = TeamsRed,
                contentColor = Color.White,
                disabledContainerColor = TeamsRed.copy(alpha = 0.45f),
                disabledContentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isDeleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(
                    text = language.t("teams.remove"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

