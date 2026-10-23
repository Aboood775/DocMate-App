package com.example.docmate.presentation.main.notification.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docmate.presentation.main.notification.model.NotificationListItem
import com.example.docmate.presentation.main.notification.model.NotificationModel
import com.example.docmate.presentation.main.notification.repository.NotificationRepository
import com.example.docmate.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _notificationsState = MutableLiveData<Resource<List<NotificationListItem>>>()
    val notificationsState: LiveData<Resource<List<NotificationListItem>>> = _notificationsState

    private val _unreadCount = MutableLiveData(0)
    val unreadCount: LiveData<Int> = _unreadCount

    private val _markAllReadState = MutableLiveData<Resource<Boolean>>()
    val markAllReadState: LiveData<Resource<Boolean>> = _markAllReadState

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            repository.getNotifications().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _notificationsState.value = Resource.Success(buildSectionedList(resource.data))
                        _unreadCount.value = resource.data.count { !it.isRead }
                    }
                    is Resource.Empty -> {
                        _notificationsState.value = Resource.Empty
                        _unreadCount.value = 0
                    }
                    is Resource.Error -> {
                        _notificationsState.value = resource
                    }
                    is Resource.Loading -> {
                        _notificationsState.value = Resource.Loading
                    }
                }
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(notificationId).collectLatest { resource ->
                if (resource is Resource.Success && resource.data) {
                    loadNotifications()
                }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead().collectLatest { resource ->
                _markAllReadState.value = resource
                if (resource is Resource.Success && resource.data) {
                    loadNotifications()
                }
            }
        }
    }

    private fun buildSectionedList(notifications: List<NotificationModel>): List<NotificationListItem> {
        val sorted = notifications.sortedByDescending { it.createdAt }
        val items = mutableListOf<NotificationListItem>()
        val now = System.currentTimeMillis()
        val todayStart = getStartOfDay(now)
        val yesterdayStart = todayStart - 86400000L

        var addedToday = false
        var addedYesterday = false
        var addedOlder = false

        for (notification in sorted) {
            if (notification.createdAt >= todayStart) {
                if (!addedToday) {
                    items.add(NotificationListItem.Section("Today"))
                    addedToday = true
                }
                items.add(NotificationListItem.Item(notification))
            } else if (notification.createdAt >= yesterdayStart) {
                if (!addedYesterday) {
                    items.add(NotificationListItem.Section("Yesterday"))
                    addedYesterday = true
                }
                items.add(NotificationListItem.Item(notification))
            } else {
                if (!addedOlder) {
                    items.add(NotificationListItem.Section("Older"))
                    addedOlder = true
                }
                items.add(NotificationListItem.Item(notification))
            }
        }
        return items
    }

    private fun getStartOfDay(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
