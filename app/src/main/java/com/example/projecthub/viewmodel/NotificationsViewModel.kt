package com.example.projecthub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.remote.supabase.models.NotificationDto
import com.example.projecthub.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsState(
    val notifications: List<NotificationDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val unreadCount: Int
        get() = notifications.count { !it.isRead }
}

class NotificationsViewModel(
    private val repository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsState())
    val state: StateFlow<NotificationsState> = _state

    fun loadNotifications(userId: Int?) {
        if (userId == null) {
            _state.value = NotificationsState()
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repository.getNotifications(userId)
            _state.update { state ->
                result.fold(
                    onSuccess = { notifications ->
                        state.copy(
                            notifications = notifications,
                            isLoading = false,
                            errorMessage = null
                        )
                    },
                    onFailure = { error ->
                        state.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Não foi possível carregar as notificações."
                        )
                    }
                )
            }
        }
    }

    fun markAsRead(notificationId: Long?) {
        if (notificationId == null) return

        viewModelScope.launch {
            val previousNotifications = _state.value.notifications
            _state.update { state ->
                state.copy(
                    notifications = state.notifications.map { notification ->
                        if (notification.id == notificationId) {
                            notification.copy(isRead = true)
                        } else {
                            notification
                        }
                    }
                )
            }

            val result = repository.markAsRead(notificationId)
            if (result.isFailure) {
                _state.update {
                    it.copy(
                        notifications = previousNotifications,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    fun markAllAsRead(userId: Int?) {
        if (userId == null) return

        viewModelScope.launch {
            val previousNotifications = _state.value.notifications
            _state.update { state ->
                state.copy(notifications = state.notifications.map { it.copy(isRead = true) })
            }

            val result = repository.markAllAsRead(userId)
            if (result.isFailure) {
                _state.update {
                    it.copy(
                        notifications = previousNotifications,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
}
