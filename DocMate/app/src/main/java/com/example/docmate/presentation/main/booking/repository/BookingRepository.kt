package com.example.docmate.presentation.main.booking.repository

import com.example.docmate.data.model.Appointment
import com.example.docmate.util.Resource
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    fun getAppointments(): Flow<Resource<List<Appointment>>>
}
