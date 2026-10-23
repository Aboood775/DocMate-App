package com.example.docmate.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject

class AuthRepository @Inject constructor( private val auth: FirebaseAuth,
        private val firestore: FirebaseFirestore) {


    fun isLoggedIn(): Boolean = auth.currentUser?.isEmailVerified == true

    fun signInWithEmail(
        email: String,
        password: String,
        onVerified: () -> Unit,
        onNotVerified: () -> Unit,
        onError: (String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = auth.currentUser
                if (user == null) {
                    onError(null)
                    return@addOnSuccessListener
                }

                user.reload()
                    .addOnSuccessListener {
                        val reloadedUser = auth.currentUser
                        if (reloadedUser?.isEmailVerified == true) {
                            movePasswordUserToVerified(reloadedUser, onVerified, onError)
                        } else {
                            auth.signOut()
                            onNotVerified()
                        }
                    }
                    .addOnFailureListener { error ->
                        onError(error.localizedMessage)
                    }
            }
            .addOnFailureListener { error ->
                onError(error.localizedMessage)
            }
    }

    fun signInWithGoogle(
        idToken: String?,
        onSuccess: () -> Unit,
        onMissingToken: () -> Unit,
        onAuthError: (String?) -> Unit,
        onSaveError: (String?) -> Unit
    ) {
        if (idToken.isNullOrBlank()) {
            onMissingToken()
            return
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { saveGoogleUser(onSuccess, onSaveError) }
            .addOnFailureListener { error ->
                onAuthError(error.localizedMessage)
            }
    }

    fun createAccount(
        fullName: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onRegisterError: (String?) -> Unit,
        onSaveError: (String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    onRegisterError(null)
                } else {
                    updateProfileAndSendVerification(user, fullName, email, onSuccess, onRegisterError, onSaveError)
                }
            }
            .addOnFailureListener { error ->
                onRegisterError(error.localizedMessage)
            }
    }

    fun sendResetLink(email: String, onSuccess: () -> Unit, onError: (String?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error ->
                onError(error.localizedMessage)
            }
    }

    private fun saveGoogleUser(onSuccess: () -> Unit, onError: (String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onError(null)
            return
        }

        val userRef = firestore.collection("user_verified").document(user.uid)
        userRef.get()
            .addOnSuccessListener { snapshot ->
                val data = mutableMapOf<String, Any?>(
                    "uid" to user.uid,
                    "name" to (user.displayName ?: ""),
                    "email" to (user.email ?: ""),
                    "provider" to "google",
                    "emailVerified" to true,
                    "status" to "verified",
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                if (!snapshot.exists()) {
                    data["createdAt"] = FieldValue.serverTimestamp()
                }

                userRef.set(data, SetOptions.merge())
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { error ->
                        onError(error.localizedMessage)
                    }
            }
            .addOnFailureListener { error ->
                onError(error.localizedMessage)
            }
    }

    private fun updateProfileAndSendVerification(
        user: FirebaseUser,
        fullName: String,
        email: String,
        onSuccess: () -> Unit,
        onRegisterError: (String?) -> Unit,
        onSaveError: (String?) -> Unit
    ) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(fullName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener {
                user.sendEmailVerification()
                    .addOnSuccessListener {
                        savePendingPasswordUser(user.uid, fullName, email, onSuccess, onSaveError)
                    }
                    .addOnFailureListener { error ->
                        onRegisterError(error.localizedMessage)
                    }
            }
    }

    private fun savePendingPasswordUser(
        uid: String,
        fullName: String,
        email: String,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ) {
        val userData = hashMapOf(
            "uid" to uid,
            "name" to fullName,
            "email" to email,
            "provider" to "password",
            "emailVerified" to false,
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("user_pending").document(uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                auth.signOut()
                onSuccess()
            }
            .addOnFailureListener { error ->
                onError(error.localizedMessage)
            }
    }

    private fun movePasswordUserToVerified(user: FirebaseUser, onSuccess: () -> Unit, onError: (String?) -> Unit) {
        val pendingRef = firestore.collection("user_pending").document(user.uid)
        val verifiedRef = firestore.collection("user_verified").document(user.uid)

        pendingRef.get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.getString("name") ?: user.displayName.orEmpty()
                val email = snapshot.getString("email") ?: user.email.orEmpty()
                val createdAt = snapshot.get("createdAt") ?: FieldValue.serverTimestamp()
                val verifiedData = mutableMapOf<String, Any?>(
                    "uid" to user.uid,
                    "name" to name,
                    "email" to email,
                    "provider" to "password",
                    "emailVerified" to true,
                    "status" to "verified",
                    "createdAt" to createdAt,
                    "verifiedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                verifiedRef.set(verifiedData, SetOptions.merge())
                    .addOnSuccessListener {
                        pendingRef.delete()
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { error ->
                                onError(error.localizedMessage)
                            }
                    }
                    .addOnFailureListener { error ->
                        onError(error.localizedMessage)
                    }
            }
            .addOnFailureListener { error ->
                onError(error.localizedMessage)
            }
    }
}
