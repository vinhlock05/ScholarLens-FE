package com.example.scholarlens_fe.domain.usecase

import com.example.scholarlens_fe.data.repository.AuthRepository
import com.example.scholarlens_fe.domain.model.User
import javax.inject.Inject

/**
 * Use case for getting the current logged in user
 * This is part of the domain layer and encapsulates business logic
 * 
 * Example of Clean Architecture:
 * - Presentation layer calls this use case
 * - Use case calls repository in data layer
 * - Use case transforms data to domain models
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): User? {
        val firebaseUser = authRepository.currentUser ?: return null

        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "",
            photoUrl = firebaseUser.photoUrl?.toString()
        )
    }

    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn
    }
}