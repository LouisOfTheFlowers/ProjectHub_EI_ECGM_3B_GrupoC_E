package com.example.projecthub.remote.supabase.models

import kotlinx.serialization.Serializable

@Serializable
data class ProjectMemberCountDto(
    val projeto_id: Int,
    val membros: Long
)
