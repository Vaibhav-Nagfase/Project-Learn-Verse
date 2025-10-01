package com.example.learnverse.data.remote

import com.example.learnverse.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.HTTP
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Url

interface ApiService {

    @POST("auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/activities/my-feed")
    suspend fun getMyFeed(
        @Header("Authorization") token: String
    ): Response<List<Activity>>

    // NEW: Get the current user's interests
    @GET("api/user/interests")
    suspend fun getUserInterests(@Header("Authorization") token: String): Response<UserInterestsResponse>

    // UPDATE: The add function will now use InterestsUpdateRequest
    @POST("api/user/interests/add")
    suspend fun addUserInterests(
        @Header("Authorization") token: String,
        @Body interestsRequest: InterestsUpdateRequest
    ): Response<Unit>

    // NEW: Remove interests from the user's profile
    @HTTP(method = "DELETE", path = "api/user/interests/remove", hasBody = true)
    suspend fun removeUserInterests(
        @Header("Authorization") token: String,
        @Body interestsRequest: InterestsUpdateRequest
    ): Response<Unit>

    // Kept for future use, as requested
    @GET("api/activities/all")
    suspend fun searchActivities(
        @Header("Authorization") token: String,
        @Query("search") query: String
    ): Response<List<Activity>>

    @POST("api/activities/search/natural")
    suspend fun naturalSearch(
        @Header("Authorization") token: String,
        @Body request: NaturalSearchRequest,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PagedResponse<Activity>>

    @GET("api/activities/filter")
    suspend fun filterActivities(
        @Header("Authorization") token: String,
        @QueryMap filters: Map<String, String> // Pass all filters as a map
    ): Response<PagedResponse<Activity>>

    @GET("api/activities/filter/proximity")
    suspend fun getNearbyActivities(
        @Header("Authorization") token: String,
        @Query("userLatitude") latitude: Double,
        @Query("userLongitude") longitude: Double,
        @Query("maxDistanceKm") distance: Int = 1000 // Default to 20km
    ): Response<PagedResponse<Activity>> // Assuming it's a paged response like filter

    // NEW: Submit tutor verification request with documents
    @Multipart
    @POST("api/tutor-verification/register")
    suspend fun registerTutor(
        @Header("Authorization") token: String,
        @Part("email") email: RequestBody,
        @Part("fullName") fullName: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("termsAccepted") termsAccepted: RequestBody,
        @Part idDocument: MultipartBody.Part,
        @Part certificate: MultipartBody.Part
    ): Response<Unit> // Assuming a simple success/fail response

    // NEW: Check the verification status
    @GET("api/tutor-verification/status/{email}")
    suspend fun getTutorVerificationStatus(
        @Header("Authorization") token: String,
        @Path("email") email: String
    ): Response<TutorVerificationStatus>



    // --- ADMIN ENDPOINTS ---

    @GET("api/tutor-verification/admin/pending")
    suspend fun getPendingVerifications(
        @Header("Authorization") token: String
    ): Response<List<PendingVerification>>

    @POST("api/tutor-verification/admin/approve/{verificationId}")
    suspend fun approveVerification(
        @Header("Authorization") token: String,
        @Path("verificationId") verificationId: String
    ): Response<Unit> // Assuming a simple success response

    @POST("api/tutor-verification/admin/reject/{verificationId}")
    suspend fun rejectVerification(
        @Header("Authorization") token: String,
        @Path("verificationId") verificationId: String,
        @Query("reason") reason: String
    ): Response<Unit>

    // This is a special function to view the document image from a full URL
    @GET
    suspend fun getDocumentImage(@Url url: String, @Header("Authorization") token: String): Response<okhttp3.ResponseBody>

}