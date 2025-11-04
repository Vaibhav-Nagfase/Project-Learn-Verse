package com.example.learnverse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.TutorDashboardStats
import com.example.learnverse.data.repository.TutorDashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val stats: TutorDashboardStats) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class TutorDashboardViewModel(
    private val repository: TutorDashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardStats()
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            try {
                _uiState.value = DashboardUiState.Loading
                val stats = repository.getDashboardStats()
                _uiState.value = DashboardUiState.Success(stats)
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(
                    e.message ?: "Failed to load dashboard"
                )
            }
        }
    }

    fun refresh() {
        loadDashboardStats()
    }
}