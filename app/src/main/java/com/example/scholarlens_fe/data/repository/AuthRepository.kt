package com.example.scholarlens_fe.data.repository

import com.example.scholarlens_fe.data.api.AuthApiService
import com.example.scholarlens_fe.data.storage.TokenStorage
import com.example.scholarlens_fe.domain.model.AuthResult
import com.example.scholarlens_fe.domain.model.User
import com.example.scholarlens_fe.domain.model.VerifyTokenRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Firebase Authentication operations
 * Handles user sign in, sign up, sign out, token verification, and refresh
 * Based on AUTHENTICATION_FLOW.md
 */
@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val authApiService: AuthApiService,
    private val tokenStorage: TokenStorage,
) {
    /**
     * Get current logged in user
     */
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /**
     * Check if user is logged in
     */
    val isUserLoggedIn: Boolean
        get() = currentUser != null

    /**
     * Get Firebase ID token
     * @param forceRefresh If true, force refresh the token
     * @return ID token string or null if not available
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): String? {
        val user = firebaseAuth.currentUser ?: return null
        return try {
            val tokenResult: GetTokenResult = user.getIdToken(forceRefresh).await()
            tokenResult.token
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Sign in with Google
     * Uses Firebase Auth with Google provider
     * @param idToken The Google ID token from Google Sign-In
     */
    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            // 1. Create Firebase credential from Google ID token
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)

            // 2. Sign in with Firebase using Google credential
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: return AuthResult.Error("Google sign in failed")

            // 3. Get Firebase ID token
            val firebaseIdToken = getIdToken(forceRefresh = false)
                ?: return AuthResult.Error("Failed to get Firebase ID token")

            // 4. Verify token with backend
            val verifyResult = verifyTokenWithBackend(firebaseIdToken)
            if (verifyResult is AuthResult.Success) {
                // Save token and user info
                tokenStorage.saveToken(firebaseIdToken)
                tokenStorage.saveUserInfo(
                    userId = user.uid,
                    email = user.email ?: "",
                    name = user.displayName ?: ""
                )
            }
            verifyResult
        } catch (e: Exception) {
            AuthResult.Error(getErrorMessage(e))
        }
    }

    /**
     * Sign in with email and password
     * After successful Firebase sign-in, gets ID token and verifies with backend
     */
    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            // 1. Sign in with Firebase
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("Sign in failed")

            // 2. Get ID token
            val idToken = getIdToken(forceRefresh = false)
                ?: return AuthResult.Error("Failed to get ID token")

            // 3. Verify token with backend
            val verifyResult = verifyTokenWithBackend(idToken)
            if (verifyResult is AuthResult.Success) {
                // Save token and user info
                tokenStorage.saveToken(idToken)
                tokenStorage.saveUserInfo(
                    userId = user.uid,
                    email = user.email,
                    name = user.displayName
                )
            }
            verifyResult
        } catch (e: Exception) {
            AuthResult.Error(getErrorMessage(e))
        }
    }

    /**
     * Create new user account with email and password
     * After successful Firebase sign-up, gets ID token and verifies with backend
     */
    suspend fun signUp(email: String, password: String): AuthResult {
        return try {
            // 1. Create user with Firebase
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("Sign up failed")

            // 2. Get ID token
            val idToken = getIdToken(forceRefresh = false)
                ?: return AuthResult.Error("Failed to get ID token")

            // 3. Verify token with backend
            val verifyResult = verifyTokenWithBackend(idToken)
            if (verifyResult is AuthResult.Success) {
                // Save token and user info
                tokenStorage.saveToken(idToken)
                tokenStorage.saveUserInfo(
                    userId = user.uid,
                    email = user.email,
                    name = user.displayName
                )
            }
            verifyResult
        } catch (e: Exception) {
            AuthResult.Error(getErrorMessage(e))
        }
    }

    /**
     * Verify Firebase ID token with backend
     * Based on AUTHENTICATION_FLOW.md
     */
    suspend fun verifyTokenWithBackend(idToken: String): AuthResult {
        return try {
            val request = VerifyTokenRequest(id_token = idToken)
            val response = authApiService.verifyToken(request)

            if (response.isSuccessful && response.body() != null) {
                val verifyResponse = response.body()!!
                val user = User(
                    id = verifyResponse.uid,
                    email = verifyResponse.email ?: "",
                    displayName = verifyResponse.displayName ?: verifyResponse.name ?: "",
                    photoUrl = verifyResponse.photoUrl
                )
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Token verification failed: ${response.message()}")
            }
        } catch (e: Exception) {
            AuthResult.Error("Token verification error: ${e.message}")
        }
    }

    /**
     * Refresh ID token
     * Used when token expires (Firebase tokens expire after ~1 hour)
     */
    suspend fun refreshToken(): String? {
        return try {
            val newToken = getIdToken(forceRefresh = true)
            newToken?.let {
                tokenStorage.saveToken(it)
            }
            newToken
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        try {
            // Clear token storage
            tokenStorage.clear()
            firebaseAuth.signOut()
        } catch (e: Exception) {
            // Log error
        }
    }

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is com.google.firebase.auth.FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Invalid email address"
                    "ERROR_WRONG_PASSWORD" -> "Wrong password"
                    "ERROR_USER_NOT_FOUND" -> "User not found"
                    "ERROR_USER_DISABLED" -> "User account disabled"
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Email already in use"
                    "ERROR_WEAK_PASSWORD" -> "Password is too weak"
                    else -> exception.message ?: "Authentication failed"
                }
            }
            else -> exception.message ?: "An error occurred"
        }
    }
}