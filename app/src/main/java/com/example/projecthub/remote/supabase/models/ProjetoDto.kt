package com.example.projecthub.remote.supabase.models

import kotlinx.serialization.Serializable

@Serializable
data class ProjetoDto(
    val id: Int? = null,
    val nome: String,
    val descricao: String? = null,
    val data_inicio: String? = null,
    val data_fim: String? = null,
    val status: String = "PENDENTE",
    val gestor_id: Int? = null,
    val created_at: String? = null
)