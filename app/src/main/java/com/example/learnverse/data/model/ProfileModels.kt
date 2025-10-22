package com.example.learnverse.data.model

/**
 * Data class for the JSON body when setting up or updating a user profile.
 * Used for POST /api/user/profile/setup and PUT /api/user/profile/update_profile.
 */
data class UserProfileRequest(
    val firstName: String,
    val lastName: String,
    val location: String,
    val currentEducationLevel: String,
    val currentRole: String,
    val interests: List<String>,
    val careerGoal: String,
    val targetSkills: List<String>,
    val currentFocusArea: String,
    val communicationStyle: String,
    val wantsStepByStepGuidance: Boolean
)

/**
 * Data class representing the user profile as received from the server.
 * Used for the response of GET /api/user/profile/get_profile.
 */

data class UserProfile(
    val firstName: String?,
    val lastName: String?,
    val location: String?,
    val currentEducationLevel: String?,
    val currentRole: String?,
    val interests: List<String>?,
    val careerGoal: String?,
    val targetSkills: List<String>?,
    val currentFocusArea: String?,
    val communicationStyle: String?,
    val wantsStepByStepGuidance: Boolean?
    // The server might add other fields like userId, which can be added here if needed.
)
