package com.example.scholarlens_fe.data.repository

import android.net.Uri
import com.example.scholarlens_fe.data.api.AuthApiService
import com.example.scholarlens_fe.data.storage.TokenStorage
import com.example.scholarlens_fe.domain.model.AuthResult
import com.example.scholarlens_fe.domain.model.User
import com.example.scholarlens_fe.domain.model.VerifyTokenRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
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
                
                // 5. Fetch profile from backend to check if profile is complete
                val profileResult = getProfile(user.uid)
                if (profileResult.isSuccess) {
                    val profile = profileResult.getOrNull() ?: emptyMap()
                    val profileComplete = checkProfileComplete(profile)
                    return AuthResult.Success(
                        verifyResult.user.copy(
                            displayName = profile["display_name"]?.toString() ?: verifyResult.user.displayName,
                            desiredCountries = (profile["desired_countries"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                            degree = profile["degree"]?.toString(),
                            gpaRange4 = profile["gpa_range_4"]?.toString(),
                            fieldOfStudy = profile["field_of_study"]?.toString(),
                            birthDate = profile["birth_date"]?.toString(),
                            university = profile["university"]?.toString()
                        ),
                        isProfileComplete = profileComplete
                    )
                } else {
                    // If getProfile fails (user doesn't have profile yet), treat as incomplete
                    return AuthResult.Success(
                        verifyResult.user,
                        isProfileComplete = false
                    )
                }
            }
            verifyResult
        } catch (e: Exception) {
            AuthResult.Error(getErrorMessage(e))
        }
    }

    /**
     * Check if user profile has all required basic information
     */
    fun checkProfileComplete(profile: Map<String, Any?>): Boolean {
        val displayName = profile["display_name"]?.toString()
        val desiredCountries = profile["desired_countries"] as? List<*>
        val degree = profile["degree"]?.toString()
        val gpaRange4 = profile["gpa_range_4"]?.toString()
        val fieldOfStudy = profile["field_of_study"]?.toString()

        return !displayName.isNullOrBlank() &&
                desiredCountries != null && desiredCountries.isNotEmpty() &&
                !degree.isNullOrBlank() &&
                !gpaRange4.isNullOrBlank() &&
                !fieldOfStudy.isNullOrBlank()
    }

    /**
     * Check if current user's profile is complete based on stored data
     * This is used when app starts to check if user needs to complete profile
     */
    suspend fun checkCurrentUserProfileComplete(): Boolean {
        val user = currentUser ?: return false
        val profileResult = getProfile(user.uid)
        return if (profileResult.isSuccess) {
            val profile = profileResult.getOrNull() ?: emptyMap()
            checkProfileComplete(profile)
        } else {
            false
        }
    }

    /**
     * Check if current user's profile is complete based on local storage
     * This is a synchronous check that doesn't require API call
     * Used for quick checks when app starts
     */
    fun checkProfileCompleteFromStorage(): Boolean {
        val displayName = tokenStorage.getUserDisplayName()
        val desiredCountries = tokenStorage.getUserDesiredCountries()
        val degree = tokenStorage.getUserDegree()
        val gpaRange4 = tokenStorage.getUserGpa()
        val fieldOfStudy = tokenStorage.getUserFieldOfStudy()

        return !displayName.isNullOrBlank() &&
                desiredCountries.isNotEmpty() &&
                !degree.isNullOrBlank() &&
                !gpaRange4.isNullOrBlank() &&
                !fieldOfStudy.isNullOrBlank()
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
                
                // 4. Fetch profile from backend to check if profile is complete
                val profileResult = getProfile(user.uid)
                if (profileResult.isSuccess) {
                    val profile = profileResult.getOrNull() ?: emptyMap()
                    val profileComplete = checkProfileComplete(profile)
                    return AuthResult.Success(
                        verifyResult.user.copy(
                            displayName = profile["display_name"]?.toString() ?: verifyResult.user.displayName,
                            desiredCountries = (profile["desired_countries"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                            degree = profile["degree"]?.toString(),
                            gpaRange4 = profile["gpa_range_4"]?.toString(),
                            fieldOfStudy = profile["field_of_study"]?.toString(),
                            birthDate = profile["birth_date"]?.toString(),
                            university = profile["university"]?.toString()
                        ),
                        isProfileComplete = profileComplete
                    )
                } else {
                    // If getProfile fails (user doesn't have profile yet), treat as incomplete
                    return AuthResult.Success(
                        verifyResult.user,
                        isProfileComplete = false
                    )
                }
            }
            verifyResult
        } catch (e: Exception) {
            AuthResult.Error(getErrorMessage(e))
        }
    }

    /**
     * Create new user account with email and password
     * After successful Firebase sign-up, gets ID token and verifies with backend
     * New users always have incomplete profile, so isProfileComplete = false
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
                // New users always need to complete profile
                return AuthResult.Success(
                    verifyResult.user,
                    isProfileComplete = false
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

    /**
     * Get user profile
     * GET /api/v1/auth/profile/{uid}
     * Retrieves user profile from backend
     */
    suspend fun getProfile(uid: String): Result<Map<String, Any?>> {
        return try {
            val response = authApiService.getProfile(uid)
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!
                // Save to storage
                tokenStorage.saveUserProfile(profile)
                Result.success(profile)
            } else {
                Result.failure(Exception("Failed to get profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user profile
     * PUT /api/v1/auth/profile/{uid}
     * Updates user profile fields in Firestore (merge fields)
     * After successful update, fetches the updated profile and saves to storage
     */
    suspend fun updateProfile(uid: String, fields: Map<String, Any>): Result<Map<String, Any?>> {
        return try {
            // Convert Map to UpdateProfileRequest
            val request = com.example.scholarlens_fe.domain.model.UpdateProfileRequest(
                display_name = fields["display_name"] as? String,
                desired_countries = fields["desired_countries"] as? List<String>,
                degree = fields["degree"] as? String,
                gpa_range_4 = fields["gpa_range_4"] as? String,
                field_of_study = fields["field_of_study"] as? String,
                birth_date = fields["birth_date"] as? String,
                university = fields["university"] as? String
            )
            
            val updateResponse = authApiService.updateProfile(uid, request)
            if (updateResponse.isSuccessful) {
                // After successful update, fetch the updated profile
                val getProfileResult = getProfile(uid)
                if (getProfileResult.isSuccess) {
                    getProfileResult
                } else {
                    // If getProfile fails, still return success but log the error
                    Result.success(updateResponse.body() ?: emptyMap())
                }
            } else {
                Result.failure(Exception("Failed to update profile: ${updateResponse.message()}"))
            }
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
    suspend fun uploadCV(uri: Uri, onProgress: (Float) -> Unit = {}): com.example.scholarlens_fe.data.repository.CVUploadResult {
        return withContext(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
                ?: throw Exception("User not authenticated")

            val fileName = "cv_${user.uid}_${System.currentTimeMillis()}.pdf"
            val storageRef = storage.reference.child("cvs/$fileName")

            val uploadTask = storageRef.putFile(uri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount.toFloat()
                onProgress(progress)
            }

            val result = uploadTask.await()
            val downloadUrl = result.storage.downloadUrl.await().toString()

            // Update user document with CV info
            val userDocRef = firestore.collection("users").document(user.uid)
            userDocRef.update(
                mapOf(
                    "cvUrl" to downloadUrl,
                    "cvFileName" to fileName,
                    "cvUploadedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            CVUploadResult(downloadUrl, fileName)
        }
    }
}

data class CVUploadResult(
    val downloadUrl: String,
    val fileName: String
)