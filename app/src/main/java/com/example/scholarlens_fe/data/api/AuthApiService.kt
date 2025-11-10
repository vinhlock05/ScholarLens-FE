package com.example.scholarlens_fe.data.api

import com.example.scholarlens_fe.domain.model.VerifyTokenRequest
import com.example.scholarlens_fe.domain.model.VerifyTokenResponse
import retrofit2.Response
import retrofit2.http.Body
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
}