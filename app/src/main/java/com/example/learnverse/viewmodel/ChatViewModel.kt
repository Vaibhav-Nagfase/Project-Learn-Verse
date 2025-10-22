package com.example.learnverse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.repository.AuthRepository // <-- ADD IMPORT
import com.example.learnverse.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull // <-- ADD IMPORT
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val isTyping: Boolean = false
)

// --- UPDATED CONSTRUCTOR ---
class ChatViewModel(
    private val repository: ChatRepository,
    private val authRepository: AuthRepository // <-- ADD THIS
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        _messages.value += ChatMessage(text, isFromUser = true)
        _messages.value += ChatMessage(text = "", isFromUser = false, isTyping = true)

        viewModelScope.launch {
            // --- GET THE TOKEN FIRST ---
            val token = authRepository.getToken().firstOrNull()
            if (token.isNullOrBlank()) {
                // Handle error: User is not logged in
                _messages.value = _messages.value.dropLast(1) + ChatMessage(
                    text = "Error: You are not logged in. Please restart the app.",
                    isFromUser = false,
                    isTyping = false
                )
                return@launch
            }

            // --- PASS THE TOKEN TO THE REPOSITORY ---
            repository.askQuestionStream(text, token)
                .onCompletion { throwable ->
                    if (throwable == null) {
                        _messages.value.lastOrNull()?.let { lastMessage ->
                            val finalMessage = lastMessage.copy(isTyping = false)
                            _messages.value = _messages.value.dropLast(1) + finalMessage
                        }
                    }
                }
                .catch { e ->
                    _messages.value.lastOrNull()?.let { lastMessage ->
                        val errorMessage = lastMessage.copy(
                            text = "Sorry, something went wrong: ${e.message}",
                            isTyping = false
                        )
                        _messages.value = _messages.value.dropLast(1) + errorMessage
                    }
                }
                .collect { chunk ->
                    _messages.value.lastOrNull()?.let { lastMessage ->
                        val updatedMessage = lastMessage.copy(
                            text = lastMessage.text + chunk
                        )
                        _messages.value = _messages.value.dropLast(1) + updatedMessage
                    }
                }
        }
    }
}