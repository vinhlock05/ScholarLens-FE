package com.example.scholarlens_fe.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scholarlens_fe.domain.model.AuthResult
import com.example.scholarlens_fe.domain.usecase.auth.SendPasswordResetUseCase
import com.example.scholarlens_fe.domain.usecase.auth.SignInUseCase
import com.example.scholarlens_fe.domain.usecase.auth.SignInWithGoogleUseCase
import com.example.scholarlens_fe.domain.usecase.auth.SignOutUseCase
import com.example.scholarlens_fe.domain.usecase.auth.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Authentication screens
 * Manages authentication state and operations
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Sign in with Google using ID token
     * Called after Google Sign-In flow completes and returns ID token
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                emailError = null,
                passwordError = null
            )

            val result = signInWithGoogleUseCase(idToken)
            handleAuthResult(result)
        }
    }

    /**
     * Sign in with email and password
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                errorMessage = null,
                emailError = null,
                passwordError = null
            )

            val result = signInUseCase(email, password)
            handleAuthResult(result)
        }
    }

    /**
     * Sign up with email and password
     */
    fun signUp(email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                errorMessage = null,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null
            )

            val result = signUpUseCase(email, password)
            handleAuthResult(result)
        }
    }

    /**
     * Send password reset email
     */
    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            val result = sendPasswordResetUseCase(email)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        passwordResetSent = true
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to send password reset email"
                    )
                }
            )
        }
    }

    /**
     * Sign out current user
     */
    suspend fun signOut() {
        signOutUseCase()
        _uiState.value = AuthUiState()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            error = null,
            errorMessage = null
        )
    }

    /**
     * Clear password reset sent flag
     */
    fun clearPasswordResetSent() {
        _uiState.value = _uiState.value.copy(passwordResetSent = false)
    }

    // Form fields
    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(
            email = value,
            emailError = null,
            errorMessage = null
        )
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            passwordError = null,
            errorMessage = null
        )
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = value,
            confirmPasswordError = null,
            errorMessage = null
        )
    }

    /**
     * Login with email and password from UI state
     */
    fun login() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        // Validate email
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(emailError = "Email is required")
            return
        }
        if (!isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(emailError = "Invalid email format")
            return
        }

        // Validate password
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(passwordError = "Password is required")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(passwordError = "Password must be at least 6 characters")
            return
        }

        signIn(email, password)
    }

    /**
     * Register with email and password from UI state
     */
    fun register() {
        val email = _uiState.value.email
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        // Validate email
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(emailError = "Email is required")
            return
        }
        if (!isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(emailError = "Invalid email format")
            return
        }

        // Validate password
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(passwordError = "Password is required")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(passwordError = "Password must be at least 6 characters")
            return
        }

        // Validate confirm password
        if (confirmPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(confirmPasswordError = "Please confirm your password")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(confirmPasswordError = "Passwords do not match")
            return
        }

        signUp(email, password, confirmPassword)
    }

    /**
     * Sign in with Google
     * Note: This requires Google Sign-In setup and Activity context
     * For now, this is a placeholder that should be implemented with Activity result
     */
    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            // TODO: Implement Google Sign-In
            // This requires Activity context and Google Sign-In setup
            // For now, we'll show an error message
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Google Sign-In is not yet implemented"
            )
        }
    }

    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Handle authentication result
     */
    private fun handleAuthResult(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    errorMessage = null,
                    emailError = null,
                    passwordError = null,
                    confirmPasswordError = null,
                    isAuthenticated = true,
                    isProfileComplete = result.isProfileComplete,
                    user = result.user
                )
            }
            is AuthResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message,
                    errorMessage = result.message
                )
            }
        }
    }

    /**
     * UI State for Authentication screens
     */
    data class AuthUiState(
        // Form fields
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",

        // Field errors
        val emailError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null,

        // Loading and error states
        val isLoading: Boolean = false,
        val error: String? = null,
        val errorMessage: String? = null,
        val isAuthenticated: Boolean = false,
        val isProfileComplete: Boolean = true,
        val user: com.example.scholarlens_fe.domain.model.User? = null,
        val passwordResetSent: Boolean = false
    )
}
