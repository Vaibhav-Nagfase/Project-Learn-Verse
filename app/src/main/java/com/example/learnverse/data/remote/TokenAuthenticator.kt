package com.example.learnverse.data.remote

import android.content.Context
import com.example.learnverse.data.model.AuthResponse
import com.example.learnverse.data.model.RefreshTokenRequest
import com.example.learnverse.utils.UserPreferences
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenAuthenticator(
    private val context: Context,
    private val apiClient: ApiClient // Pass ApiClient to get Retrofit base
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // 1. Get the current refresh token from DataStore
        // We must use runBlocking here as this is not a suspend function
        val refreshToken = runBlocking {
            UserPreferences.getRefreshToken(context).firstOrNull()
        }

        if (refreshToken.isNullOrBlank()) {
            // No refresh token, we can't recover. Log out.
            // You might want to trigger a global logout here
            return null // Authentication fails
        }

        // 2. Make a synchronous call to refresh the token
        // We use the apiClient's Retrofit builder but with a
        // *different* OkHttpClient that does NOT have this authenticator,
        // to avoid an infinite loop.
        val newRetrofit = Retrofit.Builder()
            .baseUrl(apiClient.retrofit.baseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build()) // <-- Simple client
            .build()

        val apiService = newRetrofit.create(ApiService::class.java)

        return try {
            val refreshResponse = apiService.refreshToken(RefreshTokenRequest(refreshToken)).execute()

            if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                val authResponse = refreshResponse.body()!!

                // 3. Save the new tokens
                runBlocking {
                    UserPreferences.saveTokens(context, authResponse.accessToken, authResponse.refreshToken)
                }

                // 4. Retry the original request with the new access token
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${authResponse.accessToken}")
                    .build()
            } else {
                // Refresh token is invalid or expired. Log out.
                // You might want to trigger a global logout here
                null // Authentication fails
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null // Failed to refresh
        }
    }
}