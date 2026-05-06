package com.example.projecthub.remote.supabase.models

import kotlinx.serialization.Serializable

@Serializable
data class TarefaUserDto(
    val id: Int? = null,
    val tarefa_id: Int,
    val user_id: Int
)