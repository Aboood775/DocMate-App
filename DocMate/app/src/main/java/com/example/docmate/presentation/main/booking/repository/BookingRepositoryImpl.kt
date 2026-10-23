package com.example.docmate.presentation.main.booking.repository

import com.example.docmate.data.model.Appointment
import com.example.docmate.data.model.AppointmentStatus
import com.example.docmate.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor() : BookingRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val collection
        get() = firestore.collection("appointments")

    override fun getAppointments(): Flow<Resource<List<Appointment>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Resource.Error(Exception("User not authenticated")))
            close()
            return@callbackFlow
        }

        trySend(Resource.Loading)

        val query = collection
            .whereEqualTo("patientId", uid)
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
            val appointments = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                Appointment(
                    id = doc.id,
                    doctorId = data["doctorId"] as? String ?: "",
                    doctorName = data["doctorName"] as? String ?: "",
                    doctorSpecialty = data["doctorSpecialty"] as? String ?: "",
                    doctorInitials = data["doctorInitials"] as? String ?: "",
                    location = data["location"] as? String ?: "",
                    patientId = data["patientId"] as? String ?: "",
                    date = data["date"] as? String ?: "",
                    time = data["time"] as? String ?: "",
                    status = AppointmentStatus.from(data["status"] as? String ?: ""),
                    createdAt = when (val ts = data["createdAt"]) {
                        is Timestamp -> ts.toDate().time
                        is Number -> ts.toLong()
                        else -> 0L
                    },
                )
            }
            trySend(Resource.Success(appointments))
        }

        awaitClose { registration.remove() }
    }

    private fun handleFirestoreError(error: Throwable): Throwable {
        if (error is FirebaseFirestoreException &&
            error.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION
        ) {
            val message = "Could not load appointments. Please ensure the required Firestore indexes are created."
            return Exception(message, error)
        }
        return error
    }
}
