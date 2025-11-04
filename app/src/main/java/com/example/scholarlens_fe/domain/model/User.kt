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
    val createdAt: Long = System.currentTimeMillis()
)

