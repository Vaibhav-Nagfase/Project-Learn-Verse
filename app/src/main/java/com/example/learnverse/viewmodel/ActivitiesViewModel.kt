package com.example.learnverse.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.ActivityResponse
import com.example.learnverse.data.repository.ActivitiesRepository
import com.example.learnverse.utils.UserPreferences
import kotlinx.coroutines.launch

class ActivitiesViewModel(
    private val repository: ActivitiesRepository,
    private val context: Context
) : ViewModel() {

    var activities by mutableStateOf<List<ActivityResponse>>(emptyList())
        private set

    fun fetchActivities() {
        viewModelScope.launch {
            UserPreferences.getToken(context).collect { token ->
                if (!token.isNullOrEmpty()) {
                    try {
                        activities = repository.getActivities(token)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
