package com.example.learnverse.data.model.profile

data class ProfileResponse(
    val success: Boolean,
    val profile: UserProfile?,
    val message: String?
)