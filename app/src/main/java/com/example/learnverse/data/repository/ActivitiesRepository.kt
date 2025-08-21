package com.example.learnverse.data.repository

import com.example.learnverse.data.model.ActivityResponse
import com.example.learnverse.data.remote.ApiService

class ActivitiesRepository(private val api: ApiService) {

    suspend fun getActivities(token: String): List<ActivityResponse> {
        return api.getActivities("Bearer $token")
    }
}
