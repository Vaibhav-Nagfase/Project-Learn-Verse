package com.example.learnverse.data.repository

import com.example.learnverse.data.model.Activity
import com.example.learnverse.data.model.ActivityFilter
import com.example.learnverse.data.model.NaturalSearchRequest
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

    suspend fun filterActivities(token: String, filter: ActivityFilter): List<Activity> {
        val filterMap = mutableMapOf<String, String>()

        // This code checks each filter and adds it to the map if it has a value
        filter.subjects?.let { if (it.isNotEmpty()) filterMap["subjects"] = it.joinToString(",") }
        filter.activityTypes?.let { if (it.isNotEmpty()) filterMap["activityTypes"] = it.joinToString(",") }
        filter.modes?.let { if (it.isNotEmpty()) filterMap["modes"] = it.joinToString(",") }
        filter.difficulties?.let { if (it.isNotEmpty()) filterMap["difficulties"] = it.joinToString(",") }
        filter.cities?.let { if (it.isNotEmpty()) filterMap["cities"] = it.joinToString(",") }
        filter.minPrice?.let { filterMap["minPrice"] = it.toString() }
        filter.maxPrice?.let { filterMap["maxPrice"] = it.toString() }
        filter.demoAvailable?.let { filterMap["demoAvailable"] = it.toString() }
        filter.sortBy?.let { filterMap["sortBy"] = it }
        filter.sortDirection?.let { filterMap["sortDirection"] = it }
        filter.searchQuery?.let { if (it.isNotBlank()) filterMap["searchQuery"] = it }

        // Make the API call with the constructed map
        val response = api.filterActivities("Bearer $token", filterMap)

        if (response.isSuccessful && response.body() != null) {
            // If successful, return the 'content' list from inside the paged response
            return response.body()!!.content
        }

        throw Exception("Failed to apply filters: ${response.message()}")
    }

    suspend fun getNearbyActivities(token: String, latitude: Double, longitude: Double): List<Activity> {
        val response = api.getNearbyActivities("Bearer $token", latitude, longitude)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.content
        }
        throw Exception("Failed to get nearby activities: ${response.message()}")
    }

    suspend fun naturalSearch(token: String, request: NaturalSearchRequest): List<Activity> {
        val response = api.naturalSearch("Bearer $token", request)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.content
        }
        throw Exception("Natural search failed: ${response.message()}")
    }

}