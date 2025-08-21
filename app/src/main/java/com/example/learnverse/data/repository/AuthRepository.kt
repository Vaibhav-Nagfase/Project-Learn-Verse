package com.example.learnverse.data.repository

import android.content.Context
import com.example.learnverse.data.model.AuthResponse
import com.example.learnverse.data.model.LoginRequest
import com.example.learnverse.data.model.RegisterRequest
import com.example.learnverse.data.remote.ApiService
import com.example.learnverse.utils.UserPreferences
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val api: ApiService,
                     private val context: Context
) {

    suspend fun registerUser(request: RegisterRequest): AuthResponse {
        val response = api.registerUser(request)
        // Save the token here
        UserPreferences.saveToken(context, response.access_token)
        return response
    }

    suspend fun registerTutor(request: RegisterRequest): AuthResponse {
        return api.registerTutor(request)
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val response = api.login(request)
        // Save the token here
        UserPreferences.saveToken(context, response.access_token)
        return response
    }

    fun getToken(): Flow<String?> {
        return UserPreferences.getToken(context)
    }

    suspend fun logout() {
        UserPreferences.clearToken(context)
    }
}