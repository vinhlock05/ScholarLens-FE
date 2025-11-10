package com.example.scholarlens_fe.domain.usecase.auth

import com.example.scholarlens_fe.data.repository.AuthRepository
import com.example.scholarlens_fe.domain.model.AuthResult
import javax.inject.Inject

/**
 * Use case for signing up with email and password
 */
class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        return authRepository.signUp(email, password)
    }
}
