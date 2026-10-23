package com.example.docmate.domain.repository

import com.example.docmate.data.DummyDoctorProvider
import com.example.docmate.domain.model.Doctor
import kotlinx.coroutines.delay

/**
 * مسؤول عن جلب بيانات الدكاترة.
 *
 * حاليًا بيستخدم DummyDoctorProvider كمصدر بيانات مؤقت.
 * الـ delay بيحاكي وقت تحميل حقيقي من الشبكة.
 *
 * ═══════════════════════════════════════════════════════════════
 * TODO (لما يوصل شكل الـ Collection الحقيقي من Firestore):
 *
 * 1. احقن FirebaseFirestore في الـ constructor:
 *        class DoctorsRepository @Inject constructor(
 *            private val firestore: FirebaseFirestore
 *        )
 *
 * 2. استبدل getDoctors() بـ:
 *        suspend fun getDoctors(): List<Doctor> {
 *            val snapshot = firestore.collection("doctors").get().await()
 *            return snapshot.toObjects(Doctor::class.java)
 *        }
 *
 * 3. استبدل getDoctorById() بـ:
 *        suspend fun getDoctorById(doctorId: String): Doctor? {
 *            val snapshot = firestore.collection("doctors")
 *                .document(doctorId).get().await()
 *            return snapshot.toObject(Doctor::class.java)
 *        }
 *
 * 4. احذف DummyDoctorProvider.kt من data/.
 * ═══════════════════════════════════════════════════════════════
 */
class DoctorsRepository {

    suspend fun getDoctors(): List<Doctor> {
        delay(400) // محاكاة وقت تحميل من الشبكة
        return DummyDoctorProvider.getDoctors()
    }

    suspend fun getDoctorById(doctorId: String): Doctor? {
        delay(300)
        return DummyDoctorProvider.getDoctorById(doctorId)
    }
}
