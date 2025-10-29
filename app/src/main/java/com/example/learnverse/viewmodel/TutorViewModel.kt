// TutorViewModel.kt
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

    private var editingActivityId by mutableStateOf<String?>(null)

    // === BASIC INFO ===
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var subject by mutableStateOf("")
    var activityType by mutableStateOf("RECORDED_COURSE")
    var mode by mutableStateOf("ONLINE")
    var difficulty by mutableStateOf("BEGINNER")

    // === PRICING ===
    var price by mutableStateOf("")
    var discountPrice by mutableStateOf("")
    var priceType by mutableStateOf("ONE_TIME")
    var installmentAvailable by mutableStateOf(false)

    // === DURATION ===
    var totalSessions by mutableStateOf("")
    var estimatedDuration by mutableStateOf("")
    var durationDescription by mutableStateOf("")
    var lifetimeAccess by mutableStateOf(false)

    // === SCHEDULE ===
    var selfPaced by mutableStateOf(true)
    var accessDuration by mutableStateOf("")

    // === AGE GROUP ===
    var minAge by mutableStateOf("")
    var maxAge by mutableStateOf("")
    var ageDescription by mutableStateOf("")

    // === OTHER ===
    var prerequisites by mutableStateOf("")
    var tags by mutableStateOf("")
    var demoAvailable by mutableStateOf(false)
    var freeTrial by mutableStateOf(false)
    var trialDuration by mutableStateOf("")
    var isPublic by mutableStateOf(true)
    var featured by mutableStateOf(false)

    // === DROPDOWN OPTIONS ===
    val activityTypeOptions = listOf("RECORDED_COURSE", "LIVE_CLASS", "WORKSHOP", "WEBINAR")
    val modeOptions = listOf("ONLINE", "OFFLINE", "HYBRID")
    val difficultyOptions = listOf("BEGINNER", "INTERMEDIATE", "ADVANCED", "ALL_LEVELS")
    val priceTypeOptions = listOf("ONE_TIME", "MONTHLY", "YEARLY")

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
                // Validation
                if (title.isBlank() || description.isBlank() || subject.isBlank()) {
                    _uiState.value = UiState.Error("Please fill all required fields.")
                    return@launch
                }

                val priceInt = price.toIntOrNull()
                val sessionsInt = totalSessions.toIntOrNull()
                val durationInt = estimatedDuration.toIntOrNull()

                if (priceInt == null || sessionsInt == null || durationInt == null) {
                    _uiState.value = UiState.Error("Please enter valid numbers for price, sessions, and duration.")
                    return@launch
                }

                // Build request
                val request = CreateActivityRequest(
                    tutorId = tutorId,
                    tutorName = tutorName,
                    title = title,
                    description = description,
                    subject = subject,
                    classType = null, // Not required based on your backend
                    activityType = activityType,
                    mode = mode,
                    difficulty = difficulty,
                    pricing = CreateActivityRequest.Pricing(
                        price = priceInt,
                        currency = "INR",
                        priceType = priceType,
                        discountPrice = discountPrice.toIntOrNull(),
                        installmentAvailable = installmentAvailable
                    ),
                    suitableAgeGroup = if (minAge.isNotBlank() && maxAge.isNotBlank()) {
                        CreateActivityRequest.SuitableAgeGroup(
                            minAge = minAge.toInt(),
                            maxAge = maxAge.toInt(),
                            ageDescription = ageDescription.ifBlank { null }
                        )
                    } else null,
                    prerequisites = if (prerequisites.isNotBlank()) {
                        prerequisites.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    } else null,
                    duration = CreateActivityRequest.Duration(
                        totalSessions = sessionsInt,
                        estimatedDuration = durationInt,
                        durationDescription = durationDescription.ifBlank { "$sessionsInt sessions" },
                        lifetimeAccess = lifetimeAccess
                    ),
                    schedule = CreateActivityRequest.Schedule(
                        selfPaced = selfPaced,
                        accessDuration = accessDuration.toIntOrNull(),
                        flexibleScheduling = true
                    ),
                    demoAvailable = demoAvailable,
                    demoDetails = if (demoAvailable && freeTrial) {
                        CreateActivityRequest.DemoDetails(
                            freeTrial = true,
                            trialDuration = trialDuration.toIntOrNull()
                        )
                    } else null,
                    tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    isActive = true,
                    isPublic = isPublic,
                    featured = featured
                )

                if (editingActivityId == null) {
                    repository.createActivity(request)
                    _uiState.value = UiState.Success("Activity created successfully!")
                } else {
                    repository.updateActivity(editingActivityId!!, request)
                    _uiState.value = UiState.Success("Activity updated successfully!")
                }

                resetForm()
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to save activity: ${e.message}")
            }
        }
    }

    fun loadActivityForEdit(activityId: String) {
        val activity = _myActivities.value.find { it.id == activityId } ?: return

        editingActivityId = activity.id
        title = activity.title
        description = activity.description
        subject = activity.subject
        activityType = activity.activityType ?: "RECORDED_COURSE"
        mode = activity.mode
        difficulty = activity.difficulty ?: "BEGINNER"

        activity.pricing?.let {
            price = it.price.toString()
            discountPrice = it.discountPrice?.toString() ?: ""
            priceType = it.priceType ?: "ONE_TIME"
            installmentAvailable = it.installmentAvailable ?: false
        }

        activity.duration?.let {
            totalSessions = it.totalSessions.toString()
            estimatedDuration = it.estimatedDuration?.toString() ?: ""
            durationDescription = it.durationDescription ?: ""
            lifetimeAccess = it.lifetimeAccess ?: false
        }

        activity.schedule?.let {
            selfPaced = it.selfPaced ?: true
            accessDuration = it.accessDuration?.toString() ?: ""
        }

        activity.suitableAgeGroup?.let {
            minAge = it.minAge?.toString() ?: ""
            maxAge = it.maxAge?.toString() ?: ""
            ageDescription = it.ageDescription ?: ""
        }

        prerequisites = activity.prerequisites?.joinToString(", ") ?: ""
        tags = activity.tags?.joinToString(", ") ?: ""

        demoAvailable = activity.demoAvailable ?: false
        activity.demoDetails?.let {
            freeTrial = it.freeTrial ?: false
            trialDuration = it.trialDuration?.toString() ?: ""
        }

        isPublic = activity.isPublic ?: true
        featured = activity.featured ?: false
    }

    fun deleteActivity(activityId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                repository.deleteActivity(activityId)
                _uiState.value = UiState.Success("Activity deleted.")
                fetchMyActivities()
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Delete failed: ${e.message}")
            }
        }
    }

    fun resetForm() {
        editingActivityId = null
        title = ""
        description = ""
        subject = ""
        activityType = "RECORDED_COURSE"
        mode = "ONLINE"
        difficulty = "BEGINNER"
        price = ""
        discountPrice = ""
        priceType = "ONE_TIME"
        installmentAvailable = false
        totalSessions = ""
        estimatedDuration = ""
        durationDescription = ""
        lifetimeAccess = false
        selfPaced = true
        accessDuration = ""
        minAge = ""
        maxAge = ""
        ageDescription = ""
        prerequisites = ""
        tags = ""
        demoAvailable = false
        freeTrial = false
        trialDuration = ""
        isPublic = true
        featured = false
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }
}