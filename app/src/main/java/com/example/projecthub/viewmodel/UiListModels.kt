package com.example.projecthub.viewmodel

interface ProjectUiListItem {
    val id: Int
    val name: String
    val description: String
    val statusLabel: String
    val startDate: String
    val dueDate: String
}

interface TaskUiListItem {
    val id: Int
    val title: String
    val description: String
    val statusLabel: String
    val startDate: String
    val dueDate: String
    val isCompleted: Boolean
    val isDelayed: Boolean
}
