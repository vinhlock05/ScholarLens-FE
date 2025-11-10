package com.example.scholarlens_fe.domain.usecase.auth

import com.example.scholarlens_fe.data.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for refreshing the Firebase ID token
 * Firebase tokens expire after ~1 hour
 */
class RefreshTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): String? {
        return authRepository.refreshToken()
    }
}

