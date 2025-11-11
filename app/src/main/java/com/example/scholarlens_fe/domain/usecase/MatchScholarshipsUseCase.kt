package com.example.scholarlens_fe.domain.usecase

import com.example.scholarlens_fe.data.repository.ScholarshipRepository
import com.example.scholarlens_fe.domain.model.MatchProfile
import com.example.scholarlens_fe.domain.model.MatchResult
import javax.inject.Inject

class MatchScholarshipsUseCase @Inject constructor(
    private val repository: ScholarshipRepository
) {
    suspend operator fun invoke(
        profile: MatchProfile?,
        size: Int = 10,
        offset: Int = 0
    ): Result<MatchResult> {
        return repository.matchScholarships(profile, size, offset)
    }
}

