package com.example.docmate.presentation.main.confirmbooking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.docmate.domain.model.Doctor
import com.example.docmate.domain.model.Slot
import com.example.docmate.domain.repository.AppointmentsRepository
import com.example.docmate.domain.repository.BookingResult
import com.example.docmate.domain.repository.DoctorsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ConfirmBookingUiState(
    val doctor: Doctor? = null,
    val selectedDate: LocalDate? = null,
    val selectedSlot: Slot? = null,
    val slots: List<Slot> = emptyList(),
    val isLoadingSlots: Boolean = false,
    val isBooking: Boolean = false,
    val bookingSucceeded: Boolean = false,
    val lastBookedAppointmentId: String? = null,
    val errorMessage: String? = null
)

class ConfirmBookingViewModel(private val doctorId: String) : ViewModel() {

    private val doctorsRepository = DoctorsRepository()
    private val appointmentsRepository = AppointmentsRepository()

    private val _uiState = MutableStateFlow(ConfirmBookingUiState())
    val uiState: StateFlow<ConfirmBookingUiState> = _uiState.asStateFlow()

    /** أول 7 أيام بدءًا من النهاردة - حسب تصميم فيجما (قائمة أيام أفقية). */
    val availableDays: List<DayUiModel> = DayUiMapper.map(
        (0..6).map { LocalDate.now().plusDays(it.toLong()) }
    )

    init {
        loadDoctor()
        onDaySelected(LocalDate.now())
    }

    private fun loadDoctor() {
        viewModelScope.launch {
            val doctor = doctorsRepository.getDoctorById(doctorId)
            _uiState.value = _uiState.value.copy(doctor = doctor)
        }
    }

    fun onDaySelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            selectedSlot = null,
            isLoadingSlots = true
        )
        viewModelScope.launch {
            val slots = appointmentsRepository.getAvailableSlots(doctorId, date)
            _uiState.value = _uiState.value.copy(slots = slots, isLoadingSlots = false)
        }
    }

    fun onSlotSelected(slot: Slot) {
        _uiState.value = _uiState.value.copy(selectedSlot = slot)
    }

    /**
     * بيأكد الحجز. أهم نقطتين هنا:
     * 1. isBooking = true فورًا -> يخلي الـ Fragment يظهر الـ overlay
     *    ويعطل زرار "Confirm booking"، فمستحيل اليوزر يبعت طلب حجز
     *    تاني وهو الأول لسه شغال (ده اللي بيمنع تكرار الحجز من ناحية UI).
     * 2. delay بسيط (نص ثانية) قبل النداء الفعلي - حسب طلب الفريق
     *    إن يكون فيه تأخير محسوس بدل ما الانتقال يحصل فجأة.
     */
    fun confirmBooking() {
        val state = _uiState.value
        val date = state.selectedDate ?: return
        val slot = state.selectedSlot ?: return
        if (state.isBooking) return // حماية إضافية من ضغط متكرر سريع

        _uiState.value = state.copy(isBooking = true, errorMessage = null)

        viewModelScope.launch {
            delay(500) // تأخير بسيط مقصود، حسب طلب الفريق، قبل ما الـ request الحقيقي يبدأ

            when (val result = appointmentsRepository.bookAppointment(
                doctorId = doctorId,
                doctorName = state.doctor?.name.orEmpty(),
                doctorSpecialty = state.doctor?.specialty.orEmpty(),
                doctorInitials = state.doctor?.initials().orEmpty(),
                location = state.doctor?.location.orEmpty(),
                date = date,
                time = slot.time
            )) {
                is BookingResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isBooking = false,
                        bookingSucceeded = true,
                        lastBookedAppointmentId = result.appointmentId
                    )
                }
                is BookingResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isBooking = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun onNavigatedAfterSuccess() {
        _uiState.value = _uiState.value.copy(bookingSucceeded = false)
    }

    fun onErrorMessageShown() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class ConfirmBookingViewModelFactory(
    private val doctorId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ConfirmBookingViewModel(doctorId) as T
    }
}
