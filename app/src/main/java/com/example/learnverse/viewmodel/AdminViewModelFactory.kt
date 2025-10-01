package com.example.learnverse.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.learnverse.data.repository.AdminRepository
import com.example.learnverse.data.repository.AuthRepository

class AdminViewModelFactory(
    private val application: Application,
    private val adminRepository: AdminRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            return AdminViewModel(application, adminRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}