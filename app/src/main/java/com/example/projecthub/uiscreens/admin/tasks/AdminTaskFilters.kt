package com.example.projecthub.uiscreens.admin.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projecthub.settings.currentAppSettings
import com.example.projecthub.settings.t
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.viewmodel.admin.AdminTaskStatusFilter
import com.example.projecthub.viewmodel.admin.AdminTasksState
import com.example.projecthub.ui.theme.ProjectHubColors
import com.example.projecthub.uiscreens.components.AppDropdownField

@Composable
internal fun TaskFilters(
    state: AdminTasksState,
    onSearchChange: (String) -> Unit,
    onStatusChange: (AdminTaskStatusFilter) -> Unit
) {
    val language = currentAppSettings().language
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ProjectHubColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(language.t("tasks.search")) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            StatusDropdown(
                selected = state.selectedStatus,
                onOptionSelected = onStatusChange
            )
        }
    }
}

@Composable
private fun StatusDropdown(
    selected: AdminTaskStatusFilter,
    onOptionSelected: (AdminTaskStatusFilter) -> Unit
) {
    val language = currentAppSettings().language
    AppDropdownField(
        selected = selected,
        options = AdminTaskStatusFilter.entries,
        label = { (it ?: selected).translatedLabel(language) },
        onOptionSelected = onOptionSelected
    )
}

internal fun AdminTaskStatusFilter.translatedLabel(language: AppLanguage): String {
    return when (this) {
        AdminTaskStatusFilter.All -> language.t("filters.tasks.all")
        AdminTaskStatusFilter.Pending -> language.t("filters.tasks.pending")
        AdminTaskStatusFilter.Completed -> language.t("filters.tasks.completed")
    }
}

