package com.example.scholarlens_fe.presentation.screens.profile

import androidx.lifecycle.viewModelScope
import com.example.scholarlens_fe.data.repository.AuthRepository
import com.example.scholarlens_fe.domain.usecase.auth.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val updateProfileUseCase: UpdateProfileUseCase
) : androidx.lifecycle.ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    fun onDisplayNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            displayName = name,
            displayNameError = null
        )
    }

    fun toggleCountry(country: String) {
        val currentCountries = _uiState.value.desiredCountries.toMutableList()
        if (currentCountries.contains(country)) {
            currentCountries.remove(country)
        } else {
            currentCountries.add(country)
        }
        _uiState.value = _uiState.value.copy(desiredCountries = currentCountries)
    }

    fun onDegreeChange(degree: String) {
        _uiState.value = _uiState.value.copy(degree = degree)
    }

    fun onFieldOfStudyChange(fieldOfStudy: String) {
        _uiState.value = _uiState.value.copy(fieldOfStudy = fieldOfStudy)
    }

    fun onBirthDateChange(birthDate: String?) {
        _uiState.value = _uiState.value.copy(birthDate = birthDate)
    }

    fun onUniversityChange(university: String) {
        _uiState.value = _uiState.value.copy(university = university)
    }

    fun onGpaRangeChange(gpaRange: String) {
        val trimmed = gpaRange.trim()
        val error = if (trimmed.isNotBlank()) {
            validateGpa(trimmed)
        } else {
            null
        }
        _uiState.value = _uiState.value.copy(
            gpaRange4 = if (trimmed.isBlank()) null else trimmed,
            gpaError = error
        )
    }

    private fun validateGpa(gpa: String): String? {
        return try {
            val gpaValue = gpa.toDouble()
            when {
                gpaValue < 0.0 -> "GPA cannot be negative"
                gpaValue > 4.0 -> "GPA cannot exceed 4.0"
                else -> null
            }
        } catch (e: NumberFormatException) {
            "Please enter a valid number"
        }
    }

    fun saveProfile() {
        val state = _uiState.value

        // Validate display name (required)
        if (state.displayName.isBlank()) {
            _uiState.value = state.copy(displayNameError = "Display name is required")
            return
        }

        // Validate desired countries (required - at least one)
        if (state.desiredCountries.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Please select at least one desired country")
            return
        }

        // Validate degree (required)
        if (state.degree.isNullOrBlank()) {
            _uiState.value = state.copy(errorMessage = "Please select your degree")
            return
        }

        // Validate field of study (required)
        if (state.fieldOfStudy.isNullOrBlank()) {
            _uiState.value = state.copy(errorMessage = "Please select your field of study")
            return
        }

        // Validate GPA (required)
        if (state.gpaRange4.isNullOrBlank()) {
            _uiState.value = state.copy(gpaError = "GPA is required")
            return
        }

        // Validate GPA format
        val gpaError = validateGpa(state.gpaRange4)
        if (gpaError != null) {
            _uiState.value = state.copy(gpaError = gpaError)
            return
        }

        val userId = authRepository.currentUser?.uid
            ?: run {
                _uiState.value = state.copy(
                    errorMessage = "User not authenticated",
                    isLoading = false
                )
                return
            }

        _uiState.value = state.copy(isLoading = true, errorMessage = null, gpaError = null)

        viewModelScope.launch {
            val fields = mutableMapOf<String, Any>(
                "display_name" to state.displayName
            )

            if (state.desiredCountries.isNotEmpty()) {
                fields["desired_countries"] = state.desiredCountries
            }

            state.degree?.let { fields["degree"] = it }

            state.fieldOfStudy?.let { fields["field_of_study"] = it }

            state.birthDate?.let { fields["birth_date"] = it }

            state.university?.let { fields["university"] = it }

            state.gpaRange4?.let { fields["gpa_range_4"] = it }

            val result = updateProfileUseCase(userId, fields)
            if (result.isSuccess) {
                // After successful update, verify profile is complete
                // Profile is already fetched and saved to storage in updateProfile method
                // Verify profile completeness to ensure all required fields are saved
                val profileResult = authRepository.getProfile(userId)
                val isComplete = if (profileResult.isSuccess) {
                    val profile = profileResult.getOrNull() ?: emptyMap()
                    authRepository.checkProfileComplete(profile)
                } else {
                    false
                }
                
                if (isComplete) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSetupComplete = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Profile saved but some required fields are missing. Please try again."
                    )
                }
            } else {
                val exception = result.exceptionOrNull() ?: Exception("Unknown error")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to save profile"
                )
            }
        }
    }

    data class ProfileSetupUiState(
        val displayName: String = "",
        val displayNameError: String? = null,
        val desiredCountries: List<String> = emptyList(),
        val degree: String? = null,
        val fieldOfStudy: String? = null,
        val birthDate: String? = null,
        val university: String? = null,
        val gpaRange4: String? = null,
        val gpaError: String? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isSetupComplete: Boolean = false
    )
}

