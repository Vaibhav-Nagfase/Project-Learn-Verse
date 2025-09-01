package com.example.learnverse.viewmodel

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
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)

    private var isInitialFeedLoaded = false

    private val _isFiltered = mutableStateOf(false)
    val isFiltered: State<Boolean> = _isFiltered

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
    }

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
}