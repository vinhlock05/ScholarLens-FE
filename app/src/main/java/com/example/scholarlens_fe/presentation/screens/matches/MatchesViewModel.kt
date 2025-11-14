package com.example.scholarlens_fe.presentation.screens.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scholarlens_fe.domain.model.MatchItem
import com.example.scholarlens_fe.domain.usecase.MatchScholarshipsWithProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Matches Screen
 */
data class MatchesUiState(
    val matchItems: List<MatchItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEmpty: Boolean = false,
    val totalMatches: Int = 0,
    val warnings: List<String> = emptyList()
)

/**
 * ViewModel for Matches Screen
 * Manages scholarship matching logic based on user profile
 */
@HiltViewModel
class MatchesViewModel @Inject constructor(
    private val matchScholarshipsWithProfileUseCase: MatchScholarshipsWithProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    init {
        loadMatches()
    }

    /**
     * Load top 3 matched scholarships based on user profile
     */
    fun loadMatches() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            matchScholarshipsWithProfileUseCase(size = 3, offset = 0)
                .onSuccess { matchResult ->
                    _uiState.update {
                        it.copy(
                            matchItems = matchResult.items,
                            isLoading = false,
                            isEmpty = matchResult.items.isEmpty(),
                            totalMatches = matchResult.total,
                            warnings = matchResult.warnings,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = getErrorMessage(exception),
                            isEmpty = it.matchItems.isEmpty()
                        )
                    }
                }
        }
    }

    /**
     * Retry loading matches after error
     */
    fun retry() {
        loadMatches()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Get user-friendly error message
     */
    private fun getErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("User not logged in", ignoreCase = true) == true ->
                "Please sign in to see your matches."
            exception.message?.contains("Failed to get user profile", ignoreCase = true) == true ->
                "Unable to load your profile. Please update your profile in the Profile tab."
            exception.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                "No internet connection. Please check your network."
            exception.message?.contains("timeout", ignoreCase = true) == true ->
                "Request timeout. Please try again."
            exception.message?.contains("401", ignoreCase = true) == true ->
                "Authentication failed. Please sign in again."
            exception.message?.contains("404", ignoreCase = true) == true ->
                "Service not found. Please try again later."
            exception.message?.contains("500", ignoreCase = true) == true ->
                "Server error. Please try again later."
            exception.message?.contains("403", ignoreCase = true) == true ->
                "Access denied. Please check your permissions."
            exception.message?.contains("network", ignoreCase = true) == true ->
                "Network error. Please check your connection."
            else -> exception.message ?: "An error occurred. Please try again."
        }
    }
}

