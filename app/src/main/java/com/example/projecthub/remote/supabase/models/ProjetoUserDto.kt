package com.example.projecthub.remote.supabase.models

import kotlinx.serialization.Serializable

@Serializable
data class ProjetoUserDto(
    val id: Int? = null,
    val projeto_id: Int,
    val user_id: Int
)