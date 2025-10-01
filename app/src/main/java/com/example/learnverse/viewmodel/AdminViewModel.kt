package com.example.learnverse.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.PendingVerification
import com.example.learnverse.data.repository.AdminRepository
import com.example.learnverse.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AdminViewModel(
    application: Application,
    private val adminRepository: AdminRepository,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val _pendingVerifications = MutableStateFlow<List<PendingVerification>>(emptyList())
    val pendingVerifications: StateFlow<List<PendingVerification>> = _pendingVerifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()


    fun fetchPendingVerifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val token = authRepository.getToken().firstOrNull()
            if (token.isNullOrBlank()) {
                _errorMessage.value = "Admin token not found."
                _isLoading.value = false
                return@launch
            }
            try {
                _pendingVerifications.value = adminRepository.getPendingVerifications(token)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveRequest(verificationId: String) {
        viewModelScope.launch {
            val token = authRepository.getToken().firstOrNull() ?: return@launch
            try {
                adminRepository.approveVerification(token, verificationId)
                fetchPendingVerifications()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to approve: ${e.message}"
            }
        }
    }

    fun rejectRequest(verificationId: String, reason: String) {
        viewModelScope.launch {
            val token = authRepository.getToken().firstOrNull() ?: return@launch
            try {
                adminRepository.rejectVerification(token, verificationId, reason)
                fetchPendingVerifications()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reject: ${e.message}"
            }
        }
    }
}