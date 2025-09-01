package com.example.learnverse.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.Activity
import com.example.learnverse.data.model.ActivityFilter
import com.example.learnverse.data.repository.ActivitiesRepository
import com.example.learnverse.utils.UserPreferences
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class FilterViewModel(
    private val repository: ActivitiesRepository,
    private val context: Context // For getting the token
) : ViewModel() {

    // --- State for each filter option on the screen ---
    val selectedModes = mutableStateListOf<String>()
    val selectedDifficulties = mutableStateListOf<String>()
    val selectedSubjects = mutableStateListOf<String>()

    // For a price RangeSlider, from 0 to a max of 50000
    val priceRange = mutableStateOf(0f..50000f)

    // For checkbox/switch filters
    var demoAvailable = mutableStateOf(false)

    // For sorting options
    var sortBy = mutableStateOf<String?>(null) // e.g., "price", "rating"

    // State for loading indicator
    var isLoading = mutableStateOf(false)

    /**
     * Resets all selected filters to their default state.
     */
    fun clearFilters() {
        selectedModes.clear()
        selectedDifficulties.clear()
        selectedSubjects.clear()
        priceRange.value = 0f..50000f
        demoAvailable.value = false
        sortBy.value = null
    }


    fun applyFilters(onResult: (List<Activity>) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            val token = UserPreferences.getToken(context).firstOrNull()
            if (token.isNullOrBlank()) {
                // Handle not being logged in
                isLoading.value = false
                return@launch
            }

            // Build the filter object from the current state of the ViewModel
            val filter = ActivityFilter(
                modes = selectedModes.toList().ifEmpty { null },
                difficulties = selectedDifficulties.toList().ifEmpty { null },
                subjects = selectedSubjects.toList().ifEmpty { null },
                minPrice = priceRange.value.start.toInt(),
                maxPrice = priceRange.value.endInclusive.toInt(),
                demoAvailable = demoAvailable.value,
                sortBy = sortBy.value
            )

            try {
                val results = repository.filterActivities(token, filter)
                onResult(results) // Send the results back to the previous screen
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }
}