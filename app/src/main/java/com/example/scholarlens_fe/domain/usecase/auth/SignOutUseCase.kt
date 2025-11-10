package com.example.scholarlens_fe.domain.usecase.auth

import com.example.scholarlens_fe.data.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for signing out the current user
 */
class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke() {
        authRepository.signOut()
    }
}
