package com.example.learnverse.data.repository

import com.example.learnverse.data.model.PendingVerification
import com.example.learnverse.data.remote.ApiService

class AdminRepository(private val api: ApiService) {

    suspend fun getPendingVerifications(token: String): List<PendingVerification> {
        val response = api.getPendingVerifications("Bearer $token")
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to get pending verifications")
    }

    suspend fun approveVerification(token: String, verificationId: String) {
        val response = api.approveVerification("Bearer $token", verificationId)
        if (!response.isSuccessful) {
            throw Exception("Failed to approve verification")
        }
    }

    suspend fun rejectVerification(token: String, verificationId: String, reason: String) {
        val response = api.rejectVerification("Bearer $token", verificationId, reason)
        if (!response.isSuccessful) {
            throw Exception("Failed to reject verification")
        }
    }
}