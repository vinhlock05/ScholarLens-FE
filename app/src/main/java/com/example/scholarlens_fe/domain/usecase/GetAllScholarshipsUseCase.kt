package com.example.scholarlens_fe.domain.usecase

import com.example.scholarlens_fe.data.repository.ScholarshipRepository
import com.example.scholarlens_fe.domain.model.SearchResult
import javax.inject.Inject

/**
 * Use case for getting all scholarships
 */
class GetAllScholarshipsUseCase @Inject constructor(
    private val repository: ScholarshipRepository
) {
    suspend operator fun invoke(size: Int = 20, offset: Int = 0): Result<SearchResult> {
        return repository.getAllScholarships(size, offset)
    }
}

