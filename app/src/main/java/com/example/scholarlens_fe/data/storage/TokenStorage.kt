package com.example.scholarlens_fe.data.storage

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure token storage using SharedPreferences
 * For production, consider using EncryptedSharedPreferences or Android Keystore
 */
@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_ID_TOKEN = "id_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
    }

    /**
     * Save ID token
     */
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_ID_TOKEN, token).apply()
    }

    /**
     * Get stored ID token
     */
    fun getToken(): String? {
        return prefs.getString(KEY_ID_TOKEN, null)
    }

    /**
     * Save user info
     */
    fun saveUserInfo(userId: String, email: String?, name: String?) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    /**
     * Get user ID
     */
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    /**
     * Get user email
     */
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    /**
     * Get user name
     */
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    /**
     * Clear all stored data (logout)
     */
    fun clear() {
        prefs.edit().clear().apply()
    }

    /**
     * Check if token exists
     */
    fun hasToken(): Boolean {
        return prefs.contains(KEY_ID_TOKEN)
    }
}
