package com.example.scholarlens_fe.domain.model

sealed class AuthResult {
    data class Success(
        val user: User,
        val isProfileComplete: Boolean = true
    ) : AuthResult()
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

/**
 * Request model for updating user profile
 */
data class UpdateProfileRequest(
    val display_name: String? = null,
    val desired_countries: List<String>? = null,
    val degree: String? = null,
    val gpa_range_4: String? = null,
    val field_of_study: String? = null,
    val birth_date: String? = null,
    val university: String? = null
)