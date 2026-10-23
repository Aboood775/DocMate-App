package com.example.docmate.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docmate.domain.model.Doctor
import com.example.docmate.domain.repository.DoctorsRepository
import com.example.docmate.domain.model.UserProfile
import com.example.docmate.domain.repository.UserRepository
import com.example.docmate.util.Resources
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val doctors: List<Doctor> = emptyList(),
    val userName: String = "",
    val userLocation: String = ""
)

/**
 * ViewModel بدون Hilt حاليًا عشان يبقى أبسط للتجربة الأولى.
 * لو الفريق عايز نستخدم @HiltViewModel و @Inject للـ Repository،
 * التعديل بسيط لاحقًا - مجرد إضافة annotations.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val repository = DoctorsRepository()

    private val _allDoctors = MutableStateFlow<List<Doctor>>(emptyList())
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _searchQuery = MutableStateFlow("")
    private val _specialtyFilter = MutableStateFlow<String?>(null)

    val specialties: StateFlow<List<String>> = MutableStateFlow(emptyList())

    val uiState: StateFlow<HomeUiState> = combine(
        _allDoctors, _isLoading, _searchQuery, _specialtyFilter, _userProfile
    ) { doctors, isLoading, query, specialty, profile ->
        val filtered = doctors.filter { doctor ->
            val matchesQuery = query.isBlank() ||
                doctor.name.contains(query, ignoreCase = true) ||
                doctor.specialty.contains(query, ignoreCase = true)
            val matchesSpecialty = specialty == null || doctor.specialty == specialty
            matchesQuery && matchesSpecialty
        }
        HomeUiState(
            isLoading = isLoading,
            doctors = filtered,
            userName = profile?.fullName ?: "",
            userLocation = profile?.location ?: ""
        )
    }.let { flow ->
        val state = MutableStateFlow(HomeUiState())
        viewModelScope.launch {
            flow.collect { state.value = it }
        }
        state.asStateFlow()
    }

    init {
        loadDoctors()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            when (val result = userRepository.getUserProfile()) {
                is Resources.Success -> _userProfile.value = result.data
                else -> { /* ignore */ }
            }
        }
    }

    fun saveLocation(location: String) {
        viewModelScope.launch {
            val result = userRepository.updateLocation(location)
            if (result is Resources.Success) {
                // Refresh profile to update UI
                loadUserProfile()
            }
        }
    }

    private fun loadDoctors() {
        viewModelScope.launch {
            _isLoading.value = true
            val doctors = repository.getDoctors()
            _allDoctors.value = doctors
            (specialties as MutableStateFlow).value = doctors.map { it.specialty }.distinct()
            _isLoading.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSpecialtyFilterChanged(specialty: String?) {
        _specialtyFilter.value = specialty
    }
}
