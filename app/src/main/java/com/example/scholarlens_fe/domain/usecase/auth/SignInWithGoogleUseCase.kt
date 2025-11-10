package com.example.scholarlens_fe.domain.usecase.auth

import com.example.scholarlens_fe.data.repository.AuthRepository
import com.example.scholarlens_fe.domain.model.AuthResult
import javax.inject.Inject

/**
 * Use case for signing in with Google
 */
class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): AuthResult {
        return authRepository.signInWithGoogle(idToken)
    }
}
