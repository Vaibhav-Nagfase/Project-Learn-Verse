package com.example.learnverse.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.repository.TutorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

// The sealed class UI state remains unchanged.
sealed class VerificationUiState {
    data object Idle : VerificationUiState()
    data object Loading : VerificationUiState()
    data class Success(val message: String) : VerificationUiState()
    data class Error(val message: String) : VerificationUiState()
}

// The AuthRepository dependency has been removed from the constructor.
class TutorVerificationViewModel(
    application: Application,
    private val tutorRepository: TutorRepository
) : AndroidViewModel(application) {

    // --- State for the form fields (Unchanged) ---
    var fullName by mutableStateOf("")
    var phone by mutableStateOf("")
    var termsAccepted by mutableStateOf(false)
    var idDocumentUri by mutableStateOf<Uri?>(null)
    var certificateUri by mutableStateOf<Uri?>(null)

    // --- State for the overall UI (Unchanged) ---
    private val _uiState = MutableStateFlow<VerificationUiState>(VerificationUiState.Idle)
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    fun submitVerificationRequest(userEmail: String) {
        viewModelScope.launch {
            _uiState.value = VerificationUiState.Loading

            // --- 1. Validation (Unchanged) ---
            if (fullName.isBlank() || phone.isBlank() || idDocumentUri == null || certificateUri == null) {
                _uiState.value = VerificationUiState.Error("Please fill all fields and select both documents.")
                return@launch
            }
            if (!termsAccepted) {
                _uiState.value = VerificationUiState.Error("You must accept the terms and conditions.")
                return@launch
            }

            // --- 2. Get Token logic has been completely removed. ---

            // --- 3. Convert URIs to Files (Unchanged) ---
            val context = getApplication<Application>().applicationContext
            val idDocumentMimeType = context.contentResolver.getType(idDocumentUri!!)
            val certificateMimeType = context.contentResolver.getType(certificateUri!!)

            if (idDocumentMimeType == null || certificateMimeType == null) {
                _uiState.value = VerificationUiState.Error("Could not determine file type. Please select a different file.")
                return@launch
            }

            val idDocumentFileName = getFileName(context, idDocumentUri!!)
            val certificateFileName = getFileName(context, certificateUri!!)
            val idDocumentFile = uriToFile(context, idDocumentUri!!, idDocumentFileName)
            val certificateFile = uriToFile(context, certificateUri!!, certificateFileName)

            if (idDocumentFile == null || certificateFile == null) {
                _uiState.value = VerificationUiState.Error("Failed to process files. Please try again.")
                return@launch
            }

            // --- 4. Call the Repository ---
            try {
                // The 'token' parameter has been removed from the repository call.
                tutorRepository.registerTutor(
                    email = userEmail,
                    fullName = fullName,
                    phone = phone,
                    termsAccepted = termsAccepted,
                    idDocumentFile = idDocumentFile,
                    idDocumentMimeType = idDocumentMimeType,
                    certificateFile = certificateFile,
                    certificateMimeType = certificateMimeType
                )
                _uiState.value = VerificationUiState.Success("Verification request submitted successfully!")
            } catch (e: Exception) {
                _uiState.value = VerificationUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    // Helper functions (uriToFile, getFileName) remain unchanged.
    private fun uriToFile(context: Context, uri: Uri, fileName: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result ?: "temp_file_with_unknown_ext"
    }
}