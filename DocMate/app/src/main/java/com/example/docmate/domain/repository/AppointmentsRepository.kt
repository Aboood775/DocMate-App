package com.example.docmate.domain.repository

import com.example.docmate.data.DummyDoctorProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

sealed class BookingResult {
    data class Success(val appointmentId: String) : BookingResult()
    data class Failure(val message: String) : BookingResult()
}

/**
 * ============================================================
 * مسؤول عن حجز المواعيد والاستعلام عن التوفر.
 * تم توصيله بـ DummyDoctorProvider لمعرفة السلوتات المتاحة لكل دكتور
 * وبـ Firestore لحفظ الحجوزات فعليًا وعرضها في شاشة Appts.
 * ============================================================
 */
class AppointmentsRepository {

    // مؤقت فقط كـ cache محلي احتياطي
    private val bookedSlots = mutableSetOf<String>() // مفتاح: "doctorId|date|time"

    suspend fun getAvailableSlots(doctorId: String, date: LocalDate): List<com.example.docmate.domain.model.Slot> {
        delay(300)
        val dummySlots = DummyDoctorProvider.getAvailableSlots(doctorId, date)

        return try {
            val firestore = FirebaseFirestore.getInstance()
            val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val snapshot = firestore.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("date", dateString)
                .get()
                .await()

            val bookedTimes = snapshot.documents.mapNotNull { doc ->
                val status = doc.getString("status").orEmpty()
                if (status != "cancelled") {
                    doc.getString("time")
                } else {
                    null
                }
            }.toSet()

            dummySlots.map { slot ->
                slot.copy(available = slot.available && slot.time !in bookedTimes)
            }
        } catch (e: Exception) {
            dummySlots.map { slot ->
                val key = slotKey(doctorId, date, slot.time)
                slot.copy(available = slot.available && key !in bookedSlots)
            }
        }
    }

    suspend fun bookAppointment(
        doctorId: String,
        doctorName: String,
        doctorSpecialty: String,
        doctorInitials: String,
        location: String,
        date: LocalDate,
        time: String
    ): BookingResult {
        delay(400) // محاكاة وقت الرفع

        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return BookingResult.Failure("User not authenticated")

        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val appointmentId = UUID.randomUUID().toString()

        val key = slotKey(doctorId, date, time)
        if (key in bookedSlots) {
            return BookingResult.Failure("This slot is already booked. Please choose another time.")
        }

        try {
            // التحقق من Firestore لعدم تكرار الحجز لنفس الدكتور في نفس الوقت
            val existing = firestore.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("date", dateString)
                .whereEqualTo("time", time)
                .get()
                .await()

            val hasActiveBooking = existing.documents.any { doc ->
                doc.getString("status") != "cancelled"
            }

            if (hasActiveBooking) {
                return BookingResult.Failure("This slot is already booked. Please choose another time.")
            }

            // كتابة الحجز الفعلي في Firestore
            val appointmentData = hashMapOf(
                "doctorId" to doctorId,
                "doctorName" to doctorName,
                "doctorSpecialty" to doctorSpecialty,
                "doctorInitials" to doctorInitials,
                "location" to location,
                "patientId" to uid,
                "date" to dateString,
                "time" to time,
                "status" to "confirmed",
                "createdAt" to Timestamp.now()
            )

            firestore.collection("appointments")
                .document(appointmentId)
                .set(appointmentData)
                .await()

            bookedSlots.add(key)
            return BookingResult.Success(appointmentId = appointmentId)
        } catch (e: Exception) {
            return BookingResult.Failure(e.localizedMessage ?: "Failed to book appointment")
        }
    }

    private fun slotKey(doctorId: String, date: LocalDate, time: String): String =
        "$doctorId|${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}|$time"
}
