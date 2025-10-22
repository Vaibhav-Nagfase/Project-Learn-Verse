package com.example.learnverse.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.PendingVerification
import com.example.learnverse.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap

// The AuthRepository dependency is no longer needed and has been removed.
class AdminViewModel(
    application: Application,
    private val adminRepository: AdminRepository
) : AndroidViewModel(application) {

    private val _pendingVerifications = MutableStateFlow<List<PendingVerification>>(emptyList())
    val pendingVerifications: StateFlow<List<PendingVerification>> = _pendingVerifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _documentToViewUrl = MutableStateFlow<String?>(null)
    val documentToViewUrl: StateFlow<String?> = _documentToViewUrl.asStateFlow()

    private val _documentBitmap = MutableStateFlow<ImageBitmap?>(null)
    val documentBitmap: StateFlow<ImageBitmap?> = _documentBitmap.asStateFlow()

    private val _isDocumentLoading = MutableStateFlow(false)
    val isDocumentLoading: StateFlow<Boolean> = _isDocumentLoading.asStateFlow()

    fun fetchPendingVerifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            // The token fetching logic has been removed.
            try {
                // The token is no longer passed to the repository.
                _pendingVerifications.value = adminRepository.getPendingVerifications()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveRequest(verificationId: String) {
        viewModelScope.launch {
            // The token fetching logic has been removed.
            try {
                // The token is no longer passed to the repository.
                adminRepository.approveVerification(verificationId)
                fetchPendingVerifications()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to approve: ${e.message}"
            }
        }
    }

    fun rejectRequest(verificationId: String, reason: String) {
        viewModelScope.launch {
            // The token fetching logic has been removed.
            try {
                // The token is no longer passed to the repository.
                adminRepository.rejectVerification(verificationId, reason)
                fetchPendingVerifications()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reject: ${e.message}"
            }
        }
    }

    fun viewDocument(url: String) {
        viewModelScope.launch {
            _isDocumentLoading.value = true
            _documentToViewUrl.value = url
            _documentBitmap.value = null // Clear previous image
            // The token fetching logic has been removed.
            try {
                // The token is no longer passed to the repository.
                val responseBody = adminRepository.getDocument(url)
                val bytes = responseBody.bytes()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                _documentBitmap.value = bitmap.asImageBitmap()
            } catch (e: Exception) {
                println("Failed to load document: ${e.message}")
            } finally {
                _isDocumentLoading.value = false
            }
        }
    }

    fun dismissDocumentView() {
        _documentToViewUrl.value = null
        _documentBitmap.value = null
    }
}