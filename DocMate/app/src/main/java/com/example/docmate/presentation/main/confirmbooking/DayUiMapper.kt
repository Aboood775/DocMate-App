package com.example.docmate.presentation.main.confirmbooking

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * بيانات اليوم الواحد المعروض في القائمة الأفقية (مثل "Sat 2").
 * ده الـ UI Model بتاع اليوم - مختلف عن LocalDate الخام لأنه جاهز
 * للعرض مباشرة (اسم اليوم مختصر + رقمه كنص).
 */
data class DayUiModel(
    val date: LocalDate,
    val dayName: String,   // "Sat"
    val dayNumber: String  // "2"
)

/**
 * Mapper مسؤوليته الوحيدة تحويل LocalDate (بيانات خام) لـ DayUiModel
 * (بيانات جاهزة للعرض). مفصول عمدًا عن DaysAdapter لأن دور الـ Adapter
 * هو "العرض" بس (Presentation)، ومنطق التحويل/الـ Mapping محله هنا
 * أو في الـ ViewModel، مش جوه الـ Adapter نفسه.
 */
object DayUiMapper {
    fun map(date: LocalDate): DayUiModel = DayUiModel(
        date = date,
        dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
        dayNumber = date.dayOfMonth.toString()
    )

    fun map(dates: List<LocalDate>): List<DayUiModel> = dates.map { map(it) }
}
