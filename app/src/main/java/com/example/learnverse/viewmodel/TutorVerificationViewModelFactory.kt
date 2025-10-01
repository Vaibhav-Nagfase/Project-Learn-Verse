package com.example.learnverse.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.learnverse.data.repository.AuthRepository
import com.example.learnverse.data.repository.TutorRepository

class TutorVerificationViewModelFactory(
    private val application: Application,
    private val tutorRepository: TutorRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TutorVerificationViewModel::class.java)) {
            return TutorVerificationViewModel(application, tutorRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}