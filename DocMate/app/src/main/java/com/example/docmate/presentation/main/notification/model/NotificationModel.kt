package com.example.docmate.presentation.main.notification.model

/**
 * Model class representing a User Notification.
 * Tracks message content, category type, timestamp, and read status.
 */
data class NotificationModel(
    val notificationId: String,
    val title: String,
    val body: String,
    val type: String, // e.g. "BOOKING_CONFIRMED", "REMINDER", "SYSTEM"
    val createdAt: Long,
    val isRead: Boolean
)
