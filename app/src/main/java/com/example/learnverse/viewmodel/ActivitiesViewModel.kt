package com.example.learnverse.viewmodel

import android.annotation.SuppressLint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.content.Context
import androidx.compose.runtime.State
// --- CRITICAL COMPOSE IMPORTS ---
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
// --- ANDROIDX IMPORTS ---
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// --- YOUR PROJECT IMPORTS ---
import com.example.learnverse.data.model.Activity
import com.example.learnverse.data.model.ActivityFilter
import com.example.learnverse.data.model.NaturalSearchRequest
import com.example.learnverse.data.repository.ActivitiesRepository
import com.example.learnverse.utils.UserPreferences
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ActivitiesViewModel(
    private val repository: ActivitiesRepository,
    private val context: Context
) : ViewModel() {

    var activities by mutableStateOf<List<Activity>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)

    private var isInitialFeedLoaded = false

    private val _isFiltered = mutableStateOf(false)
    val isFiltered: State<Boolean> = _isFiltered

    // STATE to hold the nearby activities
    var nearbyActivities by mutableStateOf<List<Activity>>(emptyList())
        private set


    fun fetchMyFeed(forceRefresh: Boolean = false) {
        if (isInitialFeedLoaded && !forceRefresh) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val token = UserPreferences.getToken(context).firstOrNull()

            if (token.isNullOrBlank()) {
                errorMessage = "Authentication token not found."
                isLoading = false
                return@launch
            }

            try {
                activities = repository.getMyFeed(token)
                isInitialFeedLoaded = true
                _isFiltered.value = false
            } catch (e: Exception) {
                errorMessage = "Failed to fetch your feed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

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
        // You can reset any other states here if needed
    }

    // function for the natural language search
    @SuppressLint("MissingPermission")
    fun performNaturalSearch(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val token = UserPreferences.getToken(context).firstOrNull()
            if (token.isNullOrBlank()) {
                errorMessage = "Authentication token not found"
                isLoading = false
                return@launch
            }

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
                                val results = repository.naturalSearch(token, searchRequest)
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

    @SuppressLint("MissingPermission") // We'll handle permissions in the UI
    fun fetchNearbyActivities(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        viewModelScope.launch {
            // First, get the token
            val token = UserPreferences.getToken(context).firstOrNull()
            if (token.isNullOrBlank()) { /* Handle error */ return@launch }

            // Then, get the location
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        // Location found, now call the repository
                        viewModelScope.launch {
                            try {
                                isLoading = true // You might want a separate loading state
                                nearbyActivities = repository.getNearbyActivities(
                                    token = token,
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
}