package com.example.projecthub.remote.supabase

import com.example.projecthub.remote.supabase.models.NotificationDto
import io.github.jan.supabase.postgrest.from

class NotificationRemoteDataSource {

    suspend fun getNotifications(userId: Int): List<NotificationDto> {
        return SupabaseClientProvider.client
            .from("notifications")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeAs<List<NotificationDto>>()
            .sortedByDescending { it.createdAt.orEmpty() }
    }

    suspend fun markAsRead(notificationId: Long) {
        SupabaseClientProvider.client
            .from("notifications")
            .update(mapOf("is_read" to true)) {
                filter {
                    eq("id", notificationId)
                }
            }
    }

    suspend fun markAllAsRead(userId: Int) {
        SupabaseClientProvider.client
            .from("notifications")
            .update(mapOf("is_read" to true)) {
                filter {
                    eq("user_id", userId)
                    eq("is_read", false)
                }
            }
    }

    suspend fun createNotification(
        userId: Int,
        title: String,
        message: String,
        type: String = "INFO",
        relatedRoute: String? = null,
        relatedEntityId: Int? = null
    ) {
        SupabaseClientProvider.client
            .from("notifications")
            .insert(
                mapOf(
                    "user_id" to userId,
                    "title" to title,
                    "message" to message,
                    "type" to type,
                    "related_route" to relatedRoute,
                    "related_entity_id" to relatedEntityId
                )
            )
    }
}
