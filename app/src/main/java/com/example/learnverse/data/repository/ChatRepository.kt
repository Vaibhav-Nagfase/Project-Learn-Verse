package com.example.learnverse.data.repository

import com.example.learnverse.data.remote.ApiService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull // Import correct extension
import okhttp3.RequestBody.Companion.toRequestBody // Import correct extension
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

class ChatRepository(
    private val api: ApiService,
    private val okHttpClient: OkHttpClient
) {

    // --- UPDATED FUNCTION SIGNATURE ---
    fun askQuestionStream(question: String, token: String): Flow<String> = callbackFlow {

        val requestBuilder = Request.Builder()
            .url("https://learnverse-sy8l.onrender.com/api/assistant/chat")
            .header("Content-Type", "application/json")
            // --- MANUALLY ADD THE TOKEN ---
            .header("Authorization", "Bearer $token")
            // The new "skip" header
            .header("X-Skip-Interceptor-Auth", "true")
            .post(
                "{\"message\":\"$question\"}".toRequestBody("application/json".toMediaTypeOrNull())
            )

        val request = requestBuilder.build()

        val listener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                trySend(data)
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                // If it fails with 401 now, the token is invalid
                if (response?.code == 401) {
                    close(RuntimeException("Invalid token. Please log out and log in again."))
                } else {
                    close(t ?: RuntimeException("Unknown streaming error"))
                }
            }
        }

        val eventSource = EventSources.createFactory(okHttpClient).newEventSource(request, listener)

        awaitClose {
            eventSource.cancel()
        }
    }
}