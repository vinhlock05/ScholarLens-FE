package com.example.scholarlens_fe.data.api

import com.example.scholarlens_fe.domain.model.UpdateProfileRequest
import com.example.scholarlens_fe.domain.model.VerifyTokenRequest
import com.example.scholarlens_fe.domain.model.VerifyTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.POST

interface AuthApiService {

    /**
     * Verify Firebase ID token with backend
     * POST /api/v1/auth/verify
     *
     * @param request VerifyTokenRequest containing id_token
     * @return VerifyTokenResponse with decoded token info
     */
    @POST("api/v1/auth/verify")
    suspend fun verifyToken(
        @Body request: VerifyTokenRequest
    ): Response<VerifyTokenResponse>

    /**
     * Get user profile
     * GET /api/v1/auth/profile/{uid}
     *
     * @param uid User ID
     * @return User profile as Map
     */
    @GET("api/v1/auth/profile/{uid}")
    suspend fun getProfile(
        @Path("uid") uid: String
    ): Response<Map<String, Any?>>

    /**
     * Update user profile
     * PUT /api/v1/auth/profile/{uid}
     *
     * @param uid User ID
     * @param request UpdateProfileRequest containing fields to update
     * @return Updated user profile as Map
     */
    @PUT("api/v1/auth/profile/{uid}")
    suspend fun updateProfile(
        @Path("uid") uid: String,
        @Body request: UpdateProfileRequest
    ): Response<Map<String, Any?>>
}