package com.example.scholarlens_fe.data.interceptor

import com.example.scholarlens_fe.data.storage.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Interceptor to add Authorization header to API requests
 * Based on AUTHENTICATION_FLOW.md
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Get token from storage
        val token = tokenStorage.getToken()

        // Add Authorization header if token exists
        val requestBuilder = originalRequest.newBuilder()
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        val request = requestBuilder.build()
        val response = chain.proceed(request)

        // Handle 401 Unauthorized - Token expired or invalid
        if (response.code == 401) {
            // Token might be expired, clear it so user can re-authenticate
            tokenStorage.clear()
        }

        return response
    }
}