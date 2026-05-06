package com.example.projecthub.remote.supabase.models

import kotlinx.serialization.Serializable

@Serializable
data class ObservacaoDto(
    val id: Int? = null,
    val registo_id: Int,
    val texto: String,
    val created_at: String? = null
)