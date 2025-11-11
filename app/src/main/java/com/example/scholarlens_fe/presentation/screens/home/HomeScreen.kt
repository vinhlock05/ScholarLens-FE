package com.example.scholarlens_fe.presentation.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scholarlens_fe.R
import com.example.scholarlens_fe.domain.model.Scholarship
import com.example.scholarlens_fe.presentation.components.FilterChipsRow
import com.example.scholarlens_fe.presentation.components.ScholarshipCardSkeleton
import com.example.scholarlens_fe.util.DateTimeUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val DISPLAY_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val INPUT_DMY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun formatDateOrDash(input: String?): String {
    if (input.isNullOrBlank()) return "-"
    DateTimeUtils.parseIsoToLocalDateTime(input)?.let {
        return it.format(DISPLAY_DATE_FORMATTER)
    }
    return try {
        LocalDate.parse(input, INPUT_DMY_FORMATTER).format(DISPLAY_DATE_FORMATTER)
    } catch (_: DateTimeParseException) {
        input
    }
}

/**
 * Home Screen - Scholarship Browsing
 * Main screen showing scholarship search and list with filters and infinite scroll
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Infinite scroll - load more when scrolled near bottom
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }.collect { lastVisibleIndex ->
            val totalItems = listState.layoutInfo.totalItemsCount
            if (lastVisibleIndex != null &&
                lastVisibleIndex >= totalItems - 3 &&
                uiState.hasMore &&
                !uiState.isLoadingMore &&
                !uiState.isLoading &&
                totalItems > 0
            ) {
                viewModel.loadMore()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Title
        Text(
            text = stringResource(R.string.browse_scholarships),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Search Bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Filter UI temporarily removed as requested

        // Scholarship Count
        if (!uiState.isLoading && !uiState.isEmpty && uiState.scholarships.isNotEmpty()) {
            Text(
                text = stringResource(
                    R.string.scholarships_available,
                    uiState.totalCount
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Content with Infinite Scroll
        when {
            uiState.isLoading && uiState.scholarships.isEmpty() -> {
                // Show skeleton loaders for initial load
                SkeletonLoadingState()
            }
            uiState.error != null && uiState.scholarships.isEmpty() -> {
                // Show error only if no data
                ErrorState(
                    error = uiState.error!!,
                    onRetry = viewModel::retry
                )
            }
            uiState.isEmpty && !uiState.isLoading -> {
                // Show empty state
                EmptyState()
            }
            else -> {
                // Show list with infinite scroll
                ScholarshipListWithInfiniteScroll(
                    scholarships = uiState.scholarships,
                    isLoadingMore = uiState.isLoadingMore,
                    hasMore = uiState.hasMore,
                    listState = listState,
                    onError = if (uiState.error != null) uiState.error else null,
                    onRetry = viewModel::retry
                )
            }
        }
    }
}

/**
 * Search Bar Component
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(stringResource(R.string.search_placeholder))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}

/**
 * Scholarship List Component with Infinite Scroll
 */
@Composable
fun ScholarshipListWithInfiniteScroll(
    scholarships: List<Scholarship>,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onError: String?,
    onRetry: () -> Unit
) {
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = scholarships,
            key = { it.id }
        ) { scholarship ->
            ScholarshipCard(scholarship = scholarship)
        }

        // Show error snackbar at bottom if error occurs during load more
        if (onError != null && scholarships.isNotEmpty()) {
            item {
                ErrorSnackbar(
                    error = onError,
                    onRetry = onRetry,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Show loading more indicator
        if (isLoadingMore) {
            item {
                LoadingMoreIndicator()
            }
        }

        // Show end of list message
        if (!hasMore && scholarships.isNotEmpty()) {
            item {
                EndOfListIndicator()
            }
        }
    }
}

/**
 * Scholarship Card Component
 */
@Composable
fun ScholarshipCard(
    scholarship: Scholarship
) {
    val context = LocalContext.current
    val daysBadge = scholarship.daysUntilDeadline?.let { days ->
        val trimmed = days.trim()
        val parsed = trimmed.toIntOrNull()
        when {
            trimmed.equals("expired", ignoreCase = true) || parsed != null && parsed <= 0 -> {
                stringResource(R.string.deadline_expired) to MaterialTheme.colorScheme.error
            }
            parsed != null -> {
                stringResource(R.string.deadline_days_left, parsed) to MaterialTheme.colorScheme.primary
            }
            else -> trimmed to MaterialTheme.colorScheme.primary
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Scholarship Name + Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = scholarship.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    scholarship.university?.let { university ->
                        Text(
                            text = university,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                IconButton(onClick = { /* TODO: Bookmark functionality */ }) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = stringResource(R.string.bookmark_scholarship),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            daysBadge?.let { (label, color) ->
                Surface(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.End),
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = color,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            scholarship.fieldOfStudy?.let { field ->
                Text(
                    text = field,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            scholarship.amount?.let { amount ->
                Surface(
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = amount,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Application Period
            if (scholarship.openDate != null || scholarship.closeDate != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.application_period),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Dates Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.opens),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
							text = formatDateOrDash(scholarship.openDate),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.closes),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
							text = formatDateOrDash(scholarship.closeDate),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // View Official Page Button
            scholarship.url?.let { website ->
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(website))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.view_official_page),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

/**
 * Skeleton Loading State Component
 * Shows skeleton loaders while loading initial data
 */
@Composable
fun SkeletonLoadingState() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) {
            ScholarshipCardSkeleton()
        }
    }
}

/**
 * Loading More Indicator
 * Shows at bottom of list when loading more items
 */
@Composable
fun LoadingMoreIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.loading_more),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * End of List Indicator
 */
@Composable
fun EndOfListIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_more_scholarships),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Error Snackbar Component
 * Shows error message in list when load more fails
 */
@Composable
fun ErrorSnackbar(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) {
                Text(
                    text = stringResource(R.string.retry),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/**
 * Empty State Component
 */
@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = stringResource(R.string.no_scholarships_found),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.try_different_search),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Error State Component
 */
@Composable
fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = stringResource(R.string.error_loading_scholarships),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

