package com.example.learnverse.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.LoginRequest
import com.example.learnverse.data.model.RegisterRequest
import com.example.learnverse.data.repository.AuthRepository
import com.example.learnverse.data.repository.TutorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.auth0.android.jwt.JWT
import com.example.learnverse.data.model.TutorVerificationStatus

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
    private val repository: AuthRepository,
    private val tutorRepository: TutorRepository
) : ViewModel() {

    // StateFlow for the overall authentication journey.
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // StateFlow for UI feedback on the Login/Register screens.
    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    // --- NEW STATE for managing the user's interests list ---
    private val _userInterests = MutableStateFlow<List<String>>(emptyList())
    val userInterests: StateFlow<List<String>> = _userInterests.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    // ADD THIS NEW STATE to store the user's role
    private val _currentUserRole = MutableStateFlow<String?>(null)
    val currentUserRole: StateFlow<String?> = _currentUserRole.asStateFlow()

    // NEW STATE: To hold the user's tutor application status
    private val _verificationStatus = MutableStateFlow<TutorVerificationStatus?>(null)
    val verificationStatus: StateFlow<TutorVerificationStatus?> = _verificationStatus.asStateFlow()


    private var userToken: String? = null

    var interestSelectionCancelled by mutableStateOf(false)
        private set

    var navigateToFeedAfterOnboarding by mutableStateOf(false)
        private set

    init {
        checkCurrentUser()
    }


    private fun checkCurrentUser() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            userToken = repository.getToken().firstOrNull()

            interestSelectionCancelled = repository.getInterestsSkippedFlag().firstOrNull() ?: false
            navigateToFeedAfterOnboarding = false

            if (userToken == null) {
                _authState.value = AuthState.Unauthenticated
                return@launch
            }

            // --- All logic for an existing token is now in one try-catch ---
            try {
                val decodedJwt = JWT(userToken!!)
                val userEmail = decodedJwt.getClaim("email").asString()
                val userRole = decodedJwt.getClaim("role").asString()
                _currentUserEmail.value = userEmail
                _currentUserRole.value = userRole

                // If the user is an ADMIN, they are authenticated. Stop here.
                if (userRole == "ADMIN") {
                    _authState.value = AuthState.Authenticated
                    return@launch
                }

                // If the user is a USER, try to get their verification status.
                if (userEmail != null && userRole != "TUTOR") {
                    try {
                        // This will now only update the status if the call is successful.
                        _verificationStatus.value = tutorRepository.getTutorVerificationStatus(userToken!!, userEmail)
                    } catch (e: Exception) {
                        // If it fails (e.g., a 404), we print the error for debugging
                        // but we DO NOT log the user out. The process continues.
                        println("Could not get tutor status (user may not have applied yet): ${e.message}")
                        _verificationStatus.value = null // Ensure status is reset
                    }
                }

                // Finally, check for interests to decide the next screen.
                val interestsResponse = repository.getUserInterests(userToken!!)
                _userInterests.value = interestsResponse.interests
                if (interestsResponse.interestCount == 0) {
                    _authState.value = AuthState.NeedsInterestSelection
                } else {
                    _authState.value = AuthState.Authenticated
                }
            } catch (e: Exception) {
                // This will now only catch critical errors (like an invalid token)
                // and log the user out.
                logout()
            }
        }
    }


    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading
            try {
                // Step 1: Just log in. The repository saves the token.
                repository.login(LoginRequest(email, password))
                _loginUiState.value = LoginUiState.Idle

                // Step 2: Call checkCurrentUser() to handle ALL the complex logic in one place.
                checkCurrentUser()

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
                    navigateToFeedAfterOnboarding = true
                    _authState.value = AuthState.Authenticated
                }
            } catch (e: Exception) {
                // Optionally, update a UI state to show an error
            }
        }
    }

    fun onNavigationToFeedComplete() {
        navigateToFeedAfterOnboarding = false
    }

    fun onInterestSelectionCancelled() {
        viewModelScope.launch {
            interestSelectionCancelled = true
            // Save the decision to persistent storage
            repository.saveInterestsSkippedFlag(true)
            _authState.value = AuthState.Authenticated
        }
    }

    // --- NEW function to be called from the "My Interests" screen ---
    fun fetchUserInterests() {
        viewModelScope.launch {
            userToken?.let { token ->
                try {
                    val interestsResponse = repository.getUserInterests(token)
                    _userInterests.value = interestsResponse.interests
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    // --- NEW function to add interests from the "My Interests" screen ---
    fun addInterests(interestsToAdd: List<String>) {
        viewModelScope.launch {
            userToken?.let { token ->
                try {
                    repository.addUserInterests(token, interestsToAdd)
                    fetchUserInterests() // Refresh the list after adding
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    // --- NEW function to remove interests from the "My Interests" screen ---
    fun removeInterests(interestsToRemove: List<String>) {
        viewModelScope.launch {
            userToken?.let { token ->
                try {
                    repository.removeUserInterests(token, interestsToRemove)
                    fetchUserInterests() // Refresh the list after removing
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    fun logout() {

        viewModelScope.launch {
            interestSelectionCancelled = false
            repository.saveInterestsSkippedFlag(false)
            _currentUserRole.value = null // Clear the role on logout

            repository.logout()
            userToken = null
            _authState.value = AuthState.Unauthenticated
        }
    }
}