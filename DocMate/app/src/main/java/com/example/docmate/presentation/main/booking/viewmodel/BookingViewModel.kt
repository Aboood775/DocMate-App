package com.example.docmate.presentation.main.booking.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docmate.presentation.main.booking.repository.BookingRepository
import com.example.docmate.data.model.Appointment
import com.example.docmate.data.model.AppointmentStatus
import com.example.docmate.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val repository: BookingRepository
) : ViewModel() {

    private val _appointmentsState = MutableLiveData<Resource<List<Appointment>>>()
    val appointmentsState: LiveData<Resource<List<Appointment>>> = _appointmentsState

    private val _selectedTab = MutableLiveData(0)
    val selectedTab: LiveData<Int> = _selectedTab

    init {
        loadAppointments()
    }

    fun loadAppointments() {
        viewModelScope.launch {
            repository.getAppointments().collectLatest { resource ->
                _appointmentsState.value = resource
            }
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun getFilteredAppointments(appointments: List<Appointment>, tab: Int): List<Appointment> {
        return when (tab) {
            1 -> appointments.filter { it.status == AppointmentStatus.COMPLETED }
            2 -> appointments.filter { it.status == AppointmentStatus.CANCELLED }
            else -> appointments.filter {
                it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.PENDING
            }
        }
    }
}
