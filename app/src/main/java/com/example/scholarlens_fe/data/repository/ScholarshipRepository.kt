package com.example.scholarlens_fe.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.scholarlens_fe.data.api.ScholarshipApiService
import com.example.scholarlens_fe.data.cache.ScholarshipCache
import com.example.scholarlens_fe.data.mapper.ScholarshipMapper
import com.example.scholarlens_fe.domain.model.FilterItem
import com.example.scholarlens_fe.domain.model.Scholarship
import com.example.scholarlens_fe.domain.model.ScholarshipFilter
import com.example.scholarlens_fe.domain.model.SearchResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for scholarship data operations
 * Handles backend API calls and data mapping
 */
@Singleton
class ScholarshipRepository @Inject constructor(
    private val apiService: ScholarshipApiService,
    private val firebaseAuth: FirebaseAuth,
    private val cache: ScholarshipCache,
    @ApplicationContext private val context: Context
) {

    /**
     * Get Firebase authentication token
     */
    private suspend fun getAuthToken(): String {
        val user = firebaseAuth.currentUser
        return if (user != null) {
            try {
                val tokenResult: GetTokenResult = user.getIdToken(false).await()
                tokenResult.token ?: ""
            } catch (e: Exception) {
                ""
            }
        } else {
            ""
        }
    }

    /**
     * Check if device is online
     */
    private fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Search scholarships by keyword
     * GET /api/v1/es/search
     * Falls back to cache if offline
     */
    suspend fun searchScholarships(
        filter: ScholarshipFilter,
        size: Int = 20,
        offset: Int = 0
    ): Result<SearchResult> {
        return try {
            if (!isOnline()) {
                // Offline: Try to get from cache
                return getCachedResult(filter, size, offset)
            }

            val token = getAuthToken()
            val authHeader = if (token.isNotEmpty()) "Bearer $token" else ""

            val response = apiService.searchScholarships(
                keyword = filter.keyword,
                collection = "scholarships",
                size = size,
                offset = offset,
                authToken = authHeader
            )

            if (response.isSuccessful && response.body() != null) {
                val searchResponse = response.body()!!
                val scholarships = searchResponse.items.map { hit ->
                    ScholarshipMapper.toScholarship(hit)
                }
                val hasMore = (offset + scholarships.size) < searchResponse.total
                
                // Cache results if it's the first page (offset = 0) and no filters
                if (offset == 0 && filter.keyword.isBlank() && 
                    filter.country == null && filter.fundingLevel == null &&
                    filter.scholarshipType == null && filter.eligibleFields == null) {
                    cache.saveScholarships(scholarships)
                }
                
                Result.success(
                    SearchResult(
                        scholarships = scholarships,
                        total = searchResponse.total,
                        hasMore = hasMore
                    )
                )
            } else {
                // API error: Try cache as fallback
                getCachedResult(filter, size, offset)
            }
        } catch (e: Exception) {
            // Network error: Try cache as fallback
            getCachedResult(filter, size, offset)
        }
    }

    /**
     * Get cached result when offline
     */
    private suspend fun getCachedResult(
        filter: ScholarshipFilter,
        size: Int,
        offset: Int
    ): Result<SearchResult> {
        val cached = cache.getCachedScholarships()
        return if (cached != null && cache.isCacheValid()) {
            // Filter cached data based on filter criteria
            val filtered = cached.filter { scholarship ->
                val matchesKeyword = filter.keyword.isBlank() || 
                    scholarship.scholarshipName.contains(filter.keyword, ignoreCase = true) ||
                    scholarship.country?.contains(filter.keyword, ignoreCase = true) == true
                val matchesCountry = filter.country == null || 
                    scholarship.country.equals(filter.country, ignoreCase = true)
                val matchesFunding = filter.fundingLevel == null ||
                    scholarship.fundingLevel?.contains(filter.fundingLevel, ignoreCase = true) == true
                val matchesType = filter.scholarshipType == null ||
                    scholarship.scholarshipType?.equals(filter.scholarshipType, ignoreCase = true) == true
                val matchesField = filter.eligibleFields == null || filter.eligibleFields.isEmpty() ||
                    scholarship.eligibleFields?.any { field ->
                        filter.eligibleFields!!.any { it.equals(field, ignoreCase = true) }
                    } == true

                matchesKeyword && matchesCountry && matchesFunding && matchesType && matchesField
            }
            
            // Apply pagination
            val paginated = filtered.drop(offset).take(size)
            val hasMore = (offset + paginated.size) < filtered.size
            
            Result.success(
                SearchResult(
                    scholarships = paginated,
                    total = filtered.size,
                    hasMore = hasMore
                )
            )
        } else {
            Result.failure(Exception("No internet connection and no cached data available"))
        }
    }

    /**
     * Filter scholarships with advanced filters
     * POST /api/v1/es/filter
     * Falls back to cache if offline
     */
    suspend fun filterScholarships(
        filters: List<FilterItem>,
        size: Int = 20,
        offset: Int = 0,
        interFieldOperator: String = "AND"
    ): Result<SearchResult> {
        return try {
            if (!isOnline()) {
                // Offline: Convert filters to ScholarshipFilter and use cache
                val filter = convertFiltersToScholarshipFilter(filters)
                return getCachedResult(filter, size, offset)
            }

            val token = getAuthToken()
            val authHeader = if (token.isNotEmpty()) "Bearer $token" else ""

            val response = apiService.filterScholarships(
                collection = "scholarships",
                size = size,
                offset = offset,
                interFieldOperator = interFieldOperator,
                filters = filters,
                authToken = authHeader
            )

            if (response.isSuccessful && response.body() != null) {
                val searchResponse = response.body()!!
                val scholarships = searchResponse.items.map { hit ->
                    ScholarshipMapper.toScholarship(hit)
                }
                val hasMore = (offset + scholarships.size) < searchResponse.total
                Result.success(
                    SearchResult(
                        scholarships = scholarships,
                        total = searchResponse.total,
                        hasMore = hasMore
                    )
                )
            } else {
                // API error: Try cache as fallback
                val filter = convertFiltersToScholarshipFilter(filters)
                getCachedResult(filter, size, offset)
            }
        } catch (e: Exception) {
            // Network error: Try cache as fallback
            val filter = convertFiltersToScholarshipFilter(filters)
            getCachedResult(filter, size, offset)
        }
    }

    /**
     * Convert FilterItem list to ScholarshipFilter
     */
    private fun convertFiltersToScholarshipFilter(filters: List<FilterItem>): ScholarshipFilter {
        var country: String? = null
        var fundingLevel: String? = null
        var scholarshipType: String? = null
        var eligibleFields: List<String>? = null

        filters.forEach { filter ->
            when (filter.field) {
                "Country" -> country = filter.values.firstOrNull() as? String
                "Funding_Level" -> fundingLevel = filter.values.firstOrNull() as? String
                "Scholarship_Type" -> scholarshipType = filter.values.firstOrNull() as? String
                "Eligible_Fields" -> eligibleFields = filter.values.filterIsInstance<String>()
            }
        }

        return ScholarshipFilter(
            keyword = "",
            country = country,
            fundingLevel = fundingLevel,
            scholarshipType = scholarshipType,
            eligibleFields = eligibleFields
        )
    }

    /**
     * Search or filter scholarships based on filter criteria
     * Uses search API if keyword is provided, otherwise uses filter API
     */
    suspend fun searchOrFilterScholarships(
        filter: ScholarshipFilter,
        size: Int = 20,
        offset: Int = 0
    ): Result<SearchResult> {
        return try {
            // If keyword is provided, use search API
            if (filter.keyword.isNotBlank()) {
                searchScholarships(filter, size, offset)
            } else {
                // Otherwise, build filters for filter API
                val filterItems = buildList {
                    filter.country?.let {
                        add(FilterItem("Country", listOf(it), "OR"))
                    }
                    filter.fundingLevel?.let {
                        add(FilterItem("Funding_Level", listOf(it), "OR"))
                    }
                    filter.scholarshipType?.let {
                        add(FilterItem("Scholarship_Type", listOf(it), "OR"))
                    }
                    filter.eligibleFields?.let {
                        if (it.isNotEmpty()) {
                            add(FilterItem("Eligible_Fields", it.map { field -> field as Any }, "OR"))
                        }
                    }
                    filter.applicationMode?.let {
                        add(FilterItem("Application_Mode", listOf(it), "OR"))
                    }
                }

                if (filterItems.isEmpty()) {
                    // If no filters, search with empty keyword to get all
                    searchScholarships(ScholarshipFilter(keyword = ""), size, offset)
                } else {
                    filterScholarships(filterItems, size, offset)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all scholarships (empty search)
     */
    suspend fun getAllScholarships(size: Int = 20, offset: Int = 0): Result<SearchResult> {
        return searchScholarships(ScholarshipFilter(keyword = ""), size, offset)
    }
}

