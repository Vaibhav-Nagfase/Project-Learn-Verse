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

    suspend fun createActivity(request: CreateActivityRequest): Activity {
        val response = api.createActivity(request)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to create activity (Code: ${response.code()}): ${response.errorBody()?.string()}")
    }

    suspend fun getMyActivities(): List<Activity> {
        val response = api.getMyTutorActivities()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to fetch tutor activities: ${response.message()}")
    }

    suspend fun updateActivity(activityId: String, request: CreateActivityRequest): Activity {
        val response = api.updateActivity(activityId, request)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to update activity (Code: ${response.code()}): ${response.errorBody()?.string()}")
    }

    suspend fun deleteActivity(activityId: String) {
        val response = api.deleteActivity(activityId)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete activity (Code: ${response.code()}): ${response.errorBody()?.string()}")
        }
    }

    // TutorRepository.kt
    suspend fun registerTutor(
        email: String,
        fullName: String,
        phone: String,
        bio: String,
        qualifications: List<String>,
        experience: String,
        specializations: List<String>,
        termsAccepted: Boolean,
        profilePictureFile: File,
        profilePictureMimeType: String,
        idDocumentFile: File,
        idDocumentMimeType: String,
        certificateFile: File,
        certificateMimeType: String
    ) {
        // Create multipart parts for files
        val profilePicturePart = MultipartBody.Part.createFormData(
            "profilePicture",
            profilePictureFile.name,
            profilePictureFile.asRequestBody(profilePictureMimeType.toMediaTypeOrNull())
        )

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

        val response = api.registerTutor(
            email = email.toRequestBody("text/plain".toMediaTypeOrNull()),
            fullName = fullName.toRequestBody("text/plain".toMediaTypeOrNull()),
            phone = phone.toRequestBody("text/plain".toMediaTypeOrNull()),
            bio = bio.toRequestBody("text/plain".toMediaTypeOrNull()),
            qualifications = qualifications,
            experience = experience.toRequestBody("text/plain".toMediaTypeOrNull()),
            specializations = specializations,
            termsAccepted = termsAccepted.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            profilePicture = profilePicturePart,
            idDocument = idDocumentPart,
            certificate = certificatePart
        )

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            throw Exception("Failed to submit: ${response.code()} - $errorBody")
        }
    }

    suspend fun getTutorVerificationStatus(email: String): TutorVerificationStatus {
        val response = api.getTutorVerificationStatus(email)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to get verification status")
    }
}