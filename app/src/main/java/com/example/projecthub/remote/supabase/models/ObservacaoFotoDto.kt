package com.example.projecthub.remote.supabase.models

import kotlinx.serialization.Serializable

@Serializable
data class ObservacaoFotoDto(
    val id: Int? = null,
    val observacao_id: Int,
    val foto_url: String
)