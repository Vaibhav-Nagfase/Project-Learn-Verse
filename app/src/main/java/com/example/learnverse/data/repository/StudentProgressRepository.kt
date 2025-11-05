package com.example.learnverse.data.repository

import com.example.learnverse.data.model.UserProgressResponse
import com.example.learnverse.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudentProgressRepository(
    private val apiService: ApiService
) {

    suspend fun getMyCourses(): List<UserProgressResponse> {
        return withContext(Dispatchers.IO) {
            val response = apiService.getMyCourses()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception("Failed to fetch courses: ${response.message()}")
            }
        }
    }

    suspend fun getCourseProgress(activityId: String): UserProgressResponse {
        return withContext(Dispatchers.IO) {
            val response = apiService.getCourseProgress(activityId)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception("Failed to fetch progress: ${response.message()}")
            }
        }
    }
}
