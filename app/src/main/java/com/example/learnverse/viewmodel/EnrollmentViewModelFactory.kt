package com.example.learnverse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.learnverse.data.repository.EnrollmentRepository

class EnrollmentViewModelFactory(
    private val repository: EnrollmentRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EnrollmentViewModel::class.java)) {
            return EnrollmentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}