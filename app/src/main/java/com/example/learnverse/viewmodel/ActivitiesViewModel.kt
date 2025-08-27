package com.example.learnverse.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.Activity
import com.example.learnverse.data.repository.ActivitiesRepository
import com.example.learnverse.utils.UserPreferences
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ActivitiesViewModel(
    private val repository: ActivitiesRepository,
    private val context: Context // We'll need to remove this later, but for now it's okay
) : ViewModel() {

    // State to hold the list of activities
    var activities by mutableStateOf<List<Activity>>(emptyList())
        private set

    // State for the search text field
    var searchQuery by mutableStateOf("")
        private set

    // State for loading and error messages
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Function to update the search query from the UI
    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
    }

    fun fetchMyFeed() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val token = UserPreferences.getToken(context).firstOrNull()

            if (token.isNullOrEmpty()) {
                errorMessage = "Authentication token not found."
                isLoading = false
                return@launch
            }

            try {
                // This fetches the list of full Activity objects
                activities = repository.getMyFeed(token)
            } catch (e: Exception) {
                errorMessage = "Failed to fetch your feed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // NEW FUNCTION: Finds a specific activity from the list we already fetched.
    fun getActivityById(id: String): Activity? {
        return activities.find { it.id == id }
    }


    // Function to fetch activities based on the current searchQuery
    fun fetchActivities() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            // Use .first() to get the token once, which is safer
            val token = UserPreferences.getToken(context).firstOrNull()

            if (token.isNullOrEmpty()) {
                errorMessage = "Authentication token not found."
                isLoading = false
                return@launch
            }

            try {
                // Pass the token and search query to the repository
                activities = repository.searchActivities(token, searchQuery)
            } catch (e: Exception) {
                errorMessage = "Failed to fetch activities: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}
