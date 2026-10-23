package com.example.docmate.domain.model

/**
 * سلوت وقت واحد لدكتور في يوم معين.
 * مش دايمًا جاي من Firestore مباشرة - ممكن يتحسب محليًا من ساعات
 * عمل الدكتور (Doctor.workingHours) ثم نتأكد من توفره بمقارنته
 * بالحجوزات الموجودة في appointments.
 */
data class Slot(
    val time: String,       // مثال: "10:00 AM"
    val available: Boolean
)
