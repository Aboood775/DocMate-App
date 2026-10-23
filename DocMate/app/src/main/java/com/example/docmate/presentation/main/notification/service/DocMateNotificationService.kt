package com.example.docmate.presentation.main.notification.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.docmate.R
import com.example.docmate.presentation.main.activity.MainActivity
import com.example.docmate.presentation.main.notification.model.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class DocMateNotificationService : com.google.firebase.messaging.FirebaseMessagingService() {

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        saveTokenToFirestore(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received from: ${message.from}")

        val title = message.notification?.title ?: message.data["title"] ?: "DocMate"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val type = sanitizeType(message.data["type"])

        showSystemNotification(title, body)
        saveNotificationToFirestore(title, body, type)
    }

    private fun sanitizeType(raw: String?): String {
        return when (raw?.uppercase()) {
            NotificationType.APPOINTMENT_REMINDER -> NotificationType.APPOINTMENT_REMINDER
            NotificationType.PRESCRIPTION_UPDATE -> NotificationType.PRESCRIPTION_UPDATE
            NotificationType.LAB_RESULT -> NotificationType.LAB_RESULT
            else -> NotificationType.SYSTEM
        }
    }

    private fun saveTokenToFirestore(token: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to save FCM token", e)
            }
    }

    private fun saveNotificationToFirestore(title: String, body: String, type: String) {
        val uid = auth.currentUser?.uid ?: return
        val notifRef = firestore.collection("notifications").document()

        val data = hashMapOf(
            "notificationId" to notifRef.id,
            "title" to title,
            "body" to body,
            "type" to type,
            "createdAt" to FieldValue.serverTimestamp(),
            "isRead" to false,
            "receiverId" to uid
        )

        notifRef.set(data)
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to save notification to Firestore", e)
            }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showSystemNotification(title: String, body: String) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(Random.nextInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DocMate Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications from DocMate"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "DocMateFCM"
        private const val CHANNEL_ID = "docmate_notifications"
    }
}
