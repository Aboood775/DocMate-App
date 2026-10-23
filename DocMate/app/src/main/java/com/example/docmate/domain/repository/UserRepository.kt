package com.example.docmate.domain.repository


import com.example.docmate.domain.model.UserProfile
import com.example.docmate.util.Resources
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun getUserProfile(): Resources<UserProfile> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resources.Error("User not logged in")
            val doc = firestore.collection("user_verified").document(uid).get().await()
            if (!doc.exists()) {
                return Resources.Error("Profile not found")
            }
            val profile = UserProfile(
                uid = doc.getString("uid") ?: uid,
                fullName = doc.getString("name") ?: "",
                email = doc.getString("email") ?: "",
                location = doc.getString("location") ?: "",
                isPremium = doc.getBoolean("isPremium") ?: false
            )
            Resources.Success(profile)
        } catch (e: Exception) {
            Resources.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun updateProfile(fullName: String, location: String): Resources<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resources.Error("User not logged in")
            firestore.collection("user_verified").document(uid)
                .update(mapOf("name" to fullName, "location" to location)).await()
            Resources.Success(Unit)
        } catch (e: Exception) {
            Resources.Error(e.message ?: "Update failed")
        }
    }

    suspend fun updateLocation(location: String): Resources<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resources.Error("User not logged in")
            firestore.collection("user_verified").document(uid)
                .update(mapOf("location" to location)).await()
            Resources.Success(Unit)
        } catch (e: Exception) {
            Resources.Error(e.message ?: "Update failed")
        }
    }

    suspend fun updateFcmToken(token: String) {
        try {
            val uid = auth.currentUser?.uid ?: return
            firestore.collection("user_verified").document(uid)
                .update("fcmToken", token).await()
        } catch (e: Exception) {
            // Log error or handle silently for non-critical update
        }
    }
}
