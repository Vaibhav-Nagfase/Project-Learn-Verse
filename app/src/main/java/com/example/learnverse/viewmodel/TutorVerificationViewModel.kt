// TutorVerificationViewModel.kt
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

sealed class VerificationUiState {
    data object Idle : VerificationUiState()
    data object Loading : VerificationUiState()
    data class Success(val message: String) : VerificationUiState()
    data class Error(val message: String) : VerificationUiState()
}

class TutorVerificationViewModel(
    application: Application,
    private val tutorRepository: TutorRepository
) : AndroidViewModel(application) {

    // Form fields
    var fullName by mutableStateOf("")
    var phone by mutableStateOf("")
    var bio by mutableStateOf("")
    var qualifications by mutableStateOf("")
    var experience by mutableStateOf("")
    var specializations by mutableStateOf("")
    var termsAccepted by mutableStateOf(false)

    // File URIs
    var profilePictureUri by mutableStateOf<Uri?>(null)
    var idDocumentUri by mutableStateOf<Uri?>(null)
    var certificateUri by mutableStateOf<Uri?>(null)

    private val _uiState = MutableStateFlow<VerificationUiState>(VerificationUiState.Idle)
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    fun submitVerificationRequest(userEmail: String) {
        viewModelScope.launch {
            _uiState.value = VerificationUiState.Loading

            // Validation
            if (fullName.isBlank() || phone.isBlank() || bio.isBlank() ||
                qualifications.isBlank() || experience.isBlank() || specializations.isBlank() ||
                profilePictureUri == null || idDocumentUri == null || certificateUri == null) {
                _uiState.value = VerificationUiState.Error("Please fill all required fields and select all documents.")
                return@launch
            }

            if (!termsAccepted) {
                _uiState.value = VerificationUiState.Error("You must accept the terms and conditions.")
                return@launch
            }

            val context = getApplication<Application>().applicationContext

            // Convert URIs to files
            val profilePictureMimeType = context.contentResolver.getType(profilePictureUri!!)
            val idDocumentMimeType = context.contentResolver.getType(idDocumentUri!!)
            val certificateMimeType = context.contentResolver.getType(certificateUri!!)

            if (profilePictureMimeType == null || idDocumentMimeType == null || certificateMimeType == null) {
                _uiState.value = VerificationUiState.Error("Could not determine file types. Please select different files.")
                return@launch
            }

            val profilePictureFile = uriToFile(context, profilePictureUri!!, getFileName(context, profilePictureUri!!))
            val idDocumentFile = uriToFile(context, idDocumentUri!!, getFileName(context, idDocumentUri!!))
            val certificateFile = uriToFile(context, certificateUri!!, getFileName(context, certificateUri!!))

            if (profilePictureFile == null || idDocumentFile == null || certificateFile == null) {
                _uiState.value = VerificationUiState.Error("Failed to process files. Please try again.")
                return@launch
            }

            try {
                // Split comma-separated strings into lists
                val qualificationsList = qualifications.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val specializationsList = specializations.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                tutorRepository.registerTutor(
                    email = userEmail,
                    fullName = fullName,
                    phone = phone,
                    bio = bio,
                    qualifications = qualificationsList,
                    experience = experience,
                    specializations = specializationsList,
                    termsAccepted = termsAccepted,
                    profilePictureFile = profilePictureFile,
                    profilePictureMimeType = profilePictureMimeType,
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
        return result ?: "temp_file"
    }
}