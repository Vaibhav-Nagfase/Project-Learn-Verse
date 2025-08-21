package com.example.learnverse.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.LoginRequest
import com.example.learnverse.data.model.RegisterRequest
import com.example.learnverse.data.repository.AuthRepository
import com.example.learnverse.utils.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    var token = mutableStateOf<String?>(null)
        private set

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set


    // This block runs when the ViewModel is first created
    init {
        viewModelScope.launch {
            // Read the token from the repository and update the state
            token.value = repository.getToken().first()
        }
    }


    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                errorMessage.value = null

                val response = repository.registerUser(RegisterRequest(name, email, password))
                token.value = response.access_token

                onSuccess()
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Registration failed"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                errorMessage.value = null

                val response = repository.login(LoginRequest(email, password))
                token.value = response.access_token

                onSuccess()
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Login failed"
            } finally {
                isLoading.value = false
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            // Call the repository to clear the token from DataStore
            repository.logout()
            // Clear the token in the ViewModel's state
            token.value = null
        }
    }

}
