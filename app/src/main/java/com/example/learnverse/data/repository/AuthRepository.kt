package com.example.learnverse.data.repository

import android.content.Context
import com.example.learnverse.data.model.*
import com.example.learnverse.data.remote.ApiService
import com.example.learnverse.utils.UserPreferences
import kotlinx.coroutines.flow.Flow

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

    suspend fun addUserInterests(token: String, interests: List<String>) {
        val response = api.addUserInterests("Bearer $token", InterestsRequest(interests))
        if (!response.isSuccessful) {
            throw Exception("Failed to add interests: ${response.code()} ${response.message()}")
        }
    }

    suspend fun logout() {
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
}