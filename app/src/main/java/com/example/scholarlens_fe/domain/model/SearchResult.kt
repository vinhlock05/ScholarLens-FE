package com.example.scholarlens_fe.domain.model

/**
 * Search result containing scholarships and metadata
 */
data class SearchResult(
    val scholarships: List<Scholarship>,
    val total: Int,
    val hasMore: Boolean
)

