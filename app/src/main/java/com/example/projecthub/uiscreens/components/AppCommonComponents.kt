package com.example.projecthub.uiscreens.components

import androidx.compose.ui.graphics.Color

data class AppObservationUiModel(
    val text: String,
    val userName: String? = null,
    val date: String? = null,
    val completionPercent: Int? = null,
    val spentHours: Float? = null,
    val location: String? = null,
    val photoUrls: List<String> = emptyList()
)
data class AppDashboardMetric(
    val label: String,
    val value: String,
    val accent: Color,
    val detail: String,
    val iconRes: Int
)
