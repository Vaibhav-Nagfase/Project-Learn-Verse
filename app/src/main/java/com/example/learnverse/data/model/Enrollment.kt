package com.example.learnverse.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Enrollment(
    val id: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val userPhone: String,
    val activityId: String,
    val activityTitle: String,
    val tutorId: String,
    val tutorName: String,
    val educationalBackground: String?,
    val reasonForEnrollment: String?,
    val status: EnrollmentStatus,
    val enrolledAt: Date,
    val completedAt: Date?,
    val droppedAt: Date?,
    val pausedAt: Date?,
    val progress: Progress,
    val orderId: String?,
    val amountPaid: Double?
) {
    data class Progress(
        val completedSessions: Int,
        val totalSessions: Int,
        val completionPercentage: Double,
        val lastAccessedAt: Date
    )

    enum class EnrollmentStatus {
        ENROLLED,
        IN_PROGRESS,
        COMPLETED,
        DROPPED,
        PAUSED,
        SUSPENDED
    }

}

data class EnrollmentRequest(
    val activityId: String,
    val studentName: String,
    val studentEmail: String,
    val studentPhone: String,
    val educationalBackground: String? = null,
    val reasonForEnrollment: String? = null
)