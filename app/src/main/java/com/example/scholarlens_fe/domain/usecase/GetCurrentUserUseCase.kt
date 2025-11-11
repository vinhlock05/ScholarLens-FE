package com.example.scholarlens_fe.domain.usecase

import com.example.scholarlens_fe.data.repository.AuthRepository
import com.example.scholarlens_fe.data.storage.TokenStorage
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
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage
) {
    operator fun invoke(): User? {
        val firebaseUser = authRepository.currentUser ?: return null

        // Get profile data from storage (which is updated after API calls)
        val displayName = tokenStorage.getUserDisplayName() 
            ?: firebaseUser.displayName 
            ?: ""
        val desiredCountries = tokenStorage.getUserDesiredCountries()
        val degree = tokenStorage.getUserDegree()
        val gpaRange4 = tokenStorage.getUserGpa()
        val fieldOfStudy = tokenStorage.getUserFieldOfStudy()
        val birthDate = tokenStorage.getUserBirthDate()
        val university = tokenStorage.getUserUniversity()

        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = displayName,
            photoUrl = firebaseUser.photoUrl?.toString(),
            desiredCountries = desiredCountries,
            degree = degree,
            gpaRange4 = gpaRange4,
            fieldOfStudy = fieldOfStudy,
            birthDate = birthDate,
            university = university
        )
    }

    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn
    }
}