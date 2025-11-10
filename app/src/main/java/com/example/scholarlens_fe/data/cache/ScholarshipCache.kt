package com.example.scholarlens_fe.data.cache

import android.content.Context
import android.content.SharedPreferences
import com.example.scholarlens_fe.domain.model.Scholarship
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache manager for scholarship data
 * Uses SharedPreferences to store cached data for offline access
 */
@Singleton
class ScholarshipCache @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("scholarship_cache", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_ALL_SCHOLARSHIPS = "all_scholarships"
        private const val KEY_CACHE_TIMESTAMP = "cache_timestamp"
        private const val CACHE_EXPIRY_HOURS = 24L // Cache expires after 24 hours
    }

    /**
     * Save scholarships to cache
     */
    fun saveScholarships(scholarships: List<Scholarship>) {
        val json = gson.toJson(scholarships)
        prefs.edit()
            .putString(KEY_ALL_SCHOLARSHIPS, json)
            .putLong(KEY_CACHE_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    /**
     * Get cached scholarships
     */
    fun getCachedScholarships(): List<Scholarship>? {
        val json = prefs.getString(KEY_ALL_SCHOLARSHIPS, null) ?: return null
        val type = object : TypeToken<List<Scholarship>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if cache is valid (not expired)
     */
    fun isCacheValid(): Boolean {
        val timestamp = prefs.getLong(KEY_CACHE_TIMESTAMP, 0)
        if (timestamp == 0L) return false
        
        val cacheAge = System.currentTimeMillis() - timestamp
        val expiryTime = CACHE_EXPIRY_HOURS * 60 * 60 * 1000 // Convert to milliseconds
        return cacheAge < expiryTime
    }

    /**
     * Clear cache
     */
    fun clearCache() {
        prefs.edit().clear().apply()
    }

    /**
     * Check if cache exists
     */
    fun hasCache(): Boolean {
        return prefs.contains(KEY_ALL_SCHOLARSHIPS)
    }
}

