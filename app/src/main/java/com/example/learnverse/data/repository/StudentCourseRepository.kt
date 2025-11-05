package com.example.learnverse.data.repository

import com.example.learnverse.data.model.StudentCourseViewResponse
import com.example.learnverse.data.model.UpdateVideoProgressRequest
import com.example.learnverse.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudentCourseRepository(
    private val apiService: ApiService
) {

    suspend fun getCourseView(activityId: String): StudentCourseViewResponse {
        return withContext(Dispatchers.IO) {
            val response = apiService.getStudentCourseView(activityId)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception("Failed to fetch course: ${response.message()}")
            }
        }
    }

    suspend fun updateVideoProgress(
        activityId: String,
        request: UpdateVideoProgressRequest
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val response = apiService.updateVideoProgress(activityId, request)
            response.isSuccessful
        }
    }

    suspend fun markVideoComplete(
        activityId: String,
        videoId: String,
        videoTitle: String,
        totalSeconds: Long
    ): Boolean {
        val request = UpdateVideoProgressRequest(
            videoId = videoId,
            videoTitle = videoTitle,
            watchedSeconds = totalSeconds,
            totalSeconds = totalSeconds,
            completed = true
        )
        return updateVideoProgress(activityId, request)
    }

    suspend fun markResourceDownloaded(
        activityId: String,
        resourceId: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.markResourceCompleted(activityId, resourceId)
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }
}
