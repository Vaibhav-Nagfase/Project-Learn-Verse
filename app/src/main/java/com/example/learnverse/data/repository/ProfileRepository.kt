package com.example.learnverse.data.repository

import com.example.learnverse.data.model.UserProfile
import com.example.learnverse.data.model.UserProfileRequest
import com.example.learnverse.data.remote.ApiService

class ProfileRepository(private val api: ApiService) {

    /**
     * Fetches the current user's profile.
     * Returns the UserProfile on success, or null if not found (e.g., 404 error).
     */
    suspend fun getProfile(): UserProfile? {
        val response = api.getProfile()
        if (response.isSuccessful) {
            return response.body()
        } else if (response.code() == 404) {
            // A 404 here means the user hasn't set up their profile yet, which is not an error.
            return null
        }
        throw Exception("Failed to get profile: ${response.message()}")
    }

    /**
     * Creates a new user profile.
     */
    suspend fun setupProfile(profile: UserProfileRequest): UserProfile {
        val response = api.setupProfile(profile)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to set up profile: ${response.message()}")
    }

    /**
     * Updates an existing user profile.
     */
    suspend fun updateProfile(profile: UserProfileRequest): UserProfile {
        val response = api.updateProfile(profile)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to update profile: ${response.message()}")
    }
}
