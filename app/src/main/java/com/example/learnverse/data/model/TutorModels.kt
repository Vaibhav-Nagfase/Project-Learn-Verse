package com.example.learnverse.data.model

data class TutorVerificationStatus(
    val email: String,
    val status: String,
    val statusDescription: String,
    val submittedAt: String,
    val canCreateActivities: Boolean,
    val rejectionReason: String?
)
