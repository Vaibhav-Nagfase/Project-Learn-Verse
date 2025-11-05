package com.example.learnverse.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.StudentCourseViewResponse
import com.example.learnverse.data.repository.StudentCourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class StudentCourseUiState {
    object Loading : StudentCourseUiState()
    data class Success(val course: StudentCourseViewResponse) : StudentCourseUiState()
    data class Error(val message: String) : StudentCourseUiState()
}

class StudentCourseViewModel(
    private val repository: StudentCourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentCourseUiState>(StudentCourseUiState.Loading)
    val uiState: StateFlow<StudentCourseUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // ✅ ADD THESE HELPER PROPERTIES
    val courseData: StateFlow<StudentCourseViewResponse?> = MutableStateFlow(null)
    private val _courseDataMutable = courseData as MutableStateFlow<StudentCourseViewResponse?>

    val isLoading: StateFlow<Boolean> = MutableStateFlow(false)
    private val _isLoadingMutable = isLoading as MutableStateFlow<Boolean>

    val errorMessage: StateFlow<String?> = MutableStateFlow(null)
    private val _errorMessageMutable = errorMessage as MutableStateFlow<String?>

    fun fetchCourseDetails(activityId: String) {
        viewModelScope.launch {
            try {
                _isLoadingMutable.value = true
                _errorMessageMutable.value = null
                _uiState.value = StudentCourseUiState.Loading

                val course = repository.getCourseView(activityId)

                _courseDataMutable.value = course
                _uiState.value = StudentCourseUiState.Success(course)
                _isLoadingMutable.value = false
            } catch (e: Exception) {
                val error = e.message ?: "Failed to load course"
                _errorMessageMutable.value = error
                _uiState.value = StudentCourseUiState.Error(error)
                _isLoadingMutable.value = false
                Log.e("StudentCourseVM", "Error loading course", e)
            }
        }
    }

    fun loadCourse(activityId: String) {
        fetchCourseDetails(activityId)
    }

    fun refresh(activityId: String) {
        fetchCourseDetails(activityId)
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }


    fun markResourceDownloaded(activityId: String, resourceId: String) {
        viewModelScope.launch {
            try {
                repository.markResourceDownloaded(activityId, resourceId)
                Log.d("StudentCourse", "Resource marked as downloaded: $resourceId")

                // Refresh course to update UI
                fetchCourseDetails(activityId)
            } catch (e: Exception) {
                Log.e("StudentCourse", "Failed to mark resource downloaded", e)
            }
        }
    }
}

class StudentCourseViewModelFactory(
    private val repository: StudentCourseRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentCourseViewModel::class.java)) {
            return StudentCourseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}