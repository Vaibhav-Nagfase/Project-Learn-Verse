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
import com.example.learnverse.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.auth0.android.jwt.JWT
import com.example.learnverse.data.model.TutorVerificationStatus
import com.example.learnverse.data.repository.ProfileRepository

// State for app-level navigation and flow control. (Unchanged)
sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data object NeedsInterestSelection : AuthState()
    data object Authenticated : AuthState()
}

// State for screen-level UI feedback (loading indicators, error messages). (Unchanged)
sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class AuthViewModel(
    private val repository: AuthRepository,
    private val tutorRepository: TutorRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    // All StateFlow declarations remain the same.
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    // --- NEW: State to track if the user has a profile ---
    private val _hasProfile = MutableStateFlow<Boolean?>(null) // null = unknown, true = yes, false = no
    val hasProfile: StateFlow<Boolean?> = _hasProfile.asStateFlow()

    private val _userInterests = MutableStateFlow<List<String>>(emptyList())
    val userInterests: StateFlow<List<String>> = _userInterests.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _currentUserRole = MutableStateFlow<String?>(null)
    val currentUserRole: StateFlow<String?> = _currentUserRole.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _currentUserName = MutableStateFlow<String?>(null)
    val currentUserName: StateFlow<String?> = _currentUserName.asStateFlow()

    private val _verificationStatus = MutableStateFlow<TutorVerificationStatus?>(null)
    val verificationStatus: StateFlow<TutorVerificationStatus?> = _verificationStatus.asStateFlow()

    var interestSelectionCancelled by mutableStateOf(false)
        private set

    var navigateToFeedAfterOnboarding by mutableStateOf(false)
        private set


    init {
        checkCurrentUser()

        // Uncommented and finalized: Listens for global logout events.
        viewModelScope.launch {
            SessionManager.logoutEvent.collect {
                logout()
            }
        }
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            // This is the one place we need to get the token, just to see if it exists.
            val token = repository.getToken().firstOrNull()

            if (token.isNullOrBlank()) {
                _authState.value = AuthState.Unauthenticated
                return@launch
            }

            try {
                val decodedJwt = JWT(token)
                val userEmail = decodedJwt.getClaim("email").asString()
                val userRole = decodedJwt.getClaim("role").asString()

                _currentUserId.value = decodedJwt.subject // The 'sub' claim is usually the ID
                _currentUserName.value = decodedJwt.getClaim("name").asString()

                _currentUserEmail.value = userEmail
                _currentUserRole.value = userRole

                // --- NEW: Check for user profile after verifying token ---
                val userProfile = profileRepository.getProfile()
                _hasProfile.value = userProfile != null

                if (userRole == "ADMIN") {
                    _authState.value = AuthState.Authenticated
                    return@launch
                }

                if (userEmail != null) {
                    try {
                        // The token is no longer passed to the repository.
                        _verificationStatus.value = tutorRepository.getTutorVerificationStatus(userEmail)
                    } catch (e: Exception) {
                        println("Could not get tutor status (user may not have applied yet): ${e.message}")
                        _verificationStatus.value = null
                    }
                }

                // The token is no longer passed to the repository.
                val interestsResponse = repository.getUserInterests()
                _userInterests.value = interestsResponse.interests
                if (interestsResponse.interestCount == 0) {
                    _authState.value = AuthState.NeedsInterestSelection
                } else {
                    _authState.value = AuthState.Authenticated
                }
            } catch (e: Exception) {
                logout()
            }
        }
    }

    fun saveInterests(interests: List<String>) {
        viewModelScope.launch {
            interestSelectionCancelled = false
            repository.saveInterestsSkippedFlag(false)
            try {
                // The token is no longer passed to the repository.
                repository.addUserInterests(interests)
                navigateToFeedAfterOnboarding = false
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun fetchUserInterests() {
        viewModelScope.launch {
            try {
                // The token is no longer passed to the repository.
                val interestsResponse = repository.getUserInterests()
                _userInterests.value = interestsResponse.interests
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addInterests(interestsToAdd: List<String>) {
        viewModelScope.launch {
            try {
                // The token is no longer passed to the repository.
                repository.addUserInterests(interestsToAdd)
                fetchUserInterests() // Refresh the list after adding
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun removeInterests(interestsToRemove: List<String>) {
        viewModelScope.launch {
            try {
                // The token is no longer passed to the repository.
                repository.removeUserInterests(interestsToRemove)
                fetchUserInterests() // Refresh the list after removing
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun refreshTutorStatus() {
        viewModelScope.launch {
            val email = _currentUserEmail.value ?: return@launch
            try {
                // The token is no longer passed to the repository.
                _verificationStatus.value = tutorRepository.getTutorVerificationStatus(email)
            } catch (e: Exception) {
                println("Could not refresh tutor status: ${e.message}")
            }
        }
    }

    /**
     * Called from the UI after a user successfully creates their profile.
     * This updates the state without needing another network call.
     */
    fun onProfileSetupComplete() {
        _hasProfile.value = true
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Unauthenticated
            interestSelectionCancelled = false
            repository.saveInterestsSkippedFlag(false)

            _currentUserRole.value = null
            _verificationStatus.value = null

            // --- CLEAR THE NEW STATES ON LOGOUT ---
            _currentUserId.value = null
            _currentUserName.value = null

            _hasProfile.value = null

            repository.logout()
        }
    }

    // Login and Register methods remain largely the same, as they don't use a pre-existing token
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading
            try {
                repository.login(LoginRequest(email, password))
                _loginUiState.value = LoginUiState.Idle
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
                repository.registerUser(RegisterRequest(name, email, password))
                _loginUiState.value = LoginUiState.Idle
                _authState.value = AuthState.NeedsInterestSelection
                _hasProfile.value = false
            } catch (e: Exception) {
                _loginUiState.value = LoginUiState.Error(e.message ?: "Registration failed")
            }
        }
    }

    // Unchanged methods
    fun onNavigationToFeedComplete() {
        navigateToFeedAfterOnboarding = false
    }

    fun onInterestSelectionCancelled() {
        viewModelScope.launch {
            interestSelectionCancelled = true
            repository.saveInterestsSkippedFlag(true)
            _authState.value = AuthState.Authenticated
        }
    }
}