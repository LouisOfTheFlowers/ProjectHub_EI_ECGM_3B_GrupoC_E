package com.example.projecthub.remote.supabase.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: Long? = null,

    @SerialName("user_id")
    val userId: Int,

    val title: String,
    val message: String,
    val type: String = "INFO",

    @SerialName("related_route")
    val relatedRoute: String? = null,

    @SerialName("related_entity_id")
    val relatedEntityId: Int? = null,

    @SerialName("is_read")
    val isRead: Boolean = false,

    @SerialName("created_at")
    val createdAt: String? = null
)
