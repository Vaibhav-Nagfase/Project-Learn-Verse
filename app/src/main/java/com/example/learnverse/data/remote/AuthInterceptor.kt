package com.example.learnverse.data.remote

import android.content.Context
import com.example.learnverse.utils.UserPreferences // Import your UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

// It now only needs the application context
class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Fetch the token directly from UserPreferences
        val token = runBlocking {
            UserPreferences.getToken(context).first()
        }

        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}