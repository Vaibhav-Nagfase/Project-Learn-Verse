package com.example.learnverse.data.remote

import android.content.Context
import com.example.learnverse.utils.UserPreferences // Import your UserPreferences
import com.example.learnverse.utils.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

// It now only needs the application context
class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()

        // --- ADD THIS CHECK ---
        // Check if our special "skip" header is present
        if (originalRequest.header("X-Skip-Interceptor-Auth") != null) {
            // If it is, this is our chat request.
            // The token is already added manually.
            // Just remove our special header and proceed.
            val newRequest = originalRequest.newBuilder()
                .removeHeader("X-Skip-Interceptor-Auth")
                .build()
            return chain.proceed(newRequest)
        }

        // Fetch the token directly from UserPreferences
        val token = runBlocking {
            UserPreferences.getToken(context).first()
        }

        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}