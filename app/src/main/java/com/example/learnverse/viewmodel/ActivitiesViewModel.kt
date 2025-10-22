package com.example.learnverse.viewmodel

import android.annotation.SuppressLint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.Activity
import com.example.learnverse.data.model.NaturalSearchRequest
import com.example.learnverse.data.repository.ActivitiesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ActivitiesViewModel(
    private val repository: ActivitiesRepository,
    private val context: Context
) : ViewModel() {

    var activities by mutableStateOf<List<Activity>>(emptyList())
        private set

    private val _myEnrolledActivities = MutableStateFlow<List<Activity>>(emptyList())
    val myEnrolledActivities: StateFlow<List<Activity>> = _myEnrolledActivities

    private var enrolledActivityIds by mutableStateOf<Set<String>>(emptySet())
    val isEnrolled: (String) -> Boolean = { it in enrolledActivityIds }

    var searchQuery by mutableStateOf("")

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)

    private var isInitialFeedLoaded = false

    private val _isFiltered = mutableStateOf(false)
    val isFiltered: State<Boolean> = _isFiltered

    var nearbyActivities by mutableStateOf<List<Activity>>(emptyList())
        private set

    fun fetchMyFeed(forceRefresh: Boolean = false) {
        if (isInitialFeedLoaded && !forceRefresh) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // The token logic has been removed.
                activities = repository.getMyFeed()
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
            // The token logic has been removed.
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
                                // The token is no longer passed to the repository.
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
            // The token logic has been removed.
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        viewModelScope.launch {
                            try {
                                isLoading = true
                                // The token is no longer passed to the repository.
                                nearbyActivities = repository.getNearbyActivities(
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )
                            } catch (e: Exception) {
                                // Handle error
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
                // The token logic has been removed.
                val enrolled = repository.getMyEnrolledActivities()
                _myEnrolledActivities.value = enrolled
                enrolledActivityIds = enrolled.map { it.id }.toSet()
            } catch (e: Exception) {
                errorMessage = "Failed to load your courses: ${e.message}"
            }
        }
    }

    fun enrollInActivity(activityId: String) {
        viewModelScope.launch {
            try {
                // The token logic has been removed.
                repository.enrollInActivity(activityId)

                enrolledActivityIds = enrolledActivityIds + activityId
                val updatedList = activities.map { activity ->
                    if (activity.id == activityId) {
                        activity.copy(
                            enrollmentInfo = activity.enrollmentInfo?.copy(
                                enrolledCount = activity.enrollmentInfo.enrolledCount + 1
                            )
                        )
                    } else {
                        activity
                    }
                }
                activities = updatedList
                fetchMyEnrollments()

            } catch (e: Exception) {
                errorMessage = "Enrollment failed: ${e.message}"
            }
        }
    }

    // Unchanged methods
    fun getActivityById(id: String): Activity? {
        return activities.find { it.id == id }
    }

    fun updateActivitiesList(newActivities: List<Activity>) {
        activities = newActivities
        isInitialFeedLoaded = true
        _isFiltered.value = true
    }

    fun clearData() {
        activities = emptyList()
        _isFiltered.value = false
        isInitialFeedLoaded = false
    }
}