package com.example.scholarlens_fe.domain.model

data class Scholarship(
    val id: String,
    val name: String,
    val university: String? = null,
    val openDate: String? = null,
    val closeDate: String? = null,
    val amount: String? = null,
    val fieldOfStudy: String? = null,
    val url: String? = null,
    val daysUntilDeadline: String? = null,
    val score: Double? = null
)

data class ScholarshipFilter(
    val keyword: String = "",
    val name: String? = null,
    val university: String? = null,
    val fieldOfStudy: String? = null,
    val amount: String? = null,
    val sortByDeadline: Boolean = false,
    val sortOrder: SortOrder = SortOrder.ASC
)

enum class SortOrder {
    ASC,
    DESC
}

data class SearchResult(
    val scholarships: List<Scholarship>,
    val total: Int,
    val hasMore: Boolean
)

data class MatchResult(
    val total: Int,
    val hasNextPage: Boolean,
    val nextOffset: Int?,
    val warnings: List<String>,
    val items: List<MatchItem>
)

data class MatchItem(
    val id: String,
    val esScore: Double,
    val matchScore: Double,
    val matchedFields: List<String>,
    val summaryName: String?,
    val summaryStartDate: String?,
    val summaryEndDate: String?,
    val summaryAmount: String?
)

data class MatchProfile(
    val name: String? = null,
    val universities: List<String>? = null,
    val fieldOfStudy: String? = null,
    val minAmount: String? = null,
    val maxAmount: String? = null,
    val deadlineAfter: String? = null,
    val deadlineBefore: String? = null
)