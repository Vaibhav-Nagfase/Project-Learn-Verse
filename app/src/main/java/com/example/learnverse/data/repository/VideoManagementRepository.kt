package com.example.learnverse.data.repository

import com.example.learnverse.data.model.*
import com.example.learnverse.data.remote.ApiService

class VideoManagementRepository(
    private val apiService: ApiService
) {

    // Video Management - uses YOUR existing endpoints
    suspend fun addVideo(activityId: String, request: AddVideoRequest) {
        // POST /api/tutor/activities/{activityId}/videos
        val response = apiService.addVideoToActivity(activityId, request)
        if (!response.isSuccessful) {
            throw Exception("Failed to add video: ${response.message()}")
        }
    }

    suspend fun updateVideo(activityId: String, videoId: String, request: UpdateVideoRequest) {
        // PUT /api/tutor/activities/{activityId}/videos/{videoId}
        val response = apiService.updateActivityVideo(activityId, videoId, request)
        if (!response.isSuccessful) {
            throw Exception("Failed to update video: ${response.message()}")
        }
    }

    suspend fun deleteVideo(activityId: String, videoId: String) {
        // DELETE /api/tutor/activities/{activityId}/videos/{videoId}
        val response = apiService.deleteActivityVideo(activityId, videoId)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete video: ${response.message()}")
        }
    }

    // Resource Management - uses YOUR existing endpoints
    suspend fun addResource(activityId: String, videoId: String, request: AddResourceRequest) {
        // POST /api/tutor/activities/{activityId}/videos/{videoId}/resources
        val response = apiService.addVideoResource(activityId, videoId, request)
        if (!response.isSuccessful) {
            throw Exception("Failed to add resource: ${response.message()}")
        }
    }

    suspend fun deleteResource(activityId: String, videoId: String, resourceUrl: String) {
        // DELETE /api/tutor/activities/{activityId}/videos/{videoId}/resources?url={resourceUrl}
        val response = apiService.deleteVideoResource(activityId, videoId, resourceUrl)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete resource: ${response.message()}")
        }
    }

    // Meeting Management - uses YOUR existing endpoint
    suspend fun updateMeeting(activityId: String, request: UpdateMeetingRequest) {
        // PUT /api/tutor/activities/{activityId}/meeting
        val response = apiService.updateActivityMeeting(activityId, request)
        if (!response.isSuccessful) {
            throw Exception("Failed to update meeting: ${response.message()}")
        }
    }

    suspend fun getMeetingInfo(activityId: String): Activity.VideoContent? {
        // GET /api/tutor/activities/{activityId}/meeting (if you have this endpoint)
        val response = apiService.getActivityMeeting(activityId)
        if (response.isSuccessful) {
            return response.body()
        }
        return null
    }
}