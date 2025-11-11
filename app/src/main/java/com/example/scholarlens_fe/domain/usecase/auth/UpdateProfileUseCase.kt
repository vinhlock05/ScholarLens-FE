package com.example.scholarlens_fe.domain.usecase.auth

import com.example.scholarlens_fe.data.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for updating user profile
 */
class UpdateProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(uid: String, fields: Map<String, Any>): Result<Map<String, Any?>> {
        return authRepository.updateProfile(uid, fields)
    }
}


