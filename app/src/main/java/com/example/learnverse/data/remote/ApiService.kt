package com.example.learnverse.data.remote

import com.example.learnverse.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("auth/register-user")
    suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/activities/my-feed")
    suspend fun getMyFeed(
        @Header("Authorization") token: String
    ): Response<List<Activity>>

    @POST("api/user/interests/add")
    suspend fun addUserInterests(
        @Header("Authorization") token: String,
        @Body interestsRequest: InterestsRequest
    ): Response<Unit>

    // Kept for future use, as requested
    @GET("api/activities/all")
    suspend fun searchActivities(
        @Header("Authorization") token: String,
        @Query("search") query: String
    ): Response<List<Activity>>
}