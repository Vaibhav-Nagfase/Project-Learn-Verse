package com.example.learnverse.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(context: Context) {

    val okHttpClient: OkHttpClient
    val retrofit: Retrofit

    // ✅ ADD THIS: Create the ApiService
    val apiService: ApiService

    companion object {
        @Volatile
        private var INSTANCE: ApiClient? = null
        private const val BASE_URL = "https://learnverse-sy8l.onrender.com/"

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

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .authenticator(authenticator)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // ✅ ADD THIS: Initialize ApiService from Retrofit
        apiService = retrofit.create(ApiService::class.java)
    }
}