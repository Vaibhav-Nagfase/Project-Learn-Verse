package com.example.learnverse.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.Activity
import com.example.learnverse.data.model.ActivityFilter
import com.example.learnverse.data.repository.ActivitiesRepository
import kotlinx.coroutines.launch

// The 'context' dependency has been removed as it's no longer needed.
class FilterViewModel(
    private val repository: ActivitiesRepository
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
            // The logic to get the token has been completely removed.

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
                // The token is no longer passed to the repository method.
                val results = repository.filterActivities(filter)
                onResult(results) // Send the results back to the previous screen
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }
}