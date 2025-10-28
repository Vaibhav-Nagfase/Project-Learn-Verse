package com.example.learnverse.data.repository

import com.example.learnverse.data.model.Activity
import com.example.learnverse.data.model.ActivityFilter
import com.example.learnverse.data.model.NaturalSearchRequest
import com.example.learnverse.data.remote.ApiService
import com.example.learnverse.data.model.ActivitiesByIdsRequest
import com.example.learnverse.data.model.AddReviewResponse
import com.example.learnverse.data.model.CreateReviewRequest
import com.example.learnverse.data.model.DeleteReviewResponse
import com.example.learnverse.data.model.EnrollmentRequest
import com.example.learnverse.data.model.MyReviewsResponse
import com.example.learnverse.data.model.ReviewsResponse
import com.example.learnverse.data.model.UpdateReviewRequest
import com.example.learnverse.data.model.UpdateReviewResponse
import okhttp3.MultipartBody
import retrofit2.Response

class ActivitiesRepository(private val api: ApiService) {

    // The 'token' parameter has been removed.
    suspend fun getMyFeed(): List<Activity> {
        // The "Bearer $token" argument has been removed.
        val response = api.getMyFeed()
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        }
        throw Exception("Failed to fetch feed: ${response.message()}")
    }

    // The 'token' parameter has been removed.
    suspend fun searchActivities(query: String): List<Activity> {
        // The "Bearer $token" argument has been removed.
        val response = api.searchActivities(query)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        }
        throw Exception("Search failed: ${response.message()}")
    }

    // The 'token' parameter has been removed.
    suspend fun filterActivities(filter: ActivityFilter): List<Activity> {
        val filterMap = mutableMapOf<String, String>()

        filter.subjects?.let { if (it.isNotEmpty()) filterMap["subjects"] = it.joinToString(",") }
        filter.activityTypes?.let { if (it.isNotEmpty()) filterMap["activityTypes"] = it.joinToString(",") }
        filter.modes?.let { if (it.isNotEmpty()) filterMap["modes"] = it.joinToString(",") }
        filter.difficulties?.let { if (it.isNotEmpty()) filterMap["difficulties"] = it.joinToString(",") }
        filter.cities?.let { if (it.isNotEmpty()) filterMap["cities"] = it.joinToString(",") }
        filter.minPrice?.let { filterMap["minPrice"] = it.toString() }
        filter.maxPrice?.let { filterMap["maxPrice"] = it.toString() }
        filter.demoAvailable?.let { filterMap["demoAvailable"] = it.toString() }
        filter.sortBy?.let { filterMap["sortBy"] = it }
        filter.sortDirection?.let { filterMap["sortDirection"] = it }
        filter.searchQuery?.let { if (it.isNotBlank()) filterMap["searchQuery"] = it }

        // The "Bearer $token" argument has been removed.
        val response = api.filterActivities(filterMap)

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.content
        }

        throw Exception("Failed to apply filters: ${response.message()}")
    }

    // The 'token' parameter has been removed.
    suspend fun getNearbyActivities(latitude: Double, longitude: Double): List<Activity> {
        // The "Bearer $token" argument has been removed.
        val response = api.getNearbyActivities(latitude, longitude)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.content
        }
        throw Exception("Failed to get nearby activities: ${response.message()}")
    }

    // The 'token' parameter has been removed.
    suspend fun naturalSearch(request: NaturalSearchRequest): List<Activity> {
        // The "Bearer $token" argument has been removed.
        val response = api.naturalSearch(request)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.content
        }
        throw Exception("Natural search failed: ${response.message()}")
    }

    // The 'token' parameter has been removed.
//    suspend fun enrollInActivity(activityId: String) {
//        // The "Bearer $token" argument has been removed.
//        val response = api.enrollInActivity(EnrollmentRequest(activityId))
//        if (!response.isSuccessful) {
//            throw Exception("Failed to enroll: ${response.message()}")
//        }
//    }

    // The 'token' parameter has been removed.
    suspend fun getMyEnrolledActivities(): List<Activity> {
        // The "Bearer $token" argument has been removed.
        val enrollmentsResponse = api.getMyEnrollments()
        if (!enrollmentsResponse.isSuccessful || enrollmentsResponse.body() == null) {
            throw Exception("Failed to fetch enrollments")
        }

        val enrollments = enrollmentsResponse.body()!!.enrollments
        if (enrollments.isEmpty()) {
            return emptyList()
        }

        val activityIds = enrollments.map { it.activityId }

        // The "Bearer $token" argument has been removed.
        val activitiesResponse = api.getActivitiesByIds(activityIds)
        if (activitiesResponse.isSuccessful && activitiesResponse.body() != null) {
            return activitiesResponse.body()!!
        }

        throw Exception("Failed to fetch activity details for enrollments")
    }

    /**
     * Upload banner image
     */
    suspend fun uploadBanner(
        activityId: String,
        banner: MultipartBody.Part
    ): Response<Map<String, Any>> {
        return api.uploadBanner(activityId, banner)
    }

    /**
     * Get activity by ID
     */
    suspend fun getActivityById(activityId: String): Response<Activity> {
        return api.getActivityById(activityId)
    }

    /**
     * Enroll in activity
     */
    suspend fun enrollInActivity(activityId: String): Response<Map<String, Any>> {
        val enrollmentData = mapOf("activityId" to activityId) // ✅ Wrap in Map
        return api.enrollInActivity(enrollmentData)
    }

    /**
     * Get reviews for activity
     */
    suspend fun getActivityReviews(
        activityId: String,
        page: Int = 0,
        size: Int = 10
    ): ReviewsResponse {
        val response = api.getActivityReviews(activityId, page, size)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to fetch reviews: ${response.message()}")
    }

    /**
     * Add review
     */
    suspend fun addReview(
        activityId: String,
        rating: Int,
        feedback: String?
    ): Response<AddReviewResponse> {
        val request = CreateReviewRequest(rating, feedback)
        return api.addReview(activityId, request)
    }

    /**
     * Update review
     */
    suspend fun updateReview(
        reviewId: String,
        rating: Int?,
        feedback: String?
    ): Response<UpdateReviewResponse> {
        val request = UpdateReviewRequest(rating, feedback)
        return api.updateReview(reviewId, request)
    }

    /**
     * Delete review
     */
    suspend fun deleteReview(reviewId: String): Response<DeleteReviewResponse> {
        return api.deleteReview(reviewId)
    }

    /**
     * Get my reviews
     */
    suspend fun getMyReviews(): MyReviewsResponse {
        val response = api.getMyReviews()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to fetch your reviews: ${response.message()}")
    }

    /**
     * Check if user has reviewed
     */
    suspend fun checkUserReview(activityId: String): Boolean {
        val response = api.checkUserReview(activityId)
        return response.isSuccessful && response.body()?.hasReviewed == true
    }

}