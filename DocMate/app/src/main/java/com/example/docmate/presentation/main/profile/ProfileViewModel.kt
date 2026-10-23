package com.example.docmate.presentation.main.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docmate.domain.model.UserProfile
import com.example.docmate.domain.repository.UserRepository
import com.example.docmate.util.Resources
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val profile = MutableLiveData<UserProfile>()
    val isLoading = MutableLiveData<Boolean>()
    val isLoggedOut = MutableLiveData(false)

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            isLoading.value = true
            when (val result = repository.getUserProfile()) {
                is Resources.Success -> {
                    profile.value = result.data!!
                    isLoading.value = false
                }
                is Resources.Error -> {
                    isLoading.value = false
                }
                is Resources.Loading -> { /* ignore */ }

            }
        }
    }

    fun logout() {
        auth.signOut()
        isLoggedOut.value = true
    }
}
