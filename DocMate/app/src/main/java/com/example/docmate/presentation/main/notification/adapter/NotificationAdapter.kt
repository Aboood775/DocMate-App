package com.example.docmate.presentation.main.notification.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.docmate.R
import com.example.docmate.databinding.ItemNotificationBinding
import com.example.docmate.databinding.ItemNotificationSectionBinding
import com.example.docmate.presentation.main.notification.model.NotificationListItem
import com.example.docmate.presentation.main.notification.model.NotificationModel
import com.example.docmate.presentation.main.notification.model.NotificationType.APPOINTMENT_REMINDER
import com.example.docmate.presentation.main.notification.model.NotificationType.BOOKING_CONFIRMED
import com.example.docmate.presentation.main.notification.model.NotificationType.LAB_RESULT
import com.example.docmate.presentation.main.notification.model.NotificationType.PRESCRIPTION
import com.example.docmate.presentation.main.notification.model.NotificationType.PRESCRIPTION_UPDATE
import com.example.docmate.presentation.main.notification.model.NotificationType.REMINDER
import com.example.docmate.presentation.main.notification.model.NotificationType.REPLY
import com.example.docmate.presentation.main.notification.model.NotificationType.SYSTEM

class NotificationAdapter(
    private val onNotificationClick: (NotificationModel) -> Unit
) : androidx.recyclerview.widget.ListAdapter<NotificationListItem, RecyclerView.ViewHolder>(NotificationDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SECTION = 0
        private const val VIEW_TYPE_NOTIFICATION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is NotificationListItem.Section -> VIEW_TYPE_SECTION
            is NotificationListItem.Item -> VIEW_TYPE_NOTIFICATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SECTION -> {
                val binding = ItemNotificationSectionBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SectionViewHolder(binding)
            }
            else -> {
                val binding = ItemNotificationBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                NotificationViewHolder(binding, onNotificationClick)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is NotificationListItem.Section -> (holder as SectionViewHolder).bind(item)
            is NotificationListItem.Item -> (holder as NotificationViewHolder).bind(item.notification)
        }
    }

    class SectionViewHolder(
        private val binding: ItemNotificationSectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(section: NotificationListItem.Section) {
            binding.tvSectionTitle.text = section.title
        }
    }

    class NotificationViewHolder(
        private val binding: ItemNotificationBinding,
        private val onNotificationClick: (NotificationModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: NotificationModel) {
            binding.tvNotificationTitle.text = notification.title
            binding.tvNotificationBody.text = notification.body
            binding.tvNotificationDate.text = formatTimeAgo(notification.createdAt)

            val context = binding.root.context

            val (iconRes, iconTint) = when (notification.type.uppercase()) {
                APPOINTMENT_REMINDER, BOOKING_CONFIRMED -> Pair(R.drawable.ic_calendar, R.color.colorSuccess)
                PRESCRIPTION_UPDATE, PRESCRIPTION -> Pair(R.drawable.ic_document, R.color.colorPrimary)
                LAB_RESULT, REMINDER -> Pair(R.drawable.ic_clock, R.color.colorWarning)
                REPLY -> Pair(R.drawable.ic_chat, R.color.colorSecondary)
                SYSTEM -> Pair(R.drawable.ic_bell, R.color.colorTextHint)
                else -> Pair(R.drawable.ic_bell, R.color.colorTextHint)
            }

            binding.ivNotificationIcon.setImageResource(iconRes)
            binding.ivNotificationIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, iconTint)
            )

            if (notification.isRead) {
                binding.viewUnreadIndicator.visibility = View.GONE
                binding.cardNotification.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.colorSurface)
                )
                binding.cardNotification.strokeColor =
                    ContextCompat.getColor(context, R.color.colorDivider)
            } else {
                binding.viewUnreadIndicator.visibility = View.VISIBLE
                binding.cardNotification.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.colorUnreadBg)
                )
                binding.cardNotification.strokeColor =
                    ContextCompat.getColor(context, R.color.colorUnreadStroke)
            }

            binding.root.setOnClickListener {
                onNotificationClick(notification)
            }
        }

        private fun formatTimeAgo(timestamp: Long): String {
            val diff = System.currentTimeMillis() - timestamp
            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000}m ago"
                diff < 86400000 -> "${diff / 3600000}h ago"
                else -> "${diff / 86400000}d ago"
            }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationListItem>() {
        override fun areItemsTheSame(
            oldItem: NotificationListItem, newItem: NotificationListItem
        ): Boolean {
            return when {
                oldItem is NotificationListItem.Section && newItem is NotificationListItem.Section ->
                    oldItem.title == newItem.title
                oldItem is NotificationListItem.Item && newItem is NotificationListItem.Item ->
                    oldItem.notification.notificationId == newItem.notification.notificationId
                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: NotificationListItem, newItem: NotificationListItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}
