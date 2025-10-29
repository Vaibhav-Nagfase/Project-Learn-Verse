package com.example.learnverse.data.model

import com.google.gson.annotations.SerializedName

// Represents the response from GET /api/tutor-verification/status/{email}
data class TutorVerificationStatus(
    val email: String,
    val status: String, // e.g., "PENDING"
    val statusDescription: String,
    val rejectionReason: String,
    val submittedAt: String,
    val canCreateActivities: Boolean
)
