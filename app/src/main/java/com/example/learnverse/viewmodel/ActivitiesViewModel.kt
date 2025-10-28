// ActivitiesViewModel.kt
package com.example.learnverse.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.Activity
import com.example.learnverse.data.model.NaturalSearchRequest
import com.example.learnverse.data.repository.ActivitiesRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ActivitiesViewModel(
    private val repository: ActivitiesRepository,
    private val context: Context
) : ViewModel() {

    // Main activities list
    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities.asStateFlow()

    // Enrolled activity IDs
    private val _enrolledActivities = MutableStateFlow<Set<String>>(emptySet())
    val enrolledActivities: StateFlow<Set<String>> = _enrolledActivities.asStateFlow()

    // Full enrolled activities list
    private val _myEnrolledActivities = MutableStateFlow<List<Activity>>(emptyList())
    val myEnrolledActivities: StateFlow<List<Activity>> = _myEnrolledActivities.asStateFlow()

    // Helper for checking enrollment
    val isEnrolled: (String) -> Boolean = { activityId ->
        _enrolledActivities.value.contains(activityId)
    }

    // Search query
    var searchQuery by mutableStateOf("")

    // Loading state
    var isLoading by mutableStateOf(false)
        private set

    // Error message
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Initial load flag
    private var isInitialFeedLoaded = false

    // Filter state
    private val _isFiltered = mutableStateOf(false)
    val isFiltered: State<Boolean> = _isFiltered

    // Nearby activities
    var nearbyActivities by mutableStateOf<List<Activity>>(emptyList())
        private set

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun fetchMyFeed(forceRefresh: Boolean = false) {
        if (isInitialFeedLoaded && !forceRefresh) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val feedActivities = repository.getMyFeed()
                _activities.value = feedActivities // Use _activities instead of activities
                isInitialFeedLoaded = true
                _isFiltered.value = false
            } catch (e: Exception) {
                errorMessage = "Failed to fetch your feed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun performNaturalSearch(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        viewModelScope.launch {
                            try {
                                val searchRequest = NaturalSearchRequest(
                                    text = searchQuery,
                                    userLatitude = location.latitude,
                                    userLongitude = location.longitude
                                )
                                val results = repository.naturalSearch(searchRequest)
                                updateActivitiesList(results)
                            } catch (e: Exception) {
                                errorMessage = "Search failed: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        errorMessage = "Could not get location for search. Please ensure location is enabled."
                        isLoading = false
                    }
                }
                .addOnFailureListener {
                    errorMessage = "Failed to get location: ${it.message}"
                    isLoading = false
                }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchNearbyActivities(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        viewModelScope.launch {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        viewModelScope.launch {
                            try {
                                isLoading = true
                                nearbyActivities = repository.getNearbyActivities(
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )
                            } catch (e: Exception) {
                                errorMessage = "Failed to load nearby activities: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }
        }
    }

    fun fetchMyEnrollments() {
        viewModelScope.launch {
            try {
                val enrolled = repository.getMyEnrolledActivities()
                _myEnrolledActivities.value = enrolled // Now this will work
                _enrolledActivities.value = enrolled.map { it.id }.toSet()
            } catch (e: Exception) {
                errorMessage = "Failed to load your courses: ${e.message}"
            }
        }
    }

    fun updateActivitiesList(newActivities: List<Activity>) {
        _activities.value = newActivities // Use _activities
        isInitialFeedLoaded = true
        _isFiltered.value = true
    }

    fun clearData() {
        _activities.value = emptyList() // Use _activities
        _isFiltered.value = false
        isInitialFeedLoaded = false
    }

    /**
     * Upload banner image for activity
     */
    fun uploadBanner(activityId: String, imageUri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val file = uriToFile(imageUri, context)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("banner", file.name, requestFile)

                val response = repository.uploadBanner(activityId, body)

                if (response.isSuccessful) {
                    val bannerUrl = response.body()?.get("bannerUrl") as? String

                    _activities.value = _activities.value.map { activity ->
                        if (activity.id == activityId) {
                            activity.copy(bannerImageUrl = bannerUrl)
                        } else {
                            activity
                        }
                    }

                    android.widget.Toast.makeText(
                        context,
                        "Banner updated successfully!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }

                file.delete()
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(
                    context,
                    "Failed to upload banner: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Add review to activity
     */
    fun addReview(activityId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            try {
                val reviewData = mapOf(
                    "rating" to rating,
                    "comment" to comment
                )

                val response = repository.addReview(activityId, reviewData)

                if (response.isSuccessful) {
                    fetchActivityDetails(activityId)

                    android.widget.Toast.makeText(
                        context,
                        "Review added successfully!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(
                    context,
                    "Failed to add review: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Fetch single activity details
     */
    private suspend fun fetchActivityDetails(activityId: String) {
        try {
            val response = repository.getActivityById(activityId)
            if (response.isSuccessful) {
                response.body()?.let { updatedActivity ->
                    _activities.value = _activities.value.map { activity ->
                        if (activity.id == activityId) updatedActivity else activity
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Helper: Convert URI to File
     */
    private fun uriToFile(uri: Uri, context: Context): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("banner_", ".jpg", context.cacheDir)

        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }


    /**
     * Get activity by ID from local cache
     */
    fun getActivityById(activityId: String): Activity? {
        return _activities.value.find { it.id == activityId }
    }

    /**
     * ✅ Add activity to cache (for tutor activities)
     */
    fun addActivityToCache(activity: Activity) {
        // Only add if not already in cache
        if (!_activities.value.any { it.id == activity.id }) {
            _activities.value = _activities.value + activity
        }
    }

    /**
     * ✅ Fetch activity by ID from API (if not in cache)
     */
    suspend fun fetchActivityById(activityId: String): Activity? {
        return withContext(Dispatchers.IO) {
            try {
                val response = repository.getActivityById(activityId)
                if (response.isSuccessful && response.body() != null) {
                    val activity = response.body()!!

                    // Add to local cache
                    _activities.value = _activities.value + activity

                    activity
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun enrollInActivity(activityId: String) {
        viewModelScope.launch {
            try {
                val response = repository.enrollInActivity(activityId)

                if (response.isSuccessful) {
                    // ✅ Add to enrolled set
                    _enrolledActivities.value = _enrolledActivities.value + activityId

                    // ✅ Reload enrollments to update UI
                    fetchMyEnrollments()

                    android.widget.Toast.makeText(
                        context,
                        "Enrolled successfully!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    android.widget.Toast.makeText(
                        context,
                        "Enrollment failed: ${response.message()}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(
                    context,
                    "Enrollment failed: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
