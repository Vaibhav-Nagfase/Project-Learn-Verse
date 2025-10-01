package com.example.learnverse.data.repository

import com.example.learnverse.data.model.TutorVerificationStatus
import com.example.learnverse.data.remote.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class TutorRepository(private val api: ApiService) {

    suspend fun registerTutor(
        token: String,
        email: String,
        fullName: String,
        phone: String,
        termsAccepted: Boolean,
        idDocumentFile: File,
        certificateFile: File
    ) {
        // Convert files and text into the format Retrofit needs for multipart requests
        val idDocumentPart = MultipartBody.Part.createFormData(
            "idDocument",
            idDocumentFile.name,
            idDocumentFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
        val certificatePart = MultipartBody.Part.createFormData(
            "certificate",
            certificateFile.name,
            certificateFile.asRequestBody("application/pdf".toMediaTypeOrNull()) // Or image/*
        )

        val response = api.registerTutor(
            token = "Bearer $token",
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

    suspend fun getTutorVerificationStatus(token: String, email: String): TutorVerificationStatus {
        val response = api.getTutorVerificationStatus("Bearer $token", email)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to get verification status")
    }
}