package com.example.learnverse.data.model

import com.example.learnverse.data.model.profile.UserProfile

data class ProfileResponse(
    val success: Boolean,
    val profile: UserProfile?,
    val message: String?
)