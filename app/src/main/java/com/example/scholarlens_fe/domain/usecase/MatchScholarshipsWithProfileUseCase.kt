package com.example.scholarlens_fe.domain.usecase

import com.example.scholarlens_fe.data.api.AuthApiService
import com.example.scholarlens_fe.data.repository.AuthRepository
import com.example.scholarlens_fe.domain.model.MatchProfile
import com.example.scholarlens_fe.domain.model.MatchResult
import javax.inject.Inject

/**
 * Use case to match scholarships based on user profile from API
 * 
 * This use case:
 * 1. Gets current user's UID from Firebase Auth
 * 2. Fetches user profile from API using AuthApiService
 * 3. Extracts fields needed for UserProfileInput and maps them to MatchProfile
 * 4. Calls MatchScholarshipsUseCase to get matched scholarships
 */
class MatchScholarshipsWithProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val authApiService: AuthApiService,
    private val matchScholarshipsUseCase: MatchScholarshipsUseCase
) {
    suspend operator fun invoke(
        size: Int = 10,
        offset: Int = 0
    ): Result<MatchResult> {
        // 1. Get current user UID
        val user = authRepository.currentUser
            ?: return Result.failure(Exception("User not logged in"))

        val uid = user.uid

        // 2. Fetch profile from API
        return try {
            val response = authApiService.getProfile(uid)
            if (!response.isSuccessful || response.body() == null) {
                return Result.failure(
                    Exception("Failed to get user profile: ${response.message()}")
                )
            }

            val profile = response.body()!!

            // 3. Extract fields for UserProfileInput and map to MatchProfile
            val matchProfile = extractMatchProfile(profile)

            // 4. Match scholarships using the profile
            matchScholarshipsUseCase(matchProfile, size, offset)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extract UserProfileInput fields from profile Map and create MatchProfile
     * 
     * Maps API profile fields to GraphQL UserProfileInput:
     * - display_name -> name
     * - university -> university (single string to list)
     * - field_of_study -> fieldOfStudy
     * - Other fields (minAmount, maxAmount, deadlineAfter, deadlineBefore) are not in profile
     */
    private fun extractMatchProfile(profile: Map<String, Any?>): MatchProfile? {
        val name = profile["display_name"]?.toString()?.takeIf { it.isNotBlank() }
        val universityString = profile["university"]?.toString()?.takeIf { it.isNotBlank() }
        val universities = universityString?.let { listOf(it) }
        val fieldOfStudy = profile["field_of_study"]?.toString()?.takeIf { it.isNotBlank() }

        // If no relevant fields, return null (will match without profile)
        if (name == null && universities == null && fieldOfStudy == null) {
            return null
        }

        return MatchProfile(
            name = name,
            universities = universities,
            fieldOfStudy = fieldOfStudy,
            minAmount = null, // Not available in profile
            maxAmount = null, // Not available in profile
            deadlineAfter = null, // Not available in profile
            deadlineBefore = null // Not available in profile
        )
    }
}

