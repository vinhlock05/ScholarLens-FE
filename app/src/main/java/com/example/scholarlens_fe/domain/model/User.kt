package com.example.scholarlens_fe.domain.model

/**
 * Domain model for User
 * This represents the core business object in the domain layer
 */
data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val desiredCountries: List<String> = emptyList(),
    val degree: String? = null,
    val gpaRange4: String? = null,
    val fieldOfStudy: String? = null,
    val birthDate: String? = null,
    val university: String? = null
)

