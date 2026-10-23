package com.example.docmate.domain.model

/**
 * يمثل بيانات الدكتور القادمة من Firestore.
 *
 * ملاحظة مهمة: الحقول دي افتراضية بناءً على تصميم فيجما (Home + Doctor Detail).
 * لما يوصلنا شكل الـ Collection الحقيقي من محمد الشحات، يمكن نحتاج نعدل
 * أسماء الحقول هنا بس (الباقي - Adapters/Fragments - ميتأثرش).
 *
 * لازم يكون فيه constructor بدون parameters (no-arg) عشان Firestore
 * يقدر يحول الـ document تلقائيًا للكلاس ده، فكل الحقول معاها default value.
 */
data class Doctor(
    val id: String = "",
    val name: String = "",
    val specialty: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val yearsExperience: Int = 0,
    val patientsCount: Int = 0,
    val location: String = "",
    val consultationFee: Double = 0.0,
    val about: String = "",
    val imageUrl: String = "",
    val availableToday: Boolean = false,
    // ساعات العمل: المفتاح هو رقم اليوم كنص ("0" = Sunday ... "6" = Saturday)
    // والقيمة "start-end" بصيغة 24 ساعة، أو null لو يوم عطلة.
    val workingHours: Map<String, WorkingHoursRange?> = emptyMap()
) {
    /** أول حرفين من الاسم لعرضهم في الأفاتار، مثلاً "Dr. Amira Hassan" -> "AH" */
    fun initials(): String {
        return name
            .removePrefix("Dr. ")
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
    }
}

data class WorkingHoursRange(
    val start: String = "",
    val end: String = ""
)
