package com.example.docmate.data.model

enum class AppointmentStatus(val value: String) {
    CONFIRMED("confirmed"),
    PENDING("pending"),
    CANCELLED("cancelled"),
    COMPLETED("completed");

    companion object {
        fun from(value: String): AppointmentStatus =
            entries.find { it.value == value } ?: PENDING
    }
}

data class Appointment(
    val id: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val doctorSpecialty: String = "",
    val doctorInitials: String = "",
    val location: String = "",
    val patientId: String = "",
    val date: String = "",
    val time: String = "",
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val createdAt: Long = 0L,
)
