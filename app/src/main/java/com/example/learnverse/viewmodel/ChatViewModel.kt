package com.example.learnverse.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.repository.AuthRepository
import com.example.learnverse.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val isTyping: Boolean = false,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatViewModel(
    private val repository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
    }

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Add welcome message
        _messages.value = listOf(
            ChatMessage(
                text = "Hi! 👋 I'm LearnVerse AI. Ask me anything about courses, learning paths, or study tips!",
                isFromUser = false
            )
        )
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        _error.value = null
        _messages.value += ChatMessage(text, isFromUser = true)
        _messages.value += ChatMessage(text = "", isFromUser = false, isTyping = true)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val token = authRepository.getToken().firstOrNull()

                if (token.isNullOrBlank()) {
                    showErrorMessage("Please login to use the assistant")
                    return@launch
                }

                var hasReceivedData = false

                repository.askQuestionStream(text, token)
                    .onCompletion { error ->
                        _isLoading.value = false

                        if (error == null && hasReceivedData) {
                            // Successfully completed with data
                            _messages.value.lastOrNull()?.let { lastMessage ->
                                if (lastMessage.isTyping) {
                                    val finalMessage = lastMessage.copy(isTyping = false)
                                    _messages.value = _messages.value.dropLast(1) + finalMessage
                                }
                            }
                            Log.d(TAG, "✅ Stream completed successfully")
                        } else if (error != null) {
                            Log.e(TAG, "❌ Stream error: ${error.message}")
                        }
                    }
                    .catch { error ->
                        // Only show real errors with messages
                        if (error.message?.isNotBlank() == true && !hasReceivedData) {
                            Log.e(TAG, "❌ Catch error: ${error.message}", error)
                            showErrorMessage(error.message ?: "Something went wrong")
                        }
                    }
                    .collect { chunk ->
                        hasReceivedData = true
                        _messages.value.lastOrNull()?.let { lastMessage ->
                            val updatedMessage = lastMessage.copy(
                                text = lastMessage.text + chunk,
                                isTyping = false
                            )
                            _messages.value = _messages.value.dropLast(1) + updatedMessage
                        }
                    }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Send error: ${e.message}", e)
                showErrorMessage("Failed to send message: ${e.message}")
            }
        }
    }

    private fun showErrorMessage(errorText: String) {
        _isLoading.value = false
        _error.value = errorText

        // Remove typing indicator
        val messagesWithoutTyping = _messages.value.filter { !it.isTyping }

        // Add error message
        _messages.value = messagesWithoutTyping + ChatMessage(
            text = errorText,
            isFromUser = false,
            isError = true
        )
    }

    fun clearError() {
        _error.value = null
    }

    fun clearChat() {
        _messages.value = listOf(
            ChatMessage(
                text = "Hi! 👋 I'm LearnVerse AI. Ask me anything!",
                isFromUser = false
            )
        )
    }
}