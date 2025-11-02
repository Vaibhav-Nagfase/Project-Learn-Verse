package com.example.learnverse.data.model

// 1. For the GET /api/tutor-verification/my-profile response
data class TutorMyProfileResponse(
    val id: String,
    val email: String?, // ✅ Change to nullable
    val fullName: String?, // ✅ Change to nullable
    val phone: String?, // ✅ Change to nullable
    val bio: String?,
    val qualifications: List<String>?,
    val experience: String?,
    val specializations: List<String>?,
    val profilePicture: ProfilePicture?,
    val status: String?, // ✅ Change to nullable
    val statusDescription: String?, // ✅ Change to nullable
    val createdAt: String?, // ✅ Change to nullable
    val updatedAt: String? // ✅ Change to nullable
) {
    data class ProfilePicture(
        val originalName: String,
        val url: String
    )
}

// 2. For the PUT /api/tutor-verification/update-profile request
data class TutorProfileUpdateRequest(
    val bio: String?,
    val qualifications: List<String>?,
    val experience: String?,
    val specializations: List<String>?
)