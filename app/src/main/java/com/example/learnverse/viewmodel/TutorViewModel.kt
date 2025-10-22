package com.example.learnverse.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.Activity
import com.example.learnverse.data.model.CreateActivityRequest
import com.example.learnverse.data.repository.TutorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
}

class TutorViewModel(private val repository: TutorRepository) : ViewModel() {

    private val _myActivities = MutableStateFlow<List<Activity>>(emptyList())
    val myActivities: StateFlow<List<Activity>> = _myActivities.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // --- State for the Create Activity Form ---
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var subject by mutableStateOf("")
    var price by mutableStateOf("")
    var totalSessions by mutableStateOf("")
    var classType by mutableStateOf("Group")
    var activityType by mutableStateOf("Course")
    var mode by mutableStateOf("Online")
    var difficulty by mutableStateOf("Beginner")
    var tags by mutableStateOf("")

    // --- NEW: To track if we are editing an existing activity ---
    private var editingActivityId by mutableStateOf<String?>(null)

    // --- Options for the dropdown menus ---
    val classTypeOptions = listOf("Group", "One-on-one")
    val activityTypeOptions = listOf("Course", "Workshop", "Webinar", "Club")
    val modeOptions = listOf("Online", "Offline")
    val difficultyOptions = listOf("Beginner", "Intermediate", "Advanced", "All Levels")

    fun fetchMyActivities() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                _myActivities.value = repository.getMyActivities()
                _uiState.value = UiState.Idle
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to fetch activities: ${e.message}")
            }
        }
    }

    fun saveActivity(tutorId: String, tutorName: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val priceDouble = price.toDoubleOrNull()
                val sessionsInt = totalSessions.toIntOrNull()
                if (title.isBlank() || description.isBlank() || subject.isBlank() || priceDouble == null || sessionsInt == null) {
                    _uiState.value = UiState.Error("Please fill all fields with valid data.")
                    return@launch
                }

                val request = CreateActivityRequest(
                    tutorId = tutorId,
                    tutorName = tutorName,
                    title = title,
                    description = description,
                    subject = subject,
                    classType = classType.lowercase(),
                    activityType = activityType.lowercase(),
                    mode = mode.lowercase(),
                    difficulty = difficulty.lowercase(),
                    tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { subject.split(",").map { it.trim() } },
                    pricing = CreateActivityRequest.Pricing(
                        price = priceDouble,
                        currency = "INR",
                        priceType = "one-time"
                    ),
                    duration = CreateActivityRequest.Duration(
                        totalDuration = sessionsInt * 120, // Assuming 2-hour sessions
                        totalSessions = sessionsInt,
                        durationDescription = "$sessionsInt sessions"
                    ),
                    isActive = true,
                    isPublic = true
                )

                if (editingActivityId == null) {
                    // CREATE new activity
                    repository.createActivity(request)
                    _uiState.value = UiState.Success("Activity created successfully!")
                } else {
                    // UPDATE existing activity
                    repository.updateActivity(editingActivityId!!, request)
                    _uiState.value = UiState.Success("Activity updated successfully!")
                }
                resetForm()

                // THIS LINE HAS BEEN REMOVED TO FIX THE RACE CONDITION
                // fetchMyActivities()

            } catch (e: Exception) {
                _uiState.value = UiState.Error("Creation failed: ${e.message}")
            }
        }
    }

    // --- NEW: Populates the form fields for editing ---
    fun loadActivityForEdit(activityId: String) {
        val activity = _myActivities.value.find { it.id == activityId }
        activity?.let {
            editingActivityId = it.id
            title = it.title
            description = it.description
            subject = it.subject
            classType = it.classType?.replaceFirstChar { char -> char.uppercase() } ?: "Group"
            activityType = it.activityType?.replaceFirstChar { char -> char.uppercase() } ?: "Course"
            mode = it.mode.replaceFirstChar { char -> char.uppercase() }
            difficulty = it.difficulty.replaceFirstChar { char -> char.uppercase() }
            tags = it.tags?.joinToString(", ") ?: ""
            price = it.pricing?.price.toString()
            totalSessions = it.durationInfo?.totalSessions.toString()
        }
    }

    // --- NEW: Deletes an activity and refreshes the list ---
    fun deleteActivity(activityId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                repository.deleteActivity(activityId)
                _uiState.value = UiState.Success("Activity deleted.")
                fetchMyActivities() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Delete failed: ${e.message}")
            }
        }
    }

     fun resetForm() {
        title = ""
        description = ""
        subject = ""
        price = ""
        totalSessions = ""
        classType = "Group"
        activityType = "Course"
        mode = "Online"
        difficulty = "Beginner"
        tags = ""
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }
}