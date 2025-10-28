package com.example.learnverse.data.repository

import android.content.Context
import com.example.learnverse.data.model.*
import com.example.learnverse.data.remote.ApiService
import com.example.learnverse.utils.UserPreferences
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class AuthRepository(private val api: ApiService, private val context: Context) {

    suspend fun registerUser(request: RegisterRequest): AuthResponse {
        val response = api.registerUser(request)
        if (response.isSuccessful && response.body() != null) {
            UserPreferences.saveToken(context, response.body()!!.accessToken)
            return response.body()!!
        }
        throw Exception("Registration failed: ${response.message()}")
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val response = api.login(request)
        if (response.isSuccessful && response.body() != null) {
            UserPreferences.saveToken(context, response.body()!!.accessToken)
            return response.body()!!
        }
        throw Exception("Login failed: ${response.message()}")
    }

    suspend fun getUserInterests(): UserInterestsResponse {
        // The "Bearer $token" argument has been removed.
        val response = api.getUserInterests()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to get user interests")
    }

    // The 'token' parameter has been removed.
    suspend fun addUserInterests(interests: List<String>) {
        // The "Bearer $token" argument has been removed.
        val response = api.addUserInterests(InterestsUpdateRequest(interests))
        if (!response.isSuccessful) {
            throw Exception("Failed to add interests: ${response.code()} ${response.message()}")
        }
    }

    // The 'token' parameter has been removed.
    suspend fun removeUserInterests(interests: List<String>) {
        // The "Bearer $token" argument has been removed.
        val response = api.removeUserInterests(InterestsUpdateRequest(interests))
        if (!response.isSuccessful) {
            throw Exception("Failed to remove interests")
        }
    }

    suspend fun logout() {
        try {
            // 1. Call the server to invalidate the token.
            // The AuthInterceptor will automatically add the
            // current token to this request.
            api.logout()
        } catch (e: Exception) {
            // Log the error, but don't stop.
            // We must always clear the local token.
            println("Server logout failed, clearing local token anyway: ${e.message}")
        }
        UserPreferences.clearToken(context)
        saveInterestsSkippedFlag(false)
    }

    fun getToken(): Flow<String?> {
        return UserPreferences.getToken(context)
    }

    suspend fun saveInterestsSkippedFlag(skipped: Boolean) {
        UserPreferences.saveInterestsSkippedFlag(context, skipped)
    }

    fun getInterestsSkippedFlag(): Flow<Boolean> {
        return UserPreferences.getInterestsSkippedFlag(context)
    }

    // AuthRepository.kt
    suspend fun getTutorVerificationStatus(email: String): Response<TutorVerificationStatus> {
        return api.getTutorVerificationStatus(email)
    }

}