package com.example.learnverse.data.repository

import android.util.Log
import com.example.learnverse.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ChatRepository(
    private val api: ApiService,
    private val okHttpClient: OkHttpClient
) {

    companion object {
        private const val TAG = "ChatRepository"
        // ✅ Change to your computer's IP or keep 127.0.0.1 for emulator
        private const val BASE_URL = "http://127.0.0.1:8080"
        // For real device, use: "http://192.168.1.XXX:8080" (your computer's local IP)
    }

    fun askQuestionStream(question: String, token: String): Flow<String> = flow {
        val requestBody = JSONObject().apply {
            put("message", question)
        }.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("$BASE_URL/api/assistant/chat")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")
            .addHeader("User-Agent", "LearnVerseApp/1.0 (Android)")
            .addHeader("Origin", BASE_URL)
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .post(requestBody)
            .build()

        Log.d(TAG, "🚀 Request: $question")

        // ✅ REMOVE withContext - flow already has flowOn()
        try {
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.e(TAG, "❌ HTTP ${response.code}: $errorBody")

                when (response.code) {
                    400 -> throw Exception("Bad request. Check your profile completion.")
                    401 -> throw Exception("Session expired. Please login again.")
                    403 -> throw Exception("Please complete your profile first.")
                    else -> throw Exception("Server error: ${response.code}")
                }
            }

            val reader = response.body?.byteStream()?.bufferedReader()
                ?: throw Exception("No response from server")

            Log.d(TAG, "✅ Connected, streaming...")

            reader.use { bufferedReader ->
                var line: String?

                while (bufferedReader.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue

                    // SSE format: "data: content"
                    if (currentLine.startsWith("data: ")) {
                        val content = currentLine.substring(6).trim()

                        if (content.isEmpty() || content == "[DONE]") {
                            continue
                        }

                        Log.d(TAG, "📥 Chunk: ${content.take(30)}...")
                        emit(content) // ✅ Emit directly without context switching
                    }
                }

                Log.d(TAG, "✅ Complete")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}", e)
            throw Exception("Streaming failed: ${e.message}")
        }
    }.flowOn(Dispatchers.IO) // ✅ Single flowOn at the end

}