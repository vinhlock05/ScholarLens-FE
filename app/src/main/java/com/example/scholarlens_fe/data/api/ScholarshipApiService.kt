package com.example.scholarlens_fe.data.api

import com.example.scholarlens_fe.domain.model.FilterItem
import com.example.scholarlens_fe.domain.model.ScholarshipSearchResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service for Scholarship backend
 * Based on BACKEND_SEARCH_API_SPEC.md
 */
interface ScholarshipApiService {

    /**
     * Search scholarships by keyword
     * GET /api/v1/es/search
     * 
     * @param keyword Search keyword (full-text search)
     * @param collection Collection name (default: "scholarships")
     * @param size Number of results (default: 20)
     * @param offset Pagination offset (default: 0)
     * @param authToken Firebase authentication token (Bearer token)
     */
    @GET("api/v1/es/search")
    suspend fun searchScholarships(
        @Query("q") keyword: String,
        @Query("collection") collection: String = "scholarships",
        @Query("size") size: Int = 20,
        @Query("offset") offset: Int = 0,
        @Header("Authorization") authToken: String = ""
    ): Response<ScholarshipSearchResponse>

    /**
     * Filter scholarships with advanced filters
     * POST /api/v1/es/filter
     * 
     * @param collection Collection name (default: "scholarships")
     * @param size Number of results (default: 20)
     * @param offset Pagination offset (default: 0)
     * @param interFieldOperator Operator between filters: "AND" or "OR" (default: "AND")
     * @param filters List of filter items
     * @param authToken Firebase authentication token (Bearer token)
     */
    @POST("api/v1/es/filter")
    suspend fun filterScholarships(
        @Query("collection") collection: String = "scholarships",
        @Query("size") size: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("inter_field_operator") interFieldOperator: String = "AND",
        @Body filters: List<FilterItem>,
        @Header("Authorization") authToken: String = ""
    ): Response<ScholarshipSearchResponse>
}

