package com.example.projecthub.remote.supabase.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int? = null,
    val nome: String,
    val username: String,
    val email: String,
    val password: String,
    val foto: String? = null,
    val role: String = "UTILIZADOR",

    @SerialName("created_at")
    val createdAt: String? = null,

    val status: String = "ATIVO"
)