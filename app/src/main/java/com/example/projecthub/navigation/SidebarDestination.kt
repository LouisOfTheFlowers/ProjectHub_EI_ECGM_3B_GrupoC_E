package com.example.projecthub.navigation

data class SidebarDestination<T>(
    val route: String,
    val label: String,
    val icon: T
)

