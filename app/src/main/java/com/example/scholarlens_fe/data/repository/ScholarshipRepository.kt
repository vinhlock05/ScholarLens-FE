package com.example.scholarlens_fe.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.example.scholarlens_fe.data.cache.ScholarshipCache
import com.example.scholarlens_fe.domain.model.MatchItem
import com.example.scholarlens_fe.domain.model.MatchProfile
import com.example.scholarlens_fe.domain.model.MatchResult
import com.example.scholarlens_fe.domain.model.Scholarship
import com.example.scholarlens_fe.domain.model.ScholarshipFilter
import com.example.scholarlens_fe.domain.model.SearchResult
import com.example.scholarlens_fe.domain.model.SortOrder
import com.example.scholarlens_fe.graphql.MatchScholarshipsQuery
import com.example.scholarlens_fe.graphql.SearchScholarshipsQuery
import com.example.scholarlens_fe.graphql.type.InterFieldOperator
import com.example.scholarlens_fe.graphql.type.ScholarshipFilter as GqlScholarshipFilter
import com.example.scholarlens_fe.graphql.type.SortOrder as GqlSortOrder
import com.example.scholarlens_fe.graphql.type.UserProfileInput
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScholarshipRepository @Inject constructor(
    private val apolloClient: ApolloClient,
    private val firebaseAuth: FirebaseAuth,
    private val cache: ScholarshipCache,
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val COLLECTION_SCHOLARSHIPS = "scholar_lens"
    }

    suspend fun searchOrFilterScholarships(
        filter: ScholarshipFilter,
        size: Int = 20,
        offset: Int = 0
    ): Result<SearchResult> = performGraphQLSearch(filter, size, offset)

    suspend fun getAllScholarships(size: Int = 20, offset: Int = 0): Result<SearchResult> =
        performGraphQLSearch(ScholarshipFilter(), size, offset)

    suspend fun matchScholarships(
        profile: MatchProfile?,
        size: Int = 10,
        offset: Int = 0
    ): Result<MatchResult> {
        if (!isOnline()) {
            return Result.failure(Exception("Match recommendations require an internet connection"))
        }

        val token = getAuthToken()
        val query = MatchScholarshipsQuery(
            profile = Optional.presentIfNotNull(profile?.let(::buildGraphQLProfile)),
            size = Optional.present(size),
            offset = Optional.present(offset)
        )

        return try {
            var apolloCall = apolloClient.query(query)
            if (token.isNotEmpty()) {
                apolloCall = apolloCall.addHttpHeader("Authorization", "Bearer $token")
            }

            val response = apolloCall.execute()
            val data = response.data
            val errors = response.errors

            if (data == null || !errors.isNullOrEmpty()) {
                val message = errors?.firstOrNull()?.message ?: "matchScholarships returned no data"
                Result.failure(Exception(message))
            } else {
                val matchResult = data.matchScholarships
                val items = matchResult.items.map {
                    MatchItem(
                        id = it.id,
                        esScore = it.esScore.toDouble(),
                        matchScore = it.matchScore.toDouble(),
                        matchedFields = it.matchedFields,
                        summaryName = it.summaryName,
                        summaryStartDate = it.summaryStartDate,
                        summaryEndDate = it.summaryEndDate,
                        summaryAmount = it.summaryAmount
                    )
                }

                Result.success(
                    MatchResult(
                        total = matchResult.total,
                        hasNextPage = matchResult.hasNextPage,
                        nextOffset = matchResult.nextOffset,
                        warnings = matchResult.warnings ?: emptyList(),
                        items = items
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun performGraphQLSearch(
        filter: ScholarshipFilter,
        size: Int,
        offset: Int
    ): Result<SearchResult> {
        if (!isOnline()) {
            return getCachedResult(filter, size, offset)
        }

        val token = getAuthToken()
        val gqlFilter = buildGraphQLFilter(filter)

        val query = SearchScholarshipsQuery(
            collection = COLLECTION_SCHOLARSHIPS,
            q = Optional.presentIfNotNull(filter.keyword.trim().takeIf { it.isNotEmpty() }),
            filter = Optional.presentIfNotNull(gqlFilter),
            interFieldOperator = Optional.present(InterFieldOperator.AND),
            sortByDeadline = Optional.present(filter.sortByDeadline),
            sortOrder = Optional.present(mapSortOrder(filter.sortOrder)),
            size = Optional.present(size),
            offset = Optional.present(offset)
        )

        return try {
            var apolloCall = apolloClient.query(query)
            if (token.isNotEmpty()) {
                apolloCall = apolloCall.addHttpHeader("Authorization", "Bearer $token")
            }

            val response = apolloCall.execute()
            val data = response.data
            val errors = response.errors

            if (data == null || !errors.isNullOrEmpty()) {
                val cacheFallback = getCachedResult(filter, size, offset)
                if (cacheFallback.isSuccess) {
                    cacheFallback
                } else {
                    val message = errors?.firstOrNull()?.message ?: "searchEs returned no data"
                    Result.failure(Exception(message))
                }
            } else {
                val searchResult = data.searchEs
                val scholarships = searchResult.items.mapNotNull { item ->
                    val source = item.source ?: return@mapNotNull null
                    Scholarship(
                        id = item.id,
                        name = source.name.orEmpty(),
                        university = source.university,
                        openDate = source.openTime,
                        closeDate = source.closeTime,
                        amount = source.amount,
                        fieldOfStudy = source.fieldOfStudy,
                        url = source.url,
                        daysUntilDeadline = source.daysUntilDeadline,
                        score = item.score?.toDouble()
                    )
                }

                val total = searchResult.total
                val hasMore = (offset + scholarships.size) < total

                if (shouldCache(filter, offset)) {
                    cache.saveScholarships(scholarships)
                }

                Result.success(
                    SearchResult(
                        scholarships = scholarships,
                        total = total,
                        hasMore = hasMore
                    )
                )
            }
        } catch (e: Exception) {
            val cacheFallback = getCachedResult(filter, size, offset)
            if (cacheFallback.isSuccess) {
                cacheFallback
            } else {
                Result.failure(e)
            }
        }
    }

    private fun shouldCache(filter: ScholarshipFilter, offset: Int): Boolean {
        return offset == 0 &&
            filter.keyword.isBlank() &&
            filter.name == null &&
            filter.university == null &&
            filter.fieldOfStudy == null &&
            filter.amount == null &&
            !filter.sortByDeadline
    }

    private fun buildGraphQLFilter(filter: ScholarshipFilter): GqlScholarshipFilter? {
        if (
            filter.name.isNullOrBlank() &&
            filter.university.isNullOrBlank() &&
            filter.fieldOfStudy.isNullOrBlank() &&
            filter.amount.isNullOrBlank()
        ) {
            return null
        }

        return GqlScholarshipFilter(
            name = Optional.presentIfNotNull(filter.name?.takeIf { it.isNotBlank() }),
            university = Optional.presentIfNotNull(filter.university?.takeIf { it.isNotBlank() }),
            field_of_study = Optional.presentIfNotNull(filter.fieldOfStudy?.takeIf { it.isNotBlank() }),
            amount = Optional.presentIfNotNull(filter.amount?.takeIf { it.isNotBlank() })
        )
    }

    private fun buildGraphQLProfile(profile: MatchProfile): UserProfileInput {
        return UserProfileInput(
            name = Optional.presentIfNotNull(profile.name?.takeIf { it.isNotBlank() }),
            university = Optional.presentIfNotNull(profile.universities?.takeIf { it.isNotEmpty() }),
            field_of_study = Optional.presentIfNotNull(profile.fieldOfStudy?.takeIf { it.isNotBlank() }),
            min_amount = Optional.presentIfNotNull(profile.minAmount?.takeIf { it.isNotBlank() }),
            max_amount = Optional.presentIfNotNull(profile.maxAmount?.takeIf { it.isNotBlank() }),
            deadline_after = Optional.presentIfNotNull(profile.deadlineAfter?.takeIf { it.isNotBlank() }),
            deadline_before = Optional.presentIfNotNull(profile.deadlineBefore?.takeIf { it.isNotBlank() })
        )
    }

    private fun mapSortOrder(sortOrder: SortOrder): GqlSortOrder =
        when (sortOrder) {
            SortOrder.ASC -> GqlSortOrder.ASC
            SortOrder.DESC -> GqlSortOrder.DESC
        }

    private suspend fun getCachedResult(
        filter: ScholarshipFilter,
        size: Int,
        offset: Int
    ): Result<SearchResult> {
        val cached = cache.getCachedScholarships()
        return if (cached != null && cache.isCacheValid()) {
            val filtered = cached.filter { scholarship ->
                val matchesKeyword = filter.keyword.isBlank() ||
                    scholarship.name.contains(filter.keyword, ignoreCase = true) ||
                    scholarship.university?.contains(filter.keyword, ignoreCase = true) == true
                val matchesName = filter.name.isNullOrBlank() ||
                    scholarship.name.contains(filter.name, ignoreCase = true)
                val matchesUniversity = filter.university.isNullOrBlank() ||
                    scholarship.university.equals(filter.university, ignoreCase = true)
                val matchesFieldOfStudy = filter.fieldOfStudy.isNullOrBlank() ||
                    scholarship.fieldOfStudy?.contains(filter.fieldOfStudy, ignoreCase = true) == true
                val matchesAmount = filter.amount.isNullOrBlank() ||
                    scholarship.amount?.contains(filter.amount, ignoreCase = true) == true

                matchesKeyword && matchesName && matchesUniversity && matchesFieldOfStudy && matchesAmount
            }

            val sorted = if (filter.sortByDeadline) {
                val ordered = filtered.sortedWith(compareBy { deadlineSortValue(it.daysUntilDeadline) })
                if (filter.sortOrder == SortOrder.DESC) ordered.reversed() else ordered
            } else {
                filtered
            }

            val paginated = sorted.drop(offset).take(size)
            val hasMore = (offset + paginated.size) < sorted.size

            Result.success(
                SearchResult(
                    scholarships = paginated,
                    total = sorted.size,
                    hasMore = hasMore
                )
            )
        } else {
            Result.failure(Exception("No internet connection and no cached data available"))
        }
    }

    private fun deadlineSortValue(days: String?): Int {
        if (days.isNullOrBlank()) return Int.MAX_VALUE
        val trimmed = days.trim()
        val numeric = trimmed.toIntOrNull()
        return when {
            numeric != null -> numeric
            trimmed.equals("expired", ignoreCase = true) -> Int.MAX_VALUE
            else -> Int.MAX_VALUE - 1
        }
    }

    private suspend fun getAuthToken(): String {
        val user = firebaseAuth.currentUser ?: return ""
        return try {
            val tokenResult: GetTokenResult = user.getIdToken(false).await()
            tokenResult.token ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
