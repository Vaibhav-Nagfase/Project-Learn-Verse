package com.example.learnverse.data.repository

import com.example.learnverse.data.model.CreateActivityRequest
import com.example.learnverse.data.model.TutorVerificationStatus
import com.example.learnverse.data.remote.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import com.example.learnverse.data.model.Activity


class TutorRepository(private val api: ApiService) {

    // --- NEW: Method to create a new activity ---
    suspend fun createActivity(request: CreateActivityRequest): Activity {
        val response = api.createActivity(request)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        // This provides a more detailed error message for easier debugging
        throw Exception("Failed to create activity (Code: ${response.code()}): ${response.errorBody()?.string()}")
    }

    // --- NEW: Method to get the current tutor's activities ---
    suspend fun getMyActivities(): List<Activity> {
        val response = api.getMyTutorActivities()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to fetch tutor activities: ${response.message()}")
    }

    // --- ADDED: Method to update an activity ---
    suspend fun updateActivity(activityId: String, request: CreateActivityRequest): Activity {
        val response = api.updateActivity(activityId, request)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to update activity (Code: ${response.code()}): ${response.errorBody()?.string()}")
    }

    // --- ADDED: Method to delete an activity ---
    suspend fun deleteActivity(activityId: String) {
        val response = api.deleteActivity(activityId)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete activity (Code: ${response.code()}): ${response.errorBody()?.string()}")
        }
    }

    // The 'token' parameter has been removed.
    suspend fun registerTutor(
        email: String,
        fullName: String,
        phone: String,
        termsAccepted: Boolean,
        idDocumentFile: File,
        idDocumentMimeType: String,
        certificateFile: File,
        certificateMimeType: String
    ) {
        val idDocumentPart = MultipartBody.Part.createFormData(
            "idDocument",
            idDocumentFile.name,
            idDocumentFile.asRequestBody(idDocumentMimeType.toMediaTypeOrNull())
        )
        val certificatePart = MultipartBody.Part.createFormData(
            "certificate",
            certificateFile.name,
            certificateFile.asRequestBody(certificateMimeType.toMediaTypeOrNull())
        )

        // The "token" argument has been removed.
        val response = api.registerTutor(
            email = email.toRequestBody("text/plain".toMediaTypeOrNull()),
            fullName = fullName.toRequestBody("text/plain".toMediaTypeOrNull()),
            phone = phone.toRequestBody("text/plain".toMediaTypeOrNull()),
            termsAccepted = termsAccepted.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            idDocument = idDocumentPart,
            certificate = certificatePart
        )
        if (!response.isSuccessful) {
            throw Exception("Failed to submit verification request: ${response.message()}")
        }
    }

    // The 'token' parameter has been removed.
    suspend fun getTutorVerificationStatus(email: String): TutorVerificationStatus {
        // The "Bearer $token" argument has been removed.
        val response = api.getTutorVerificationStatus(email)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to get verification status")
    }
}