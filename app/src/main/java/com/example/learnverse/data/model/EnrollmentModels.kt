package com.example.learnverse.data.model

// Request body for POST /api/enrollments/enroll
data class EnrollmentRequest(
    val activityId: String
)

// The detailed Enrollment object returned by the server
data class Enrollment(
    val id: String,
    val userId: String,
    val activityId: String,
    val activityTitle: String,
    val status: String,
    val progressPercentage: Double,
    val totalSessions: Int
    // Add other fields if you need them
)

// The response from GET /api/enrollments/my-enrollments
data class MyEnrollmentsResponse(
    val success: Boolean,
    val enrollments: List<Enrollment>
)

// The request body for POST /api/activities/by-ids
data class ActivitiesByIdsRequest(
    val activityIds: List<String>
)