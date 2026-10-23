package com.example.docmate.presentation.main.doctordetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.docmate.domain.model.Doctor
import com.example.docmate.domain.repository.DoctorsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DoctorDetailUiState(
    val isLoading: Boolean = true,
    val doctor: Doctor? = null
)

class DoctorDetailViewModel(private val doctorId: String) : ViewModel() {

    private val repository = DoctorsRepository()

    private val _uiState = MutableStateFlow(DoctorDetailUiState())
    val uiState: StateFlow<DoctorDetailUiState> = _uiState.asStateFlow()

    init {
        loadDoctor()
    }

    private fun loadDoctor() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val doctor = repository.getDoctorById(doctorId)
            _uiState.value = DoctorDetailUiState(isLoading = false, doctor = doctor)
        }
    }
}

class DoctorDetailViewModelFactory(
    private val doctorId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DoctorDetailViewModel(doctorId) as T
    }
}
