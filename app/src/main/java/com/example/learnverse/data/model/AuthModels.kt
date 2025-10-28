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

// NEW: Represents the response from GET /api/user/interests
data class UserInterestsResponse(
    val interests: List<String>,
    val interestCount: Int
)

// NEW: Represents the request body for POST /api/user/interests/remove
data class InterestsUpdateRequest(
    val interests: List<String>
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val role: String,
    val userId: String
)

