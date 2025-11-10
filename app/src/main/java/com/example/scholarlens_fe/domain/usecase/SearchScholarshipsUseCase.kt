package com.example.scholarlens_fe.domain.usecase

import com.example.scholarlens_fe.data.repository.ScholarshipRepository
import com.example.scholarlens_fe.domain.model.ScholarshipFilter
import com.example.scholarlens_fe.domain.model.SearchResult
import javax.inject.Inject

/**
 * Use case for searching scholarships
 * Encapsulates business logic for scholarship search
 */
class SearchScholarshipsUseCase @Inject constructor(
    private val repository: ScholarshipRepository
) {
    suspend operator fun invoke(
        filter: ScholarshipFilter,
        size: Int = 20,
        offset: Int = 0
    ): Result<SearchResult> {
        return repository.searchOrFilterScholarships(filter, size, offset)
    }
}

