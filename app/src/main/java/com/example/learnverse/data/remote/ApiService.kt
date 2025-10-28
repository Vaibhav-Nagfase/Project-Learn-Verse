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
import okhttp3.ResponseBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Url
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Streaming

interface ApiService {

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/activities/my-feed")
    suspend fun getMyFeed(): Response<List<Activity>>

    // NEW: Get the current user's interests
    @GET("api/user/interests")
    suspend fun getUserInterests(): Response<UserInterestsResponse>

    // UPDATE: The add function will now use InterestsUpdateRequest
    @POST("api/user/interests/add")
    suspend fun addUserInterests(
        @Body interestsRequest: InterestsUpdateRequest
    ): Response<Unit>

    // NEW: Remove interests from the user's profile
    @HTTP(method = "DELETE", path = "api/user/interests/remove", hasBody = true)
    suspend fun removeUserInterests(
        @Body interestsRequest: InterestsUpdateRequest
    ): Response<Unit>

    // Kept for future use, as requested
    @GET("api/activities/all")
    suspend fun searchActivities(
        @Query("search") query: String
    ): Response<List<Activity>>

    @POST("api/activities/search/natural")
    suspend fun naturalSearch(
        @Body request: NaturalSearchRequest,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PagedResponse<Activity>>

    @GET("api/activities/filter")
    suspend fun filterActivities(
        @QueryMap filters: Map<String, String> // Pass all filters as a map
    ): Response<PagedResponse<Activity>>

    @GET("api/activities/filter/proximity")
    suspend fun getNearbyActivities(
        @Query("userLatitude") latitude: Double,
        @Query("userLongitude") longitude: Double,
        @Query("maxDistanceKm") distance: Int = 1000 // Default to 20km
    ): Response<PagedResponse<Activity>> // Assuming it's a paged response like filter

    // NEW: Submit tutor verification request with documents
    @Multipart
    @POST("api/tutor-verification/register")
    suspend fun registerTutor(
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
        @Path("email") email: String
    ): Response<TutorVerificationStatus>



    // --- ADMIN ENDPOINTS ---

    @GET("api/tutor-verification/admin/pending")
    suspend fun getPendingVerifications(): Response<List<PendingVerification>>

    @PUT("api/tutor-verification/admin/approve/{verificationId}")
    suspend fun approveVerification(
        @Path("verificationId") verificationId: String
    ): Response<Unit> // Assuming a simple success response

    @POST("api/tutor-verification/admin/reject/{verificationId}")
    suspend fun rejectVerification(
        @Path("verificationId") verificationId: String,
        @Query("reason") reason: String
    ): Response<Unit>

    // This is a special function to view the document image from a full URL
    @GET
    suspend fun getDocumentImage(@Url url: String): Response<okhttp3.ResponseBody>

    // --- ENROLLMENT ENDPOINTS ---

//    @POST("api/enrollments/enroll")
//    suspend fun enrollInActivity(
//        @Body request: EnrollmentRequest
//    ): Response<Unit> // Assuming we don't need the response body for now

    @GET("api/enrollments/my-enrollments")
    suspend fun getMyEnrollments(): Response<MyEnrollmentsResponse>

    @POST("api/activities/by-ids")
    suspend fun getActivitiesByIds(
        @Body activityIds: List<String>
    ): Response<List<Activity>>


    // --- TUTOR ACTIVITY ENDPOINTS ---

    @POST("api/activities/create")
    suspend fun createActivity(@Body request: CreateActivityRequest): Response<Activity>

    // THIS IS THE CORRECTED LINE
    @GET("api/activities/my-activities")
    suspend fun getMyTutorActivities(): Response<List<Activity>>

    @PUT("api/activities/{id}")
    suspend fun updateActivity(
        @Path("id") activityId: String,
        @Body request: CreateActivityRequest
    ): Response<Activity>

    // --- ADDED: Endpoint to delete an activity ---
    @DELETE("api/activities/{id}")
    suspend fun deleteActivity(@Path("id") activityId: String): Response<Unit>

    // --- NEW PROFILE ENDPOINTS ---

    @POST("api/user/profile/setup")
    suspend fun setupProfile(@Body profileRequest: UserProfileRequest): Response<UserProfile>

    @GET("api/user/profile/get_profile")
    suspend fun getProfile(): Response<UserProfile>

    @PUT("api/user/profile/update_profile")
    suspend fun updateProfile(@Body profileRequest: UserProfileRequest): Response<UserProfile>


    // --- Community Post Endpoints ---

    @Multipart // Use Multipart for file uploads
    @POST("api/community/posts")
    suspend fun createPost(
        @Part("content") content: RequestBody?, // Nullable if only media
        @Part file: MultipartBody.Part? // Nullable if only text
    ): Response<CommunityPost> // Assuming backend returns the created post

    @GET("api/community/posts/feed")
    suspend fun getCommunityFeed(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<CommunityFeedResponse>

    @POST("api/community/posts/{postId}/like")
    suspend fun likePost(@Path("postId") postId: String): Response<CommunityPost> // Returns updated post

    // Assuming unlike uses the same endpoint, check with backend if DELETE is needed
    // @DELETE("api/community/posts/{postId}/like")
    // suspend fun unlikePost(@Path("postId") postId: String): Response<CommunityPost>

    @FormUrlEncoded
    @POST("api/community/posts/{postId}/comments")
    suspend fun addComment(
        @Path("postId") postId: String,
        @Field("content") content: String
    ): Response<CommunityPost> // Returns updated post with new comment

    @POST("api/community/posts/{postId}/comments/{commentId}/like")
    suspend fun likeComment(
        @Path("postId") postId: String,
        @Path("commentId") commentId: String
    ): Response<CommunityPost> // Returns updated post

    @Multipart // Assuming update might also change media
    @PUT("api/community/posts/{postId}")
    suspend fun updatePost(
        @Path("postId") postId: String,
        @Part("content") content: RequestBody?,
        @Part file: MultipartBody.Part?
        // Add other fields if needed for update
    ): Response<CommunityPost> // Assuming returns updated post

    @DELETE("api/community/posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: String): Response<Unit>

    @GET("api/community/posts/user/{userId}")
    suspend fun getUserPosts(
        @Path("userId") userId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<CommunityFeedResponse> // Assuming same response structure as feed

    // --- Follow Endpoints ---

    @POST("api/community/follow/{userIdToFollow}")
    suspend fun followUser(@Path("userIdToFollow") userIdToFollow: String): Response<FollowResponse>

    @DELETE("api/community/follow/{userIdToUnfollow}")
    suspend fun unfollowUser(@Path("userIdToUnfollow") userIdToUnfollow: String): Response<Unit>

    @GET("api/community/follow/following")
    suspend fun getFollowingList(): Response<List<String>> // List of user IDs

    @GET("api/community/follow/stats/{userId}")
    suspend fun getFollowStats(@Path("userId") userId: String): Response<FollowStats>

    /**
     * Upload banner for activity
     */
    @Multipart
    @PUT("api/activities/tutor/activities/{activityId}/banner")
    suspend fun uploadBanner(
        @Path("activityId") activityId: String,
        @Part banner: MultipartBody.Part
    ): Response<Map<String, Any>>

    /**
     * Add review to activity
     */
    @POST("api/activities/{activityId}/reviews")
    suspend fun addReview(
        @Path("activityId") activityId: String,
        @Body reviewData: Map<String, Any>
    ): Response<Map<String, Any>>

    /**
     * Get single activity details
     */
    @GET("api/activities/{activityId}")
    suspend fun getActivityById(
        @Path("activityId") activityId: String
    ): Response<Activity>

    /**
     * Enroll in activity
     */
    @POST("api/enrollments/enroll")
    suspend fun enrollInActivity(
        @Body enrollmentData: Map<String, String>
    ): Response<Map<String, Any>>
}