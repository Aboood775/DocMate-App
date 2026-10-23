package com.example.docmate.presentation.main.profile


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.docmate.domain.repository.UserRepository
import com.example.docmate.util.Resources
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalInfoViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    val fullName = MutableLiveData("")
    val email = MutableLiveData("")
    val location = MutableLiveData("")
    val isSaving = MutableLiveData(false)

    val initials: LiveData<String> = fullName.map { name ->
        name.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
    }

    private val _updateResult = MutableLiveData<Resources<Unit>>()
    val updateResult: LiveData<Resources<Unit>> = _updateResult

    init {
        loadCurrentProfile()
    }

    fun loadCurrentProfile() {
        viewModelScope.launch {
            when(val result = repository.getUserProfile()) {
                is Resources.Success -> {
                    fullName.value = result.data?.fullName
                    email.value = result.data?.email
                    location.value = result.data?.location
                }
                else -> {}
            }
        }
    }

    fun updateProfile() {
        val name = fullName.value?.trim() ?: ""
        val loc = location.value?.trim() ?: ""
        
        if (name.isEmpty() || loc.isEmpty()) {
            _updateResult.value = Resources.Error("Name and location cannot be empty")
            return
        }

        viewModelScope.launch {
            isSaving.value = true
            _updateResult.value = Resources.Loading()
            val result = repository.updateProfile(name, loc)
            _updateResult.value = result
            isSaving.value = false
        }
    }
}
