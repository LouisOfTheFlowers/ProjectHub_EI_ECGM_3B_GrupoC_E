package com.example.projecthub.repository

import com.example.projecthub.remote.supabase.NotificationRemoteDataSource
import com.example.projecthub.remote.supabase.models.NotificationDto

class NotificationRepository(
    private val remoteDataSource: NotificationRemoteDataSource = NotificationRemoteDataSource()
) {

    suspend fun getNotifications(userId: Int): Result<List<NotificationDto>> {
        return try {
            Result.success(remoteDataSource.getNotifications(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(notificationId: Long): Result<Unit> {
        return try {
            remoteDataSource.markAsRead(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllAsRead(userId: Int): Result<Unit> {
        return try {
            remoteDataSource.markAllAsRead(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createNotification(
        userId: Int,
        title: String,
        message: String,
        type: String = "INFO",
        relatedRoute: String? = null,
        relatedEntityId: Int? = null
    ): Result<Unit> {
        return try {
            remoteDataSource.createNotification(
                userId = userId,
                title = title,
                message = message,
                type = type,
                relatedRoute = relatedRoute,
                relatedEntityId = relatedEntityId
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun notifyUsers(
        userIds: Iterable<Int>,
        title: String,
        message: String,
        type: String = "INFO",
        relatedRoute: String? = null,
        relatedEntityId: Int? = null
    ) {
        userIds.distinct().forEach { userId ->
            createNotification(
                userId = userId,
                title = title,
                message = message,
                type = type,
                relatedRoute = relatedRoute,
                relatedEntityId = relatedEntityId
            )
        }
    }
}
