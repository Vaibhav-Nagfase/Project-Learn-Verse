package com.example.learnverse.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.learnverse.data.remote.ApiService
import com.example.learnverse.data.repository.TutorRepository

/**
 * A factory class for creating instances of TutorViewModel.
 * This is necessary because TutorViewModel has a constructor that requires multiple dependencies.
 */
class TutorViewModelFactory(
    private val repository: TutorRepository,
    private val apiService: ApiService,
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TutorViewModel::class.java)) {
            return TutorViewModel(repository, apiService, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
