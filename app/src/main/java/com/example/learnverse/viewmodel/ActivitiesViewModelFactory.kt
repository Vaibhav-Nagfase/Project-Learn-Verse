package com.example.learnverse.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.learnverse.data.repository.ActivitiesRepository

class ActivitiesViewModelFactory(
    private val repository: ActivitiesRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivitiesViewModel::class.java)) {
            return ActivitiesViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}