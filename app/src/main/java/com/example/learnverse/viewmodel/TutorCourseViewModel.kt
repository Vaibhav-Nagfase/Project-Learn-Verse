package com.example.learnverse.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.TutorCourseDetail
import com.example.learnverse.data.repository.TutorCourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TutorCourseUiState {
    object Loading : TutorCourseUiState()
    data class Success(val data: TutorCourseDetail) : TutorCourseUiState()
    data class Error(val message: String) : TutorCourseUiState()
}

class TutorCourseViewModel(
    private val repository: TutorCourseRepository,
    private val activityId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<TutorCourseUiState>(TutorCourseUiState.Loading)
    val uiState: StateFlow<TutorCourseUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        loadCourseData()
    }

    fun loadCourseData() {
        viewModelScope.launch {
            try {
                _uiState.value = TutorCourseUiState.Loading
                val data = repository.getCourseManagementData(activityId)
                _uiState.value = TutorCourseUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = TutorCourseUiState.Error(
                    e.message ?: "Failed to load course data"
                )
                Log.e("TutorCourseVM", "Error loading course", e)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val data = repository.getCourseManagementData(activityId)
                _uiState.value = TutorCourseUiState.Success(data)
            } catch (e: Exception) {
                showMessage("Failed to refresh: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    // Course Settings
    fun updateCourseStatus(isActive: Boolean?, isPublic: Boolean?) {
        viewModelScope.launch {
            try {
                repository.updateCourseStatus(activityId, isActive, isPublic)
                showMessage("Course settings updated")
                refresh()
            } catch (e: Exception) {
                showMessage("Failed to update settings: ${e.message}")
                Log.e("TutorCourseVM", "Error updating status", e)
            }
        }
    }

    private fun showMessage(message: String) {
        _actionMessage.value = message
    }

    fun clearMessage() {
        _actionMessage.value = null
    }
}

class TutorCourseViewModelFactory(
    private val repository: TutorCourseRepository,
    private val activityId: String
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TutorCourseViewModel::class.java)) {
            return TutorCourseViewModel(repository, activityId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
