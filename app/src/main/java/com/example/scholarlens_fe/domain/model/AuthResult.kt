package com.example.scholarlens_fe.domain.model

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Request model for token verification
 */
data class VerifyTokenRequest(
    val id_token: String
)

/**
 * Response model for token verification
 */
data class VerifyTokenResponse(
    val uid: String,
    val email: String? = null,
    val name: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val provider: String? = null
)
