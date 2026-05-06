package com.example.projecthub.remote.supabase.models

import kotlinx.serialization.Serializable

@Serializable
data class TarefaDto(
    val id: Int? = null,
    val titulo: String,
    val descricao: String? = null,
    val projeto_id: Int,
    val status: String = "PENDENTE",
    val data_inicio: String? = null,
    val data_fim: String? = null
)