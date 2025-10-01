package com.example.learnverse.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.repository.AuthRepository
import com.example.learnverse.data.repository.TutorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

// A state class for the screen's UI
sealed class VerificationUiState {
    data object Idle : VerificationUiState()
    data object Loading : VerificationUiState()
    data class Success(val message: String) : VerificationUiState()
    data class Error(val message: String) : VerificationUiState()
}

class TutorVerificationViewModel(
    application: Application,
    private val tutorRepository: TutorRepository,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    // --- State for the form fields ---
    var fullName by mutableStateOf("")
    var phone by mutableStateOf("")
    var termsAccepted by mutableStateOf(false)
    var idDocumentUri by mutableStateOf<Uri?>(null)
    var certificateUri by mutableStateOf<Uri?>(null)

    // --- State for the overall UI ---
    private val _uiState = MutableStateFlow<VerificationUiState>(VerificationUiState.Idle)
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    fun submitVerificationRequest(userEmail: String) {
        viewModelScope.launch {
            _uiState.value = VerificationUiState.Loading

            // --- 1. Validation ---
            if (fullName.isBlank() || phone.isBlank() || idDocumentUri == null || certificateUri == null) {
                _uiState.value = VerificationUiState.Error("Please fill all fields and select both documents.")
                return@launch
            }
            if (!termsAccepted) {
                _uiState.value = VerificationUiState.Error("You must accept the terms and conditions.")
                return@launch
            }

            // --- 2. Get Token and User Email ---
            val token = authRepository.getToken().firstOrNull()

            if (token.isNullOrBlank()) {
                _uiState.value = VerificationUiState.Error("Authentication error. Please log in again.")
                return@launch
            }

            // --- 3. Convert URIs to Files ---
            val context = getApplication<Application>().applicationContext
            val idDocumentFile = uriToFile(context, idDocumentUri!!, "id_document")
            val certificateFile = uriToFile(context, certificateUri!!, "certificate")
            if (idDocumentFile == null || certificateFile == null) {
                _uiState.value = VerificationUiState.Error("Failed to process files. Please try again.")
                return@launch
            }

            // --- 4. Call the Repository ---
            try {
                tutorRepository.registerTutor(
                    token = token,
                    email = userEmail,
                    fullName = fullName,
                    phone = phone,
                    termsAccepted = termsAccepted,
                    idDocumentFile = idDocumentFile,
                    certificateFile = certificateFile
                )
                _uiState.value = VerificationUiState.Success("Verification request submitted successfully!")
            } catch (e: Exception) {
                _uiState.value = VerificationUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    // Helper function to copy a file from a Uri to the app's cache
    private fun uriToFile(context: android.content.Context, uri: Uri, fileName: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File(context.cacheDir, "$fileName.tmp")
            tempFile.createNewFile()
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}