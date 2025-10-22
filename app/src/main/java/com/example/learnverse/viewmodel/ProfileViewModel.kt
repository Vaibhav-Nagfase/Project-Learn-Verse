package com.example.learnverse.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.UserProfile
import com.example.learnverse.data.model.UserProfileRequest
import com.example.learnverse.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    // A single state to hold the user's profile data
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    // UI state for loading, success, error messages
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Form fields, managed by the ViewModel
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var location by mutableStateOf("")
    var educationLevel by mutableStateOf("UNDERGRADUATE")
    var currentRole by mutableStateOf("")
    var interests by mutableStateOf("")
    var careerGoal by mutableStateOf("")
    var targetSkills by mutableStateOf("")
    var currentFocusArea by mutableStateOf("")
    var communicationStyle by mutableStateOf("FRIENDLY")
    var wantsStepByStepGuidance by mutableStateOf(true)

    // To know if we are updating an existing profile or creating a new one
    private var isEditMode = false

    val educationOptions = listOf("HIGH_SCHOOL", "UNDERGRADUATE", "GRADUATE", "POST_GRADUATE", "OTHER")
    val communicationStyleOptions = listOf("FORMAL", "FRIENDLY", "DIRECT")

    /**
     * Loads the user's existing profile into the form fields.
     */
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val profile = repository.getProfile()
                if (profile != null) {

                    firstName = profile.firstName ?: ""
                    lastName = profile.lastName ?: ""
                    location = profile.location ?: ""
                    educationLevel = profile.currentEducationLevel ?: "UNDERGRADUATE"
                    currentRole = profile.currentRole ?: ""
                    interests = profile.interests?.joinToString(", ") ?: ""
                    careerGoal = profile.careerGoal ?: ""
                    targetSkills = profile.targetSkills?.joinToString(", ") ?: ""
                    currentFocusArea = profile.currentFocusArea ?: ""
                    communicationStyle = profile.communicationStyle ?: "FRIENDLY"
                    wantsStepByStepGuidance = profile.wantsStepByStepGuidance ?: true
                    isEditMode = true


                } else {
                    isEditMode = false // No profile exists, we are in setup mode
                }
                _uiState.value = UiState.Idle
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load profile: ${e.message}")
            }
        }
    }

    /**
     * Saves the profile (either creates a new one or updates an existing one).
     */
    fun saveProfile() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val profileRequest = UserProfileRequest(
                firstName = firstName,
                lastName = lastName,
                location = location,
                currentEducationLevel = educationLevel,
                currentRole = currentRole,
                interests = interests.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                careerGoal = careerGoal,
                targetSkills = targetSkills.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                currentFocusArea = currentFocusArea,
                communicationStyle = communicationStyle,
                wantsStepByStepGuidance = wantsStepByStepGuidance
            )

            try {
                if (isEditMode) {
                    repository.updateProfile(profileRequest)
                    _uiState.value = UiState.Success("Profile updated successfully!")
                } else {
                    repository.setupProfile(profileRequest)
                    _uiState.value = UiState.Success("Profile created successfully!")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to save profile: ${e.message}")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }
}
