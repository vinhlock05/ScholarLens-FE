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
        private const val KEY_USER_DISPLAY_NAME = "user_display_name"
        private const val KEY_USER_DESIRED_COUNTRIES = "user_desired_countries"
        private const val KEY_USER_DEGREE = "user_degree"
        private const val KEY_USER_GPA = "user_gpa"
        private const val KEY_USER_FIELD_OF_STUDY = "user_field_of_study"
        private const val KEY_USER_BIRTH_DATE = "user_birth_date"
        private const val KEY_USER_UNIVERSITY = "user_university"
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

    /**
     * Save full user profile data
     */
    fun saveUserProfile(profile: Map<String, Any?>) {
        val editor = prefs.edit()
        
        profile["uid"]?.toString()?.let { editor.putString(KEY_USER_ID, it) }
        profile["email"]?.toString()?.let { editor.putString(KEY_USER_EMAIL, it) }
        profile["display_name"]?.toString()?.let { editor.putString(KEY_USER_DISPLAY_NAME, it) }
        profile["name"]?.toString()?.let { editor.putString(KEY_USER_NAME, it) }
        profile["degree"]?.toString()?.let { editor.putString(KEY_USER_DEGREE, it) }
        profile["gpa_range_4"]?.toString()?.let { editor.putString(KEY_USER_GPA, it) }
        profile["field_of_study"]?.toString()?.let { editor.putString(KEY_USER_FIELD_OF_STUDY, it) }
        profile["birth_date"]?.toString()?.let { editor.putString(KEY_USER_BIRTH_DATE, it) }
        profile["university"]?.toString()?.let { editor.putString(KEY_USER_UNIVERSITY, it) }
        
        // Handle desired_countries as List
        @Suppress("UNCHECKED_CAST")
        (profile["desired_countries"] as? List<*>)?.let { countries ->
            val countriesString = countries.joinToString(",")
            editor.putString(KEY_USER_DESIRED_COUNTRIES, countriesString)
        }
        
        editor.apply()
    }

    /**
     * Get user display name from storage
     */
    fun getUserDisplayName(): String? {
        return prefs.getString(KEY_USER_DISPLAY_NAME, null)
    }

    /**
     * Get user desired countries from storage
     */
    fun getUserDesiredCountries(): List<String> {
        val countriesString = prefs.getString(KEY_USER_DESIRED_COUNTRIES, null)
        return if (countriesString.isNullOrBlank()) {
            emptyList()
        } else {
            countriesString.split(",").map { it.trim() }.filter { it.isNotBlank() }
        }
    }

    /**
     * Get user degree from storage
     */
    fun getUserDegree(): String? {
        return prefs.getString(KEY_USER_DEGREE, null)
    }

    /**
     * Get user GPA from storage
     */
    fun getUserGpa(): String? {
        return prefs.getString(KEY_USER_GPA, null)
    }

    /**
     * Get user field of study from storage
     */
    fun getUserFieldOfStudy(): String? {
        return prefs.getString(KEY_USER_FIELD_OF_STUDY, null)
    }

    /**
     * Get user birth date from storage
     */
    fun getUserBirthDate(): String? {
        return prefs.getString(KEY_USER_BIRTH_DATE, null)
    }

    /**
     * Get user university from storage
     */
    fun getUserUniversity(): String? {
        return prefs.getString(KEY_USER_UNIVERSITY, null)
    }
}
