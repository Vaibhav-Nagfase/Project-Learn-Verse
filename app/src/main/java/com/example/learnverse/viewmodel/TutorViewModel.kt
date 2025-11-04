package com.example.learnverse.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.Activity
import com.example.learnverse.data.model.CreateActivityRequest
import com.example.learnverse.data.remote.ApiService
import com.example.learnverse.data.repository.TutorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
}

class TutorViewModel(
    private val repository: TutorRepository,
    private val apiService: ApiService,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private val _myActivities = MutableStateFlow<List<Activity>>(emptyList())
    val myActivities: StateFlow<List<Activity>> = _myActivities.asStateFlow()

    // === BASIC INFO ===
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var subject by mutableStateOf("")
    var activityType by mutableStateOf("")
    var mode by mutableStateOf("")
    var difficulty by mutableStateOf("")
    var prerequisites by mutableStateOf("")
    var tags by mutableStateOf("")

    // === LOCATION ===
    var address by mutableStateOf("")
    var city by mutableStateOf("")
    var state by mutableStateOf("")
    var landmark by mutableStateOf("")
    var latitude by mutableStateOf("")
    var longitude by mutableStateOf("")
    var facilities by mutableStateOf("")

    // === PRICING ===
    var price by mutableStateOf("")
    var discountPrice by mutableStateOf("")
    var priceType by mutableStateOf("ONE_TIME")
    var installmentAvailable by mutableStateOf(false)
    var demoAvailable by mutableStateOf(false)
    var freeTrial by mutableStateOf(false)
    var trialDuration by mutableStateOf("")

    // === DURATION & SCHEDULE ===
    var totalSessions by mutableStateOf("")
    var estimatedDuration by mutableStateOf("")
    var durationDescription by mutableStateOf("")
    var selfPaced by mutableStateOf(false)
    var accessDuration by mutableStateOf("")
    var lifetimeAccess by mutableStateOf(false)

    // === AGE GROUP ===
    var minAge by mutableStateOf("")
    var maxAge by mutableStateOf("")
    var ageDescription by mutableStateOf("")

    // === INSTRUCTOR DETAILS ===
    var instructorBio by mutableStateOf("")
    var qualifications by mutableStateOf("")
    var experience by mutableStateOf("")
    var specializations by mutableStateOf("")

    // === CONTACT INFO ===
    var enrollmentLink by mutableStateOf("")
    var whatsappNumber by mutableStateOf("")
    var email by mutableStateOf("")
    var youtubeLink by mutableStateOf("")
    var instagramLink by mutableStateOf("")

    // === VISIBILITY ===
    var isPublic by mutableStateOf(true)
    var featured by mutableStateOf(false)

    // === IMAGE URIS ===
    var selectedBannerImageUri: Uri? by mutableStateOf(null)
    var selectedProfileImageUri: Uri? by mutableStateOf(null)

    // === DROPDOWN OPTIONS ===
    val activityTypeOptions = listOf("COURSE", "WORKSHOP", "ONE_ON_ONE", "GROUP_CLASS", "BOOTCAMP")
    val modeOptions = listOf("ONLINE", "OFFLINE", "HYBRID")
    val difficultyOptions = listOf("BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT")
    val priceTypeOptions = listOf("ONE_TIME", "MONTHLY", "YEARLY", "PER_SESSION")

    // === EDIT MODE ===
    private var currentActivityId: String? = null

    fun resetForm() {
        title = ""
        description = ""
        subject = ""
        activityType = ""
        mode = ""
        difficulty = ""
        prerequisites = ""
        tags = ""

        address = ""
        city = ""
        state = ""
        landmark = ""
        latitude = ""
        longitude = ""
        facilities = ""

        price = ""
        discountPrice = ""
        priceType = "ONE_TIME"
        installmentAvailable = false
        demoAvailable = false
        freeTrial = false
        trialDuration = ""

        totalSessions = ""
        estimatedDuration = ""
        durationDescription = ""
        selfPaced = false
        accessDuration = ""
        lifetimeAccess = false

        minAge = ""
        maxAge = ""
        ageDescription = ""

        instructorBio = ""
        qualifications = ""
        experience = ""
        specializations = ""

        enrollmentLink = ""
        whatsappNumber = ""
        email = ""
        youtubeLink = ""
        instagramLink = ""

        isPublic = true
        featured = false

        selectedBannerImageUri = null
        selectedProfileImageUri = null
        currentActivityId = null
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    fun loadActivityForEdit(activityId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading

                val activity = apiService.getActivityById(activityId).body()
                if (activity != null) {
                    // Load all fields from activity
                    currentActivityId = activity.id
                    title = activity.title
                    description = activity.description
                    subject = activity.subject
                    activityType = activity.activityType ?: ""
                    mode = activity.mode
                    difficulty = activity.difficulty ?: ""
                    prerequisites = activity.prerequisites?.joinToString(", ") ?: ""
                    tags = activity.tags?.joinToString(", ") ?: ""

                    // Location
                    activity.location?.let {
                        address = it.address ?: ""
                        city = it.city ?: ""
                        state = it.state ?: ""
                        landmark = it.landmark ?: ""
                        it.coordinates?.coordinates?.let { coords ->
                            latitude = coords.getOrNull(1)?.toString() ?: ""
                            longitude = coords.getOrNull(0)?.toString() ?: ""
                        }
                        facilities = it.facilities?.joinToString(", ") ?: ""
                    }

                    // Pricing
                    activity.pricing?.let {
                        price = it.price?.toString() ?: ""
                        discountPrice = it.discountPrice?.toString() ?: ""
                        priceType = it.priceType ?: "ONE_TIME"
                        installmentAvailable = it.installmentAvailable ?: false
                    }

                    // Demo
                    demoAvailable = activity.demoAvailable ?: false
                    activity.demoDetails?.let {
                        freeTrial = it.freeTrial ?: false
                        trialDuration = it.trialDuration?.toString() ?: ""
                    }

                    // Duration
                    activity.duration?.let {
                        totalSessions = it.totalSessions?.toString() ?: ""
                        estimatedDuration = it.estimatedDuration?.toString() ?: ""
                        durationDescription = it.durationDescription ?: ""
                        lifetimeAccess = it.lifetimeAccess ?: false
                    }

                    activity.schedule?.let {
                        selfPaced = it.selfPaced ?: false
                        accessDuration = it.accessDuration?.toString() ?: ""
                    }

                    // Age Group
                    activity.suitableAgeGroup?.let {
                        minAge = it.minAge?.toString() ?: ""
                        maxAge = it.maxAge?.toString() ?: ""
                        ageDescription = it.ageDescription ?: ""
                    }

                    // Instructor
                    activity.instructorDetails?.let {
                        instructorBio = it.bio ?: ""
                        qualifications = it.qualifications?.joinToString(", ") ?: ""
                        experience = it.experience ?: ""
                        specializations = it.specializations?.joinToString(", ") ?: ""
                    }

                    // Contact
                    activity.contactInfo?.let {
                        enrollmentLink = it.enrollmentLink ?: ""
                        whatsappNumber = it.whatsappNumber ?: ""
                        email = it.email ?: ""
                        it.socialLinks?.let { social ->
                            youtubeLink = social.youtube ?: ""
                            instagramLink = social.instagram ?: ""
                        }
                    }

                    // Visibility
                    isPublic = activity.isPublic ?: true
                    featured = activity.featured ?: false

                    // Banner image
                    activity.bannerImageUrl?.let {
                        // Load from URL if needed (for display)
                    }

                    _uiState.value = UiState.Success("Activity loaded for editing")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load activity: ${e.message}")
            }
        }
    }

    fun saveActivity(tutorId: String, tutorName: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading

                // Validate required fields
                if (title.isBlank() || description.isBlank() || subject.isBlank() ||
                    activityType.isBlank() || mode.isBlank() || price.isBlank() || tags.isBlank()) {
                    _uiState.value = UiState.Error("Please fill all required fields (*)")
                    return@launch
                }

                // Upload banner image if selected
                var bannerUrl: String? = null
                if (currentActivityId == null && selectedBannerImageUri != null) {
                    bannerUrl = uploadBannerImage(selectedBannerImageUri!!, tutorId)
                }

                // Upload profile image if selected
                var profileImageUrl: String? = null
                if (selectedProfileImageUri != null) {
                    profileImageUrl = uploadProfileImage(selectedProfileImageUri!!, tutorId)
                }

                // Build request
                val request = CreateActivityRequest(
                    tutorId = tutorId,
                    tutorName = tutorName,
                    title = title,
                    description = description,
                    subject = subject,
                    classType = null,
                    activityType = activityType,
                    mode = mode,
                    difficulty = difficulty,
                    pricing = CreateActivityRequest.Pricing(
                        price = price.toIntOrNull() ?: 0,
                        currency = "INR",
                        priceType = priceType,
                        discountPrice = discountPrice.toIntOrNull(),
                        installmentAvailable = installmentAvailable
                    ),
                    suitableAgeGroup = if (minAge.isNotBlank() && maxAge.isNotBlank()) {
                        CreateActivityRequest.SuitableAgeGroup(
                            minAge = minAge.toIntOrNull() ?: 0,
                            maxAge = maxAge.toIntOrNull() ?: 0,
                            ageDescription = ageDescription
                        )
                    } else null,
                    prerequisites = prerequisites.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    duration = CreateActivityRequest.Duration(
                        totalSessions = totalSessions.toIntOrNull() ?: 0,
                        estimatedDuration = estimatedDuration.toIntOrNull() ?: 0,
                        durationDescription = durationDescription,
                        lifetimeAccess = lifetimeAccess
                    ),
                    schedule = if (mode != "ONLINE") {
                        CreateActivityRequest.Schedule(
                            selfPaced = selfPaced,
                            accessDuration = if (selfPaced) accessDuration.toIntOrNull() else null,
                            flexibleScheduling = null
                        )
                    } else null,
                    demoAvailable = demoAvailable,
                    demoDetails = if (demoAvailable) {
                        CreateActivityRequest.DemoDetails(
                            freeTrial = freeTrial,
                            trialDuration = if (freeTrial) trialDuration.toIntOrNull() else null
                        )
                    } else null,
                    tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    isActive = true,
                    isPublic = isPublic,
                    featured = featured
                )

                // Make API call
                val response = if (currentActivityId == null) {
                    apiService.createActivity(request)
                } else {
                    apiService.updateActivity(currentActivityId!!, request)
                }

                if (response.isSuccessful) {
                    val createdActivity = response.body()

                    // Upload banner after activity creation if needed
                    if (currentActivityId == null && selectedBannerImageUri != null && createdActivity != null) {
                        uploadBannerForActivity(createdActivity.id, selectedBannerImageUri!!)
                    }

                    // Upload profile image
                    if (selectedProfileImageUri != null && createdActivity != null) {
                        uploadProfileImageForActivity(createdActivity.id, selectedProfileImageUri!!)
                    }

                    val message = if (currentActivityId == null) {
                        "Activity created successfully!"
                    } else {
                        "Activity updated successfully!"
                    }
                    _uiState.value = UiState.Success(message)
                    resetForm()
                } else {
                    _uiState.value = UiState.Error("Failed to save activity: ${response.message()}")
                }

            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error saving activity: ${e.message}")
            }
        }
    }

    private suspend fun uploadBannerImage(uri: Uri, tutorId: String): String? {
        return try {
            val file = File(context.cacheDir, "banner_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val requestBody = file.asRequestBody("image/jpeg".toMediaType())
            val multipart = MultipartBody.Part.createFormData("banner", file.name, requestBody)

            val response = apiService.uploadBanner("temp-id", multipart)
            if (response.isSuccessful) {
                @Suppress("UNCHECKED_CAST")
                val result = response.body() as? Map<String, Any>
                (result?.get("bannerUrl") as? String)
            } else null
        } catch (e: Exception) {
            android.util.Log.e("TutorViewModel", "Error uploading banner", e)
            null
        }
    }

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

    private suspend fun uploadBannerForActivity(activityId: String, uri: Uri) {
        try {
            val file = File(context.cacheDir, "banner_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val requestBody = file.asRequestBody("image/jpeg".toMediaType())
            val multipart = MultipartBody.Part.createFormData("banner", file.name, requestBody)

            apiService.uploadBanner(activityId, multipart)
        } catch (e: Exception) {
            android.util.Log.e("TutorViewModel", "Error uploading banner", e)
        }
    }

    private suspend fun uploadProfileImage(uri: Uri, tutorId: String): String? {
        return try {
            val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val requestBody = file.asRequestBody("image/jpeg".toMediaType())
            val multipart = MultipartBody.Part.createFormData("profileImage", file.name, requestBody)

            // You may need to add a new endpoint for profile image upload
            // For now, returning the URI as string
            uri.toString()
        } catch (e: Exception) {
            android.util.Log.e("TutorViewModel", "Error uploading profile", e)
            null
        }
    }

    private suspend fun uploadProfileImageForActivity(activityId: String, uri: Uri) {
        try {
            val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val requestBody = file.asRequestBody("image/jpeg".toMediaType())
            val multipart = MultipartBody.Part.createFormData("profileImage", file.name, requestBody)

            // Add endpoint call when backend supports it
        } catch (e: Exception) {
            android.util.Log.e("TutorViewModel", "Error uploading profile", e)
        }
    }
}