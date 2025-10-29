package com.example.learnverse.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.profile.UserProfile
import com.example.learnverse.data.model.profile.UserProfileRequest
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
    var age by mutableStateOf("")
    var gender by mutableStateOf("")
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

    // ProfileViewModel.kt - Update loadProfile method
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val profile = repository.getProfile()

                _userProfile.value = profile

                if (profile != null) {
                    // Load form fields
                    firstName = profile.firstName ?: ""
                    lastName = profile.lastName ?: ""
                    location = profile.location ?: ""
                    age = profile.age?.toString() ?: ""
                    gender = profile.gender ?: ""
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
                    isEditMode = false
                }
                _uiState.value = UiState.Idle
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load profile: ${e.message}")
            }
        }
    }

    // ProfileViewModel.kt - Update saveProfile method
    fun saveProfile() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val profileRequest = UserProfileRequest(
                firstName = firstName,
                lastName = lastName,
                location = location,
                age = age.toIntOrNull(),
                gender = gender,
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
                    val updatedProfile = repository.updateProfile(profileRequest)
                    _userProfile.value = updatedProfile // ✅ Update state
                    _uiState.value = UiState.Success("Profile updated successfully!")
                } else {
                    val newProfile = repository.setupProfile(profileRequest)
                    _userProfile.value = newProfile // ✅ Update state
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
