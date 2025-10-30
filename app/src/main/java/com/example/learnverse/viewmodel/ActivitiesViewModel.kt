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
import com.example.learnverse.data.model.HomeFeedResponse
import com.example.learnverse.data.model.NaturalSearchRequest
import com.example.learnverse.data.model.Review
import com.example.learnverse.data.repository.ActivitiesRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    private val _activityReviews = MutableStateFlow<List<Review>>(emptyList())
    val activityReviews: StateFlow<List<Review>> = _activityReviews.asStateFlow()

    private val _totalReviews = MutableStateFlow(0L)
    val totalReviews: StateFlow<Long> = _totalReviews.asStateFlow()

    private val _isLoadingReviews = MutableStateFlow(false)
    val isLoadingReviews: StateFlow<Boolean> = _isLoadingReviews.asStateFlow()

    private val _hasUserReviewed = MutableStateFlow(false)
    val hasUserReviewed: StateFlow<Boolean> = _hasUserReviewed.asStateFlow()

    private val _myReviews = MutableStateFlow<List<Review>>(emptyList())
    val myReviews: StateFlow<List<Review>> = _myReviews.asStateFlow()

    private val _homeFeed = MutableStateFlow<HomeFeedResponse?>(null)
    val homeFeed: StateFlow<HomeFeedResponse?> = _homeFeed.asStateFlow()

    // Loading states for operations
    private val _isUploadingVideo = MutableStateFlow(false)
    val isUploadingVideo: StateFlow<Boolean> = _isUploadingVideo.asStateFlow()

    // Resource upload states
    private val _isUploadingResource = MutableStateFlow(false)
    val isUploadingResource: StateFlow<Boolean> = _isUploadingResource.asStateFlow()

    // Meeting update state
    private val _isUpdatingMeeting = MutableStateFlow(false)
    val isUpdatingMeeting: StateFlow<Boolean> = _isUpdatingMeeting.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress: StateFlow<Int> = _uploadProgress.asStateFlow()

    private val _selectedActivity = MutableStateFlow<Activity?>(null)
    val selectedActivity: StateFlow<Activity?> = _selectedActivity.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()



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
                _enrolledActivities.value = enrolled.map { it.id }.toSet() as Set<String>
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
     * Fetch reviews for an activity
     */
    fun fetchActivityReviews(activityId: String, page: Int = 0) {
        viewModelScope.launch {
            try {
                _isLoadingReviews.value = true

                val response = repository.getActivityReviews(activityId, page, size = 10)

                _activityReviews.value = response.reviews
                _totalReviews.value = response.totalReviews

            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Failed to load reviews: ${e.message}"
            } finally {
                _isLoadingReviews.value = false
            }
        }
    }

    /**
     * Check if user has reviewed activity
     */
    fun checkIfUserReviewed(activityId: String) {
        viewModelScope.launch {
            try {
                val hasReviewed = repository.checkUserReview(activityId)
                _hasUserReviewed.value = hasReviewed
            } catch (e: Exception) {
                e.printStackTrace()
                _hasUserReviewed.value = false
            }
        }
    }

    /**
     * Add review to activity
     */
    fun addReview(activityId: String, rating: Int, feedback: String?) {
        viewModelScope.launch {
            try {
                val response = repository.addReview(activityId, rating, feedback)

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!

                    // Add new review to local list
                    _activityReviews.value = listOf(result.review) + _activityReviews.value
                    _totalReviews.value = _totalReviews.value + 1
                    _hasUserReviewed.value = true

                    android.widget.Toast.makeText(
                        context,
                        result.message,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    android.widget.Toast.makeText(
                        context,
                        "Failed to add review: ${response.message()}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(
                    context,
                    "Error: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Update existing review
     */
    fun updateReview(reviewId: String, rating: Int?, feedback: String?) {
        viewModelScope.launch {
            try {
                val response = repository.updateReview(reviewId, rating, feedback)

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!

                    // Update review in local list
                    _activityReviews.value = _activityReviews.value.map { review ->
                        if (review.id == reviewId) result.review else review
                    }

                    android.widget.Toast.makeText(
                        context,
                        result.message,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    android.widget.Toast.makeText(
                        context,
                        "Failed to update review",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(
                    context,
                    "Error: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Delete review
     */
    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            try {
                val response = repository.deleteReview(reviewId)

                if (response.isSuccessful && response.body() != null) {
                    // Remove from local list
                    _activityReviews.value = _activityReviews.value.filter { it.id != reviewId }
                    _totalReviews.value = _totalReviews.value - 1
                    _hasUserReviewed.value = false

                    android.widget.Toast.makeText(
                        context,
                        response.body()!!.message,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    android.widget.Toast.makeText(
                        context,
                        "Failed to delete review",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(
                    context,
                    "Error: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Fetch user's own reviews
     */
    fun fetchMyReviews() {
        viewModelScope.launch {
            try {
                val response = repository.getMyReviews()
                _myReviews.value = response.reviews
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Failed to load your reviews: ${e.message}"
            }
        }
    }

    /**
     * Clear reviews when leaving activity detail
     */
    fun clearReviews() {
        _activityReviews.value = emptyList()
        _totalReviews.value = 0
        _hasUserReviewed.value = false
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

    /**
     * Fetch complete home feed
     */
    fun fetchHomeFeed() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                val feed = repository.getHomeFeed()
                _homeFeed.value = feed

            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Failed to load home feed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * ========================================
     * VIDEO MANAGEMENT OPERATIONS
     * ========================================
     */

    /**
     * Delete resource from video
     */
    fun deleteResource(
        activityId: String,
        videoId: String,
        resourceUrl: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val response = repository.deleteResource(activityId, videoId, resourceUrl)

                if (response.isSuccessful) {
                    // Refresh activity details
                    fetchActivityById(activityId)
                    onSuccess()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to delete resource"
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Delete failed")
            }
        }
    }


    /**
     * ✅ Fetch activity and update StateFlow
     */
    suspend fun fetchActivityById(activityId: String): Activity? {
        return try {
            _isRefreshing.value = true
            val response = repository.getActivityById(activityId)
            if (response.isSuccessful) {
                val activity = response.body()
                _selectedActivity.value = activity // ✅ Update StateFlow
                activity
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            _isRefreshing.value = false
        }
    }

    /**
     * ✅ Refresh current activity (call after any update)
     */
    fun refreshCurrentActivity() {
        viewModelScope.launch {
            _selectedActivity.value?.id?.let { activityId ->
                fetchActivityById(activityId)
            }
        }
    }

    /**
     * ✅ Add video with URL - WITH REFRESH
     */
    fun addVideoWithUrl(
        activityId: String,
        title: String,
        description: String,
        videoUrl: String,
        order: Int,
        isPreview: Boolean,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isUploadingVideo.value = true

                val response = repository.addVideoWithUrl(
                    activityId, title, description, videoUrl, order, isPreview
                )

                if (response.isSuccessful) {
                    // ✅ Refresh activity
                    fetchActivityById(activityId)
                    onSuccess()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to add video"
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Unknown error occurred")
            } finally {
                _isUploadingVideo.value = false
            }
        }
    }

    /**
     * ✅ Upload video file - WITH REFRESH
     */
    fun uploadVideoFile(
        activityId: String,
        videoUri: Uri,
        title: String,
        description: String,
        order: Int,
        isPreview: Boolean,
        context: Context,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            var tempFile: File? = null
            try {
                _isUploadingVideo.value = true
                _uploadProgress.value = 0

                // Check file size (500MB limit)
                val fileSizeMB = repository.getFileSize(videoUri, context)
                if (fileSizeMB > 500) {
                    onError("Video file size exceeds 500MB limit")
                    _isUploadingVideo.value = false
                    return@launch
                }

                // Convert URI to File
                tempFile = repository.uriToFile(videoUri, context)

                // Upload with real progress tracking
                val response = withContext(Dispatchers.IO) {
                    repository.uploadVideoFile(
                        activityId = activityId,
                        videoFile = tempFile,
                        title = title,
                        description = description,
                        order = order,
                        isPreview = isPreview,
                        onProgress = { progress ->
                            // ✅ Real-time progress update
                            _uploadProgress.value = progress
                        }
                    )
                }

                if (response.isSuccessful) {
                    // Refresh activity
                    fetchActivityById(activityId)
                    _uploadProgress.value = 100
                    onSuccess()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to upload video"
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Upload failed")
            } finally {
                tempFile?.delete()
                _isUploadingVideo.value = false
                delay(1000)
                _uploadProgress.value = 0
            }
        }
    }

    /**
     * ✅ Update video - WITH REFRESH
     */
    fun updateVideo(
        activityId: String,
        videoId: String,
        title: String? = null,
        description: String? = null,
        videoUrl: String? = null,
        order: Int? = null,
        isPreview: Boolean? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val response = repository.updateVideo(
                    activityId, videoId, title, description, videoUrl, order, isPreview
                )

                if (response.isSuccessful) {
                    // ✅ Refresh activity
                    fetchActivityById(activityId)
                    onSuccess()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to update video"
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Update failed")
            }
        }
    }

    /**
     * ✅ Delete video - WITH REFRESH
     */
    fun deleteVideo(
        activityId: String,
        videoId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val response = repository.deleteVideo(activityId, videoId)

                if (response.isSuccessful) {
                    // ✅ Refresh activity
                    fetchActivityById(activityId)
                    onSuccess()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to delete video"
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Delete failed")
            }
        }
    }

    /**
     * ✅ Update meeting details - WITH REFRESH
     */
    fun updateMeetingDetails(
        activityId: String,
        platform: String? = null,
        meetingLink: String? = null,
        meetingId: String? = null,
        passcode: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isUpdatingMeeting.value = true

                val response = repository.updateMeetingDetails(
                    activityId, platform, meetingLink, meetingId, passcode
                )

                if (response.isSuccessful) {
                    // ✅ Refresh activity
                    fetchActivityById(activityId)
                    onSuccess()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to update meeting"
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Update failed")
            } finally {
                _isUpdatingMeeting.value = false
            }
        }
    }

    /**
     * ✅ Delete meeting link - WITH REFRESH
     */
    fun deleteMeetingLink(
        activityId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val response = repository.deleteMeetingLink(activityId)

                if (response.isSuccessful) {
                    // ✅ Refresh activity
                    fetchActivityById(activityId)
                    onSuccess()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to delete meeting"
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Delete failed")
            }
        }
    }

    // ✅ Add same for resource operations
    fun uploadResourceFile(
        activityId: String,
        videoId: String,
        resourceUri: Uri,
        type: String,
        title: String,
        context: Context,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            var tempFile: File? = null
            try {
                _isUploadingResource.value = true
                tempFile = repository.uriToFile(resourceUri, context)

                val response = repository.uploadResourceFile(
                    activityId, videoId, tempFile, type, title
                )

                if (response.isSuccessful) {
                    // ✅ Refresh activity
                    fetchActivityById(activityId)
                    onSuccess()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to upload resource"
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Upload failed")
            } finally {
                tempFile?.delete()
                _isUploadingResource.value = false
            }
        }
    }

    fun addResourceWithUrl(
        activityId: String,
        videoId: String,
        type: String,
        title: String,
        url: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isUploadingResource.value = true

                val response = repository.addResourceWithUrl(
                    activityId, videoId, type, title, url
                )

                if (response.isSuccessful) {
                    // ✅ Refresh activity
                    fetchActivityById(activityId)
                    onSuccess()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to add resource"
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Failed to add resource")
            } finally {
                _isUploadingResource.value = false
            }
        }
    }


}