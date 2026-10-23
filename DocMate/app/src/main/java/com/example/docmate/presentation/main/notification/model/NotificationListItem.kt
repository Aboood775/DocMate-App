package com.example.docmate.presentation.main.notification.model

sealed class NotificationListItem {
    data class Section(val title: String) : NotificationListItem()
    data class Item(val notification: NotificationModel) : NotificationListItem()
}
