package com.example.learnverse.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.learnverse.data.model.Activity
import com.example.learnverse.data.model.ActivityFilter
import com.example.learnverse.data.model.NaturalSearchRequest
import com.example.learnverse.data.remote.ApiService
import com.example.learnverse.data.model.ActivitiesByIdsRequest
import com.example.learnverse.data.model.AddReviewResponse
import com.example.learnverse.data.model.EnrollmentRequest
import com.example.learnverse.data.model.ReviewsResponse
import com.example.learnverse.data.model.CreateReviewRequest
import com.example.learnverse.data.model.DeleteReviewResponse
import com.example.learnverse.data.model.HomeFeedResponse
import com.example.learnverse.data.model.MyReviewsResponse
import com.example.learnverse.data.model.UpdateReviewRequest
import com.example.learnverse.data.model.UpdateReviewResponse
import com.example.learnverse.utils.ProgressRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File


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

//    // The 'token' parameter has been removed.
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

    /**
     * Get home feed
     */
    suspend fun getHomeFeed(): HomeFeedResponse {
        val response = api.getHomeFeed()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to fetch home feed: ${response.message()}")
    }

    /**
     * ========================================
     * VIDEO MANAGEMENT OPERATIONS
     * ========================================
     */

    /**
     * Add video with URL (YouTube, Vimeo, etc.)
     */
    suspend fun addVideoWithUrl(
        activityId: String,
        title: String,
        description: String,
        videoUrl: String,
        order: Int,
        isPreview: Boolean
    ): Response<Map<String, Any>> {
        val videoData = mapOf(
            "title" to title,
            "description" to description,
            "videoUrl" to videoUrl,
            "order" to order,
            "isPreview" to isPreview
        )
        return api.addVideoWithUrl(activityId, videoData)
    }

    /**
     * Upload video file from device
     */
    suspend fun uploadVideoFile(
        activityId: String,
        videoFile: File,
        title: String,
        description: String,
        order: Int,
        isPreview: Boolean,
        onProgress: (Int) -> Unit
    ): Response<Map<String, Any>> = withContext(Dispatchers.IO) {

        // Create progress-tracking request body
        val progressRequestBody = ProgressRequestBody(
            file = videoFile,
            contentType = "video/*".toMediaTypeOrNull(),
            onProgressUpdate = onProgress
        )

        val videoPart = MultipartBody.Part.createFormData(
            "file",
            videoFile.name,
            progressRequestBody
        )

        val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val descBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val orderBody = order.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val previewBody = isPreview.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        api.uploadVideoFile(activityId, videoPart, titleBody, descBody, orderBody, previewBody)
    }

    /**
     * Update existing video details
     */
    suspend fun updateVideo(
        activityId: String,
        videoId: String,
        title: String?,
        description: String?,
        videoUrl: String?,
        order: Int?,
        isPreview: Boolean?
    ): Response<Map<String, Any>> {
        val videoData = mutableMapOf<String, Any?>()
        title?.let { videoData["title"] = it }
        description?.let { videoData["description"] = it }
        videoUrl?.let { videoData["videoUrl"] = it }
        order?.let { videoData["order"] = it }
        isPreview?.let { videoData["isPreview"] = it }

        return api.updateVideo(activityId, videoId, videoData)
    }

    /**
     * Delete video
     */
    suspend fun deleteVideo(
        activityId: String,
        videoId: String
    ): Response<Unit> {
        return api.deleteVideo(activityId, videoId)
    }

    /**
     * ========================================
     * RESOURCE MANAGEMENT OPERATIONS
     * ========================================
     */

    /**
     * Upload resource file (PDF, DOC, etc.) to video
     */
    suspend fun uploadResourceFile(
        activityId: String,
        videoId: String,
        resourceFile: File,
        type: String,
        title: String
    ): Response<Map<String, Any>> {
        val filePart = MultipartBody.Part.createFormData(
            "file",
            resourceFile.name,
            resourceFile.asRequestBody("application/*".toMediaTypeOrNull())
        )

        val typeBody = type.toRequestBody("text/plain".toMediaTypeOrNull())
        val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())

        return api.uploadResourceFile(activityId, videoId, filePart, typeBody, titleBody)
    }

    /**
     * Add resource with URL
     */
    suspend fun addResourceWithUrl(
        activityId: String,
        videoId: String,
        type: String,
        title: String,
        url: String
    ): Response<Map<String, Any>> {
        val resourceData = mapOf(
            "type" to type,
            "title" to title,
            "url" to url
        )
        return api.addResourceWithUrl(activityId, videoId, resourceData)
    }

    /**
     * Delete resource from video
     */
    suspend fun deleteResource(
        activityId: String,
        videoId: String,
        resourceUrl: String
    ): Response<Unit> {
        return api.deleteResource(activityId, videoId, resourceUrl)
    }

    /**
     * ========================================
     * MEETING MANAGEMENT OPERATIONS
     * ========================================
     */

    /**
     * Update meeting details
     */
    suspend fun updateMeetingDetails(
        activityId: String,
        platform: String?,
        meetingLink: String?,
        meetingId: String?,
        passcode: String?
    ): Response<Map<String, Any>> {
        val meetingData = mutableMapOf<String, String>()
        platform?.let { meetingData["platform"] = it }
        meetingLink?.let { meetingData["meetingLink"] = it }
        meetingId?.let { meetingData["meetingId"] = it }
        passcode?.let { meetingData["passcode"] = it }

        return api.updateMeetingDetails(activityId, meetingData)
    }

    /**
     * Delete meeting link
     */
    suspend fun deleteMeetingLink(activityId: String): Response<Unit> {
        return api.deleteMeetingLink(activityId)
    }

    /**
     * ========================================
     * FILE UTILITY METHODS
     * ========================================
     */

    /**
     * Convert URI to File for upload
     */
    fun uriToFile(uri: Uri, context: Context): File {
        val contentResolver = context.contentResolver
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}")

        contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    /**
     * Get file name from URI
     */
    fun getFileName(uri: Uri, context: Context): String {
        var fileName = "file_${System.currentTimeMillis()}"

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                fileName = cursor.getString(nameIndex)
            }
        }

        return fileName
    }

    /**
     * Get file size from URI (in MB)
     */
    fun getFileSize(uri: Uri, context: Context): Long {
        var fileSize = 0L

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && sizeIndex != -1) {
                fileSize = cursor.getLong(sizeIndex)
            }
        }

        return fileSize / (1024 * 1024) // Convert to MB
    }

}