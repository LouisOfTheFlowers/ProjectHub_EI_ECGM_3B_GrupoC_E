package com.example.projecthub.uiscreens.admin.teams

import com.example.projecthub.uiscreens.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.admin.AdminTeamProjectOption
import com.example.projecthub.viewmodel.admin.AdminTeamsState
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.components.AppSearchField

@Composable
internal fun TeamFilters(
    state: AdminTeamsState,
    onSearchChange: (String) -> Unit,
    onRoleSelected: (String?) -> Unit,
    onProjectSelected: (Int?) -> Unit
) {
    val language = currentAppSettings().language
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            AppSearchField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                placeholder = language.t("teams.searchPlaceholder")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RoleFilter(
                    selectedRole = state.selectedRole,
                    roles = state.roles,
                    onRoleSelected = onRoleSelected,
                    modifier = Modifier.weight(1f)
                )
                ProjectFilter(
                    selectedProjectId = state.selectedProjectId,
                    projects = state.projects,
                    onProjectSelected = onProjectSelected,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleFilter(
    selectedRole: String?,
    roles: List<String>,
    onRoleSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val language = currentAppSettings().language

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedRole?.toRoleFilterLabel(language) ?: language.t("common.allFemale"),
            onValueChange = {},
            readOnly = true,
            label = { Text(language.t("teams.role")) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = appTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(language.t("common.allFemale")) },
                onClick = {
                    onRoleSelected(null)
                    expanded = false
                }
            )
            roles.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role.toRoleFilterLabel(language)) },
                    onClick = {
                        onRoleSelected(role)
                        expanded = false
                    }
                )
            }
        }
    }
}

internal fun String.toRoleFilterLabel(language: AppLanguage): String {
    return when (uppercase()) {
        "ADMIN" -> language.t("role.admin")
        "GESTOR" -> language.t("role.manager")
        "UTILIZADOR" -> language.t("role.user")
        else -> this
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectFilter(
    selectedProjectId: Int?,
    projects: List<AdminTeamProjectOption>,
    onProjectSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val language = currentAppSettings().language
    val selectedProjectName = projects.firstOrNull { it.id == selectedProjectId }?.name ?: language.t("common.all")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedProjectName,
            onValueChange = {},
            readOnly = true,
            label = { Text(language.t("teams.project")) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = appTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(language.t("common.all")) },
                onClick = {
                    onProjectSelected(null)
                    expanded = false
                }
            )
            projects.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project.name) },
                    onClick = {
                        onProjectSelected(project.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

