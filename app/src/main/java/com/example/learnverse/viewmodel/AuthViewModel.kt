package com.example.learnverse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.LoginRequest
import com.example.learnverse.data.model.RegisterRequest
import com.example.learnverse.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

// State for app-level navigation and flow control.
sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data object NeedsInterestSelection : AuthState()
    data object Authenticated : AuthState()
}

// State for screen-level UI feedback (loading indicators, error messages).
sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    // StateFlow for the overall authentication journey.
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // StateFlow for UI feedback on the Login/Register screens.
    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private var userToken: String? = null

    var interestSelectionCancelled = false
        private set

    init {
        checkCurrentUser()
    }


    private fun checkCurrentUser() {
        viewModelScope.launch {
            userToken = repository.getToken().firstOrNull()

            // To read the saved skipped flag
            interestSelectionCancelled = repository.getInterestsSkippedFlag().firstOrNull() ?: false

            if (userToken == null) {
                _authState.value = AuthState.Unauthenticated
            } else {
                _authState.value = AuthState.Authenticated
            }
        }
    }

    fun login(email: String, password: String) {

        interestSelectionCancelled = false

        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading
            try {
                val response = repository.login(LoginRequest(email, password))
                userToken = response.accessToken
                _loginUiState.value = LoginUiState.Idle

                if (response.interests.isNullOrEmpty()) {
                    _authState.value = AuthState.NeedsInterestSelection
                } else {
                    _authState.value = AuthState.Authenticated
                }
            } catch (e: Exception) {
                _loginUiState.value = LoginUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading
            try {
                val response = repository.registerUser(RegisterRequest(name, email, password))
                userToken = response.accessToken
                _loginUiState.value = LoginUiState.Idle
                _authState.value = AuthState.NeedsInterestSelection
            } catch (e: Exception) {
                _loginUiState.value = LoginUiState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun saveInterests(interests: List<String>) {
        viewModelScope.launch {
            interestSelectionCancelled = false
            repository.saveInterestsSkippedFlag(false)

            try {
                userToken?.let { token ->
                    repository.addUserInterests(token, interests)
                    _authState.value = AuthState.Authenticated
                }
            } catch (e: Exception) {
                // Optionally, update a UI state to show an error
            }
        }
    }

    fun onInterestSelectionCancelled() {
        viewModelScope.launch {
            interestSelectionCancelled = true
            // Save the decision to persistent storage
            repository.saveInterestsSkippedFlag(true)
            _authState.value = AuthState.Authenticated
        }
    }

    fun logout() {

        viewModelScope.launch {
            interestSelectionCancelled = false
            repository.saveInterestsSkippedFlag(false)

            repository.logout()
            userToken = null
            _authState.value = AuthState.Unauthenticated
        }
    }
}