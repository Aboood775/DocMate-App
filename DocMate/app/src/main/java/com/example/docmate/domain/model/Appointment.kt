package com.example.docmate.domain.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

/**
 * ميعاد محجوز، بيترفع وبيتقرى من collection اسمه "appointments" في Firestore.
 *
 * @DocumentId يخلي Firestore يحط الـ document ID تلقائيًا هنا بدل
 * ما نضطر نخزنه كـ field مكرر جوه الـ document نفسه.
 */
data class Appointment(
    @DocumentId
    val id: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val doctorSpecialty: String = "",
    val doctorInitials: String = "",
    val location: String = "",
    val patientId: String = "patient_1", // ثابت مؤقتًا، حسب اتفاق الفريق
    val date: String = "",   // "YYYY-MM-DD"
    val time: String = "",   // "10:00 AM"
    val status: String = AppointmentStatus.CONFIRMED.value,
    val createdAt: Date? = null
)

/**
 * الحالات الممكنة للميعاد - بتتطابق مع التابات الثلاثة في شاشة
 * "My appointments" (Upcoming / Completed / Cancelled).
 * Upcoming بتشمل كل من CONFIRMED و PENDING.
 */
enum class AppointmentStatus(val value: String) {
    CONFIRMED("confirmed"),
    PENDING("pending"),
    CANCELLED("cancelled"),
    COMPLETED("completed");

    companion object {
        fun fromValue(value: String): AppointmentStatus =
            entries.firstOrNull { it.value == value } ?: CONFIRMED
    }
}
