package com.example.learnverse.data.repository

import com.example.learnverse.data.model.*
import com.example.learnverse.data.remote.ApiService

class TutorCourseRepository(
    private val apiService: ApiService
) {

    // Get complete course management data
    suspend fun getCourseManagementData(activityId: String): TutorCourseDetail {
        val response = apiService.getTutorCourseDetail(activityId)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to load course data: ${response.message()}")
    }

    // Get enrolled students
    suspend fun getEnrolledStudents(activityId: String): List<EnrolledStudent> {
        val response = apiService.getEnrolledStudents(activityId)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to load students: ${response.message()}")
    }

    // Get course stats
    suspend fun getCourseStats(activityId: String): CourseStats {
        val response = apiService.getCourseStats(activityId)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to load stats: ${response.message()}")
    }

    suspend fun deleteCourse(activityId: String) {
        val response = apiService.deleteActivity(activityId)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete course: ${response.message()}")
        }
    }

    // Update course status
    suspend fun updateCourseStatus(activityId: String, isActive: Boolean?, isPublic: Boolean?) {
        val status = mutableMapOf<String, Boolean>()
        isActive?.let { status["isActive"] = it }
        isPublic?.let { status["isPublic"] = it }

        val response = apiService.updateCourseStatus(activityId, status)
        if (!response.isSuccessful) {
            throw Exception("Failed to update status: ${response.message()}")
        }
    }
}
