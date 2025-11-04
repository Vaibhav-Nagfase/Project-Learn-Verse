package com.example.learnverse.data.repository

import com.example.learnverse.data.model.TutorDashboardStats
import com.example.learnverse.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TutorDashboardRepository(
    private val apiService: ApiService
) {

    suspend fun getDashboardStats(): TutorDashboardStats {
        return withContext(Dispatchers.IO) {
            val response = apiService.getTutorDashboardStats()

            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception("Failed to fetch dashboard stats: ${response.message()}")
            }
        }
    }
}
