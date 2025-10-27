package com.example.learnverse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.learnverse.data.repository.AuthRepository
import com.example.learnverse.data.repository.CommunityRepository

class CommunityViewModelFactory(
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
            return CommunityViewModel(communityRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for CommunityViewModelFactory")
    }
}