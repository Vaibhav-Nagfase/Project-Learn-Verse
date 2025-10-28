package com.example.learnverse.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension for DataStore
val Context.dataStore by preferencesDataStore("user_prefs")

object UserPreferences {
    private val TOKEN_KEY = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token") // <-- ADD THIS

    private val KEY_INTERESTS_SKIPPED = booleanPreferencesKey("interests_skipped")

    suspend fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    fun getToken(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }
    }

    // --- ADD THIS FUNCTION ---
    fun getRefreshToken(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[REFRESH_TOKEN_KEY]
        }
    }

    suspend fun clearToken(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(REFRESH_TOKEN_KEY)
        }
    }

    // FUNCTION to save the flag
    suspend fun saveInterestsSkippedFlag(context: Context, skipped: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_INTERESTS_SKIPPED] = skipped
        }
    }

    // FUNCTION to read the flag
    fun getInterestsSkippedFlag(context: Context): Flow<Boolean> {
        // Default to 'false' if no value is set
        return context.dataStore.data.map { prefs ->
            prefs[KEY_INTERESTS_SKIPPED] ?: false
        }
    }

}