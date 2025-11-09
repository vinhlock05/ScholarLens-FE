package com.example.scholarlens_fe.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scholarlens_fe.domain.model.Scholarship
import com.example.scholarlens_fe.domain.model.ScholarshipFilter
import com.example.scholarlens_fe.domain.usecase.SearchScholarshipsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Home Screen
 */
data class HomeUiState(
    val scholarships: List<Scholarship> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isEmpty: Boolean = false,
    val totalCount: Int = 0,
    val hasMore: Boolean = false,
    val currentPage: Int = 0,
    val pageSize: Int = 20,
    // Filters
    val selectedCountry: String? = null,
    val selectedFundingLevel: String? = null,
    val selectedScholarshipType: String? = null,
    val selectedFieldOfStudy: String? = null,
    // Filter options for dropdowns
    val availableCountries: List<String> = emptyList(),
    val availableFundingLevels: List<String> = emptyList(),
    val availableScholarshipTypes: List<String> = emptyList(),
    val availableFieldsOfStudy: List<String> = emptyList()
)

/**
 * ViewModel for Home Screen
 * Manages scholarship search and display logic
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchScholarshipsUseCase: SearchScholarshipsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadScholarships(reset = true)
    }

    /**
     * Load scholarships with current filters and pagination
     */
    private fun loadScholarships(reset: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val offset = if (reset) 0 else currentState.scholarships.size

            _uiState.update { 
                it.copy(
                    isLoading = reset,
                    isLoadingMore = !reset,
                    error = null
                )
            }

            val filter = buildFilter()
            val size = currentState.pageSize

            searchScholarshipsUseCase(filter, size = size, offset = offset)
                .onSuccess { searchResult ->
                    val updatedList = if (reset) {
                        searchResult.scholarships
                    } else {
                        currentState.scholarships + searchResult.scholarships
                    }

                    _uiState.update {
                        it.copy(
                            scholarships = updatedList,
                            isLoading = false,
                            isLoadingMore = false,
                            isEmpty = updatedList.isEmpty() && reset,
                            totalCount = if (reset) searchResult.total else it.totalCount,
                            hasMore = searchResult.hasMore,
                            currentPage = if (reset) 0 else it.currentPage + 1,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = getErrorMessage(exception),
                            isEmpty = it.scholarships.isEmpty()
                        )
                    }
                }
        }
    }

    /**
     * Build filter from current UI state
     */
    private fun buildFilter(): ScholarshipFilter {
        val state = _uiState.value
        return ScholarshipFilter(
            keyword = state.searchQuery.trim(),
            country = state.selectedCountry,
            fundingLevel = state.selectedFundingLevel,
            scholarshipType = state.selectedScholarshipType,
            eligibleFields = state.selectedFieldOfStudy?.let { listOf(it) }
        )
    }

    /**
     * Update search query with debounce
     */
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // Reset and reload when search changes
        viewModelScope.launch {
            kotlinx.coroutines.delay(500) // Debounce 500ms
            if (_uiState.value.searchQuery == query) {
                loadScholarships(reset = true)
            }
        }
    }

    /**
     * Load more scholarships (pagination)
     */
    fun loadMore() {
        val state = _uiState.value
        if (!state.isLoadingMore && state.hasMore && !state.isLoading) {
            loadScholarships(reset = false)
        }
    }

    /**
     * Apply country filter
     */
    fun setCountryFilter(country: String?) {
        _uiState.update { it.copy(selectedCountry = country) }
        loadScholarships(reset = true)
    }

    /**
     * Apply funding level filter
     */
    fun setFundingLevelFilter(fundingLevel: String?) {
        _uiState.update { it.copy(selectedFundingLevel = fundingLevel) }
        loadScholarships(reset = true)
    }

    /**
     * Apply scholarship type filter
     */
    fun setScholarshipTypeFilter(scholarshipType: String?) {
        _uiState.update { it.copy(selectedScholarshipType = scholarshipType) }
        loadScholarships(reset = true)
    }

    /**
     * Apply field of study filter
     */
    fun setFieldOfStudyFilter(fieldOfStudy: String?) {
        _uiState.update { it.copy(selectedFieldOfStudy = fieldOfStudy) }
        loadScholarships(reset = true)
    }

    /**
     * Clear all filters
     */
    fun clearFilters() {
        _uiState.update {
            it.copy(
                selectedCountry = null,
                selectedFundingLevel = null,
                selectedScholarshipType = null,
                selectedFieldOfStudy = null,
                searchQuery = ""
            )
        }
        loadScholarships(reset = true)
    }

    /**
     * Retry loading scholarships after error
     */
    fun retry() {
        loadScholarships(reset = true)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Get user-friendly error message
     * Note: In a real app, you'd use string resources via Context
     * For now, returning English messages
     */
    private fun getErrorMessage(exception: Throwable): String {
        return when {
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

