package com.example.learnverse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.UserProgressResponse
import com.example.learnverse.data.repository.StudentProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MyCoursesUiState {
    object Loading : MyCoursesUiState()
    data class Success(val courses: List<UserProgressResponse>) : MyCoursesUiState()
    data class Error(val message: String) : MyCoursesUiState()
}

class MyCoursesViewModel(
    private val repository: StudentProgressRepository
) : ViewModel() {

    // ✅ Store original unfiltered courses separately
    private val _allCourses = MutableStateFlow<List<UserProgressResponse>>(emptyList())

    private val _uiState = MutableStateFlow<MyCoursesUiState>(MyCoursesUiState.Loading)
    val uiState: StateFlow<MyCoursesUiState> = _uiState.asStateFlow()

    private val _selectedSortOrder = MutableStateFlow(SortOrder.RECENT)
    val selectedSortOrder: StateFlow<SortOrder> = _selectedSortOrder.asStateFlow()

    private val _selectedFilter = MutableStateFlow(FilterType.ALL)
    val selectedFilter: StateFlow<FilterType> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadCourses()
    }

    fun loadCourses() {
        viewModelScope.launch {
            try {
                _uiState.value = MyCoursesUiState.Loading
                val courses = repository.getMyCourses()

                // ✅ Store original courses
                _allCourses.value = courses

                // ✅ Apply filter/sort to original courses
                _uiState.value = MyCoursesUiState.Success(applySortAndFilter())
            } catch (e: Exception) {
                _uiState.value = MyCoursesUiState.Error(
                    e.message ?: "Failed to load courses"
                )
            }
        }
    }

    fun refresh() {
        loadCourses()
    }

    fun setSortOrder(order: SortOrder) {
        _selectedSortOrder.value = order
        // ✅ Reapply filter/sort from original courses
        _uiState.value = MyCoursesUiState.Success(applySortAndFilter())
    }

    fun setFilter(filter: FilterType) {
        _selectedFilter.value = filter
        // ✅ Reapply filter/sort from original courses
        _uiState.value = MyCoursesUiState.Success(applySortAndFilter())
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        // ✅ Reapply filter/sort from original courses
        _uiState.value = MyCoursesUiState.Success(applySortAndFilter())
    }

    // ✅ FIXED: Always work from _allCourses (original unfiltered data)
    private fun applySortAndFilter(): List<UserProgressResponse> {
        var filtered = _allCourses.value

        // Apply filter
        filtered = when (_selectedFilter.value) {
            FilterType.IN_PROGRESS -> filtered.filter {
                it.completionPercentage in 1.0..99.0
            }
            FilterType.COMPLETED -> filtered.filter {
                it.completionPercentage == 100.0
            }
            FilterType.ALL -> filtered
        }

        // Apply search
        if (_searchQuery.value.isNotEmpty()) {
            filtered = filtered.filter {
                it.activityTitle.contains(_searchQuery.value, ignoreCase = true)
            }
        }

        // Apply sort
        return when (_selectedSortOrder.value) {
            SortOrder.RECENT -> filtered.sortedByDescending { it.lastAccessed }
            SortOrder.PROGRESS -> filtered.sortedByDescending { it.completionPercentage }
            SortOrder.ALPHABETICAL -> filtered.sortedBy { it.activityTitle }
        }
    }

    // ✅ Helper function to get all courses (for counts in UI)
    fun getAllCourses(): List<UserProgressResponse> {
        return _allCourses.value
    }

    enum class SortOrder {
        RECENT, PROGRESS, ALPHABETICAL
    }

    enum class FilterType {
        ALL, IN_PROGRESS, COMPLETED
    }
}

class MyCoursesViewModelFactory(
    private val repository: StudentProgressRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyCoursesViewModel::class.java)) {
            return MyCoursesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}