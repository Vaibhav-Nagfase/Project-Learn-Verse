package com.example.learnverse.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.TutorMyProfileResponse
import com.example.learnverse.data.model.TutorProfileUpdateRequest
import com.example.learnverse.data.repository.TutorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Using the same UiState as your other ViewModels
// sealed class UiState { ... }

class MyTutorProfileViewModel(private val repository: TutorRepository) : ViewModel() {

    // A single state to hold the user's profile data
    private val _profile = MutableStateFlow<TutorMyProfileResponse?>(null)
    val profile: StateFlow<TutorMyProfileResponse?> = _profile.asStateFlow()

    // UI state for loading, success, error messages
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Form fields, managed by the ViewModel
    var bio by mutableStateOf("")
    var qualifications by mutableStateOf("")
    var experience by mutableStateOf("")
    var specializations by mutableStateOf("")

    // ✅ To know if we are updating or creating
    private var isEditMode = false

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val profileData = repository.getMyTutorProfile() // This can now be null
                _profile.value = profileData // This will be null if 404

                if (profileData != null) {
                    // ✅ We have a profile, load its data
                    isEditMode = true

                    bio = profileData.bio ?: "" // Use ?: "" as fallback
                    experience = profileData.experience ?: "" // Use ?: "" as fallback

                    // ✅ --- START OF FIX ---
                    // Use null-safe calls and provide a default empty string
                    qualifications = profileData.qualifications
                        ?.joinToString(", ")
                        ?.replace("\"", "") ?: "" // Use ?: "" as fallback

                    specializations = profileData.specializations
                        ?.joinToString(", ")
                        ?.replace("\"", "") ?: "" // Use ?: "" as fallback
                    // ✅ --- END OF FIX ---

                } else {
                    // ✅ We have NO profile, reset fields
                    isEditMode = false
                    bio = ""
                    experience = ""
                    qualifications = ""
                    specializations = ""
                }

                _uiState.value = UiState.Idle // ✅ Always go to Idle after loading
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load profile: ${e.message}")
            }
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {

                // ✅ --- START OF FIX ---
                // The server now accepts clean strings.
                // We just split the text field by comma and trim whitespace.

                val qualificationsList = qualifications.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                // The .map { "\"$it\"" } line has been REMOVED

                val specializationsList = specializations.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                // The .map { "\"$it\"" } line has been REMOVED
                // ✅ --- END OF FIX ---

                val request = TutorProfileUpdateRequest(
                    bio = bio,
                    experience = experience,
                    qualifications = qualificationsList, // Pass the corrected list
                    specializations = specializationsList // Pass the corrected list
                )

                // ✅ The API for creating and updating is the same
                val updatedProfile = repository.updateTutorProfile(request)

                _profile.value = updatedProfile
                isEditMode = true // After saving, we are now in "edit" mode
                _uiState.value = UiState.Success("Profile updated successfully!")

            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to save profile: ${e.message}")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }
}