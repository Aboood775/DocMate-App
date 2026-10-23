package com.example.docmate.presentation.main.notification.repository

import com.example.docmate.presentation.main.notification.model.NotificationModel
import com.example.docmate.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val collection
        get() = firestore.collection("notifications")

    override fun getNotifications(): Flow<Resource<List<NotificationModel>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Resource.Error(Exception("User not authenticated")))
            close()
            return@callbackFlow
        }

        trySend(Resource.Loading)

        val query = collection
            .whereEqualTo("receiverId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(handleFirestoreError(error)))
                return@addSnapshotListener
            }
            if (snapshot == null || snapshot.isEmpty) {
                trySend(Resource.Empty)
                return@addSnapshotListener
            }
            val notifications = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                NotificationModel(
                    notificationId = data["notificationId"] as? String ?: doc.id,
                    title = data["title"] as? String ?: "",
                    body = data["body"] as? String ?: "",
                    type = data["type"] as? String ?: "",
                    createdAt = when (val ts = data["createdAt"]) {
                        is com.google.firebase.Timestamp -> ts.toDate().time
                        is Number -> ts.toLong()
                        else -> 0L
                    },
                    isRead = data["isRead"] as? Boolean ?: false
                )
            }
            trySend(Resource.Success(notifications))
        }

        awaitClose { registration.remove() }
    }

    override fun markAsRead(notificationId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            collection.document(notificationId)
                .update("isRead", true)
                .await()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(handleFirestoreError(e)))
        }
    }

    override fun markAllAsRead(): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                emit(Resource.Error(Exception("User not authenticated")))
                return@flow
            }
            val unreadDocs = collection
                .whereEqualTo("receiverId", uid)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            if (unreadDocs.isEmpty) {
                emit(Resource.Success(true))
                return@flow
            }

            val batch = firestore.batch()
            unreadDocs.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(handleFirestoreError(e)))
        }
    }

    private fun handleFirestoreError(error: Throwable): Throwable {
        if (error is FirebaseFirestoreException &&
            error.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION
        ) {
            val message = "Could not load notifications. Please ensure the required Firestore indexes are created."
            return Exception(message, error)
        }
        return error
    }
}
