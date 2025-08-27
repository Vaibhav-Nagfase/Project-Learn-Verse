package com.example.learnverse.data.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
)

data class LoginRequest(
    val email: String,
    val password: String
)

// NEW request body for adding/editing interests
data class InterestsRequest(
    val interests: List<String>
)

data class AuthResponse(
    val accessToken: String,
    val interests: List<String>? // Add this field. It will be null or empty for new users.
)

