package com.example.learnverse.data.remote

import com.example.learnverse.data.model.*
import com.example.learnverse.data.model.profile.ProfileResponse
import com.example.learnverse.data.model.profile.UserProfile
import com.example.learnverse.data.model.profile.UserProfileRequest
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

    // --- ADD SYNCHRONOUS REFRESH ENDPOINT ---
    @POST("auth/refresh-token")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<AuthResponse> // Note: Not suspend, returns Call

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
    @POST("api/tutor-verification/submit")
    suspend fun registerTutor(
        @Part("email") email: RequestBody,
        @Part("fullName") fullName: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("bio") bio: RequestBody,
        @Part("qualifications") qualifications: List<String>,
        @Part("experience") experience: RequestBody,
        @Part("specializations") specializations: List<String>,
        @Part("termsAccepted") termsAccepted: RequestBody,
        @Part profilePicture: MultipartBody.Part,
        @Part idDocument: MultipartBody.Part,
        @Part certificate: MultipartBody.Part
    ): Response<Unit>

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
    ): Response<Map<String, Any>>

    @PUT("api/tutor-verification/admin/reject/{verificationId}")
    suspend fun rejectVerification(
        @Path("verificationId") verificationId: String,
        @Query("reason") reason: String
    ): Response<Unit>

    @GET("api/tutor-verification/admin/verification/{verificationId}")
    suspend fun getVerificationDetails(
        @Path("verificationId") verificationId: String
    ): Response<PendingVerification>


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

    @GET("api/user/profile/get_profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @POST("api/user/profile/setup")
    suspend fun setupProfile(@Body profileRequest: UserProfileRequest): Response<UserProfile>

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
        @Body request: CreateReviewRequest
    ): Response<AddReviewResponse>

    /**
     * Get all reviews for an activity (PUBLIC)
     */
    @GET("api/activities/{activityId}/reviews")
    suspend fun getActivityReviews(
        @Path("activityId") activityId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<ReviewsResponse>

    /**
     * Update own review
     */
    @PUT("api/reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewId: String,
        @Body request: UpdateReviewRequest
    ): Response<UpdateReviewResponse>

    /**
     * Delete own review
     */
    @DELETE("api/reviews/{reviewId}")
    suspend fun deleteReview(
        @Path("reviewId") reviewId: String
    ): Response<DeleteReviewResponse>

    /**
     * Get user's own reviews
     */
    @GET("api/reviews/my-reviews")
    suspend fun getMyReviews(): Response<MyReviewsResponse>

    /**
     * Check if user has reviewed an activity
     */
    @GET("api/activities/{activityId}/reviews/check")
    suspend fun checkUserReview(
        @Path("activityId") activityId: String
    ): Response<CheckReviewResponse>

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
        @Body enrollmentData: @JvmSuppressWildcards Map<String, String>
    ): Response<Map<String, Any>>

    /**
     * ✅ Delete video from activity
     */
    @DELETE("api/tutor/activities/{activityId}/videos/{videoId}")
    suspend fun deleteVideo(
        @Path("activityId") activityId: String,
        @Path("videoId") videoId: String
    ): Response<Unit>

    /**
     * ✅ Delete meeting link
     */
    @DELETE("api/tutor/activities/{activityId}/meeting")
    suspend fun deleteMeetingLink(
        @Path("activityId") activityId: String
    ): Response<Unit>


    /**
     * ✅ Add video with URL (no file upload)
     */
    @POST("api/tutor/activities/{activityId}/videos")
    suspend fun addVideoWithUrl(
        @Path("activityId") activityId: String,
        @Body videoData: @JvmSuppressWildcards Map<String, Any>
    ): Response<Map<String, Any>>

    /**
     * ✅ Upload video file to activity
     */
    @Multipart
    @POST("api/tutor/activities/{activityId}/videos/upload")
    suspend fun uploadVideoFile(
        @Path("activityId") activityId: String,
        @Part file: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("order") order: RequestBody,
        @Part("isPreview") isPreview: RequestBody
    ): Response<Map<String, Any>>

    /**
     * ✅ Update video details
     */
    @PUT("api/tutor/activities/{activityId}/videos/{videoId}")
    suspend fun updateVideo(
        @Path("activityId") activityId: String,
        @Path("videoId") videoId: String,
        @Body videoData: @JvmSuppressWildcards Map<String, Any?>  // ✅ Fixed
    ): Response<Map<String, Any>>


    /**
     * ✅ Upload resource to video
     */
    @Multipart
    @POST("api/tutor/activities/{activityId}/videos/{videoId}/resources/upload")
    suspend fun uploadResourceFile(
        @Path("activityId") activityId: String,
        @Path("videoId") videoId: String,
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody,
        @Part("title") title: RequestBody
    ): Response<Map<String, Any>>

    /**
     * ✅ Add resource with URL
     */
    @POST("api/tutor/activities/{activityId}/videos/{videoId}/resources")
    suspend fun addResourceWithUrl(
        @Path("activityId") activityId: String,
        @Path("videoId") videoId: String,
        @Body resourceData: Map<String, String>
    ): Response<Map<String, Any>>

    /**
     * ✅ Delete resource
     */
    @DELETE("api/tutor/activities/{activityId}/videos/{videoId}/resources")
    suspend fun deleteResource(
        @Path("activityId") activityId: String,
        @Path("videoId") videoId: String,
        @Query("url") resourceUrl: String
    ): Response<Unit>

    /**
     * ✅ Update meeting details (changed from POST to PUT)
     */
    @PUT("api/tutor/activities/{activityId}/meeting")
    suspend fun updateMeetingDetails(
        @Path("activityId") activityId: String,
        @Body meetingData: Map<String, String>
    ): Response<Map<String, Any>>

    /**
     * Get complete home feed
     */
    @GET("api/activities/home-feed")
    suspend fun getHomeFeed(): Response<HomeFeedResponse>

}