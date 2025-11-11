package com.example.scholarlens_fe.presentation.screens.profile

import androidx.lifecycle.viewModelScope
import com.example.scholarlens_fe.data.repository.AuthRepository
import com.example.scholarlens_fe.domain.model.User
import com.example.scholarlens_fe.domain.usecase.GetCurrentUserUseCase
import com.example.scholarlens_fe.domain.usecase.auth.SignOutUseCase
import com.example.scholarlens_fe.domain.usecase.auth.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val authRepository: AuthRepository,
    private val updateProfileUseCase: UpdateProfileUseCase
) : androidx.lifecycle.ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        val user = getCurrentUserUseCase()
        _uiState.value = _uiState.value.copy(
            user = user,
            displayName = user?.displayName ?: "",
            desiredCountries = user?.desiredCountries ?: emptyList(),
            degree = user?.degree,
            fieldOfStudy = user?.fieldOfStudy,
            birthDate = user?.birthDate,
            university = user?.university,
            gpaRange4 = user?.gpaRange4
        )
    }

    fun toggleEditMode() {
        val newEditingState = !_uiState.value.isEditing
        _uiState.value = _uiState.value.copy(isEditing = newEditingState)
        
        // When entering edit mode, ensure current values are loaded
        if (newEditingState) {
            loadUser()
        }
        // When canceling edit, reset to original values
        else {
            loadUser()
        }
    }

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
        _uiState.value = _uiState.value.copy(
            degree = degree,
            degreeError = null
        )
    }

    fun onFieldOfStudyChange(fieldOfStudy: String) {
        _uiState.value = _uiState.value.copy(fieldOfStudy = fieldOfStudy)
    }

    fun onBirthDateChange(birthDate: String?) {
        _uiState.value = _uiState.value.copy(birthDate = birthDate)
    }

    fun onUniversityChange(university: String) {
        _uiState.value = _uiState.value.copy(
            university = university,
            universityError = null
        )
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

    fun saveBasicInfo() {
        val state = _uiState.value
        val userId = authRepository.currentUser?.uid
            ?: run {
                _uiState.value = state.copy(
                    errorMessage = "User not authenticated",
                    isLoading = false
                )
                return
            }

        // Validate display name
        if (state.displayName.isBlank()) {
            _uiState.value = state.copy(displayNameError = "Display name is required")
            return
        }

        // Validate degree
        if (state.degree.isNullOrBlank()) {
            _uiState.value = state.copy(
                degreeError = "Degree is required",
                universityError = null
            )
            return
        }

        // Validate university
        if (state.university.isNullOrBlank()) {
            _uiState.value = state.copy(
                universityError = "University is required",
                degreeError = null
            )
            return
        }

        _uiState.value = state.copy(
            isLoading = true,
            errorMessage = null,
            displayNameError = null,
            degreeError = null,
            universityError = null
        )

        viewModelScope.launch {
            val fields = mutableMapOf<String, Any>(
                "display_name" to state.displayName,
                "degree" to state.degree,
                "university" to state.university
            )

            // Only update display_name, degree, university, and birth_date
            state.birthDate?.let { fields["birth_date"] = it }

            val result = updateProfileUseCase(userId, fields)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isEditing = false,
                    errorMessage = null
                )
                // Reload user to get updated data
                loadUser()
            } else {
                val exception = result.exceptionOrNull() ?: Exception("Unknown error")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to update profile"
                )
            }
        }
    }

    fun signOut() {
        signOutUseCase()
        _uiState.value = _uiState.value.copy(isLoggedOut = true)
    }

    data class ProfileUiState(
        val user: User? = null,
        val isLoggedOut: Boolean = false,
        val isEditing: Boolean = false,
        val displayName: String = "",
        val displayNameError: String? = null,
        val desiredCountries: List<String> = emptyList(),
        val degree: String? = null,
        val degreeError: String? = null,
        val fieldOfStudy: String? = null,
        val birthDate: String? = null,
        val university: String? = null,
        val universityError: String? = null,
        val gpaRange4: String? = null,
        val gpaError: String? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )
}
