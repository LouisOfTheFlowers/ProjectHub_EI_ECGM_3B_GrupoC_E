package com.example.projecthub.remote.supabase.models

import kotlinx.serialization.Serializable

@Serializable
data class RegistoTarefaDto(
    val id: Int? = null,
    val tarefa_id: Int,
    val user_id: Int,
    val data: String,
    val local: String? = null,
    val taxa_conclusao: Int,
    val tempo_gasto: Float? = null,
    val created_at: String? = null
)