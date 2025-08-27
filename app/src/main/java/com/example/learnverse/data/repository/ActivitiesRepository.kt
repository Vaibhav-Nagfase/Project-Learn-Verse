package com.example.learnverse.data.repository

import com.example.learnverse.data.model.Activity
import com.example.learnverse.data.remote.ApiService

class ActivitiesRepository(private val api: ApiService) {

    suspend fun getMyFeed(token: String): List<Activity> {
        val response = api.getMyFeed("Bearer $token")
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        }
        throw Exception("Failed to fetch feed: ${response.message()}")
    }

    suspend fun searchActivities(token: String, query: String): List<Activity> {
        val response = api.searchActivities("Bearer $token", query)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        }
        throw Exception("Search failed: ${response.message()}")
    }
}