package com.example.learnverse.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(context: Context) {

    // --- CHANGE 1: Made okHttpClient public ---
    val okHttpClient: OkHttpClient
    val retrofit: Retrofit // Also expose Retrofit for easier access in MainActivity

    companion object {
        @Volatile
        private var INSTANCE: ApiClient? = null // Store the ApiClient instance
        private const val BASE_URL = "https://learnverse-sy8l.onrender.com/"

        // Updated getInstance to return the ApiClient itself
        fun getInstance(context: Context): ApiClient {
            return INSTANCE ?: synchronized(this) {
                ApiClient(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        val authInterceptor = AuthInterceptor(context)

        val authenticator = TokenAuthenticator(context, this)

        // --- CHANGE 2: Increased connectTimeout to 60 seconds ---
        okHttpClient = OkHttpClient.Builder() // Assign to the public property
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .authenticator(authenticator)
            .connectTimeout(60, TimeUnit.SECONDS) // Increased from 30
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Use the same client instance
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

