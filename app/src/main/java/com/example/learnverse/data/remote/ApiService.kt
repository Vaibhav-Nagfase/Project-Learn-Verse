package com.example.learnverse.data.remote

import com.example.learnverse.data.model.ActivityResponse
import com.example.learnverse.data.model.AuthResponse
import com.example.learnverse.data.model.LoginRequest
import com.example.learnverse.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("auth/register-user")
    suspend fun registerUser(@Body request: RegisterRequest): AuthResponse

    @POST("auth/register-tutor")
    suspend fun registerTutor(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("api/activities/fetch")
    suspend fun getActivities(
        @Header("Authorization") token: String
    ): List<ActivityResponse>
}

