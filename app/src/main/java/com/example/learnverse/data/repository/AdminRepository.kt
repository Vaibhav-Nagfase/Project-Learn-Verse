package com.example.learnverse.data.repository

import com.example.learnverse.data.model.PendingVerification
import com.example.learnverse.data.remote.ApiService
import okhttp3.ResponseBody

class AdminRepository(private val api: ApiService) {

    // The 'token' parameter has been removed.
    suspend fun getPendingVerifications(): List<PendingVerification> {
        // The "Bearer $token" argument has been removed.
        val response = api.getPendingVerifications()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to get pending verifications")
    }

    // The 'token' parameter has been removed.
    suspend fun approveVerification(verificationId: String) {
        // The "Bearer $token" argument has been removed.
        val response = api.approveVerification(verificationId)
        if (!response.isSuccessful) {
            throw Exception("Failed to approve verification")
        }
    }

    // The 'token' parameter has been removed.
    suspend fun rejectVerification(verificationId: String, reason: String) {
        // The "Bearer $token" argument has been removed.
        val response = api.rejectVerification(verificationId, reason)
        if (!response.isSuccessful) {
            throw Exception("Failed to reject verification")
        }
    }
}