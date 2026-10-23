package com.example.docmate.presentation.main.notification.repository

import com.example.docmate.presentation.main.notification.model.NotificationModel
import com.example.docmate.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining operations on Notifications.
 * Decouples the UI layer from the data source.
 */
interface NotificationRepository {
    /**
     * Retrieves the current list of notifications.
     */
    fun getNotifications(): Flow<Resource<List<NotificationModel>>>

    fun markAsRead(notificationId: String): Flow<Resource<Boolean>>

    fun markAllAsRead(): Flow<Resource<Boolean>>
}
