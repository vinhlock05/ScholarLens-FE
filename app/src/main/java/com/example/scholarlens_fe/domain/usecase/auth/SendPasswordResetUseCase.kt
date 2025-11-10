package com.example.scholarlens_fe.domain.usecase.auth

import com.example.scholarlens_fe.data.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for sending password reset email
 */
class SendPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return authRepository.sendPasswordResetEmail(email)
    }
}

