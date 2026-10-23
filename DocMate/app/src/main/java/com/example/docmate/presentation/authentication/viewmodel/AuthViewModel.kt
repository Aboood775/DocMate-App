package com.example.docmate.presentation.authentication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.docmate.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val shouldOpenMain: Boolean = false,
    val shouldOpenLogin: Boolean = false
)
@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {


    private val _uiState = MutableLiveData(AuthUiState())
    val uiState: LiveData<AuthUiState> = _uiState

    var loginEmail: String = ""
    var loginPassword: String = ""
    var registerName: String = ""
    var registerEmail: String = ""
    var registerPassword: String = ""
    var forgetPasswordEmail: String = ""

    fun isLoggedIn(): Boolean = repository.isLoggedIn()

    fun signInWithEmail(email: String, password: String, fallbackError: String, notVerifiedMessage: String, saveUserError: String) {
        loginEmail = email
        loginPassword = ""
        setLoading(true)

        repository.signInWithEmail(
            email = email,
            password = password,
            onVerified = { openMain() },
            onNotVerified = { showError(notVerifiedMessage) },
            onError = { message -> showError(message ?: fallbackError) }
        )
    }

    fun signInWithGoogle(idToken: String?, googleError: String, saveUserError: String) {
        setLoading(true)
        repository.signInWithGoogle(
            idToken = idToken,
            onSuccess = { openMain() },
            onMissingToken = { showError(googleError) },
            onAuthError = { message -> showError(message ?: googleError) },
            onSaveError = { message -> showError(message ?: saveUserError) }
        )
    }

    fun createAccount(
        fullName: String,
        email: String,
        password: String,
        successMessage: String,
        registerError: String,
        saveUserError: String
    ) {
        registerName = fullName
        registerEmail = email
        registerPassword = ""
        setLoading(true)

        repository.createAccount(
            fullName = fullName,
            email = email,
            password = password,
            onSuccess = {
                _uiState.value = AuthUiState(isLoading = false, successMessage = successMessage, shouldOpenLogin = true)
            },
            onRegisterError = { message -> showError(message ?: registerError) },
            onSaveError = { message -> showError(message ?: saveUserError) }
        )
    }

    fun sendResetLink(email: String, successMessage: String, fallbackError: String) {
        forgetPasswordEmail = email
        setLoading(true)

        repository.sendResetLink(
            email = email,
            onSuccess = {
                _uiState.value = AuthUiState(isLoading = false, successMessage = successMessage)
            },
            onError = { message -> showError(message ?: fallbackError) }
        )
    }

    fun setGoogleLoading() {
        setLoading(true)
    }

    fun clearLoading() {
        _uiState.value = AuthUiState(isLoading = false)
    }

    fun clearMessage() {
        val state = _uiState.value ?: AuthUiState()
        _uiState.value = state.copy(successMessage = null, errorMessage = null)
    }

    fun clearNavigation() {
        val state = _uiState.value ?: AuthUiState()
        _uiState.value = state.copy(shouldOpenMain = false, shouldOpenLogin = false)
    }

    private fun setLoading(isLoading: Boolean) {
        _uiState.value = AuthUiState(isLoading = isLoading)
    }

    private fun openMain() {
        _uiState.value = AuthUiState(isLoading = false, shouldOpenMain = true)
    }

    private fun showError(message: String) {
        _uiState.value = AuthUiState(isLoading = false, errorMessage = message)
    }
}
