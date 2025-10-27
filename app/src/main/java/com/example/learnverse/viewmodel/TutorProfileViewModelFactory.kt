package com.example.learnverse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.learnverse.data.repository.AuthRepository
import com.example.learnverse.data.repository.CommunityRepository

class TutorProfileViewModelFactory(
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TutorProfileViewModel::class.java)) {
            return TutorProfileViewModel(communityRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for TutorProfile")
    }
}