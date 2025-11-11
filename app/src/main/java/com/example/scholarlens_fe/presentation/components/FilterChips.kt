package com.example.scholarlens_fe.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Filter chips row component
 * Displays active filters as chips with remove option
 */
@Composable
fun FilterChipsRow(
    selectedUniversity: String?,
    selectedFieldOfStudy: String?,
    selectedAmount: String?,
    sortByDeadline: Boolean,
    onUniversityRemove: () -> Unit,
    onFieldOfStudyRemove: () -> Unit,
    onAmountRemove: () -> Unit,
    onSortToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasFilters = selectedUniversity != null ||
        selectedFieldOfStudy != null ||
        selectedAmount != null ||
        sortByDeadline

    if (hasFilters) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedUniversity?.let {
                FilterChip(
                    selected = true,
                    onClick = onUniversityRemove,
                    label = { Text("University: $it") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove university filter"
                        )
                    }
                )
            }

            selectedFieldOfStudy?.let {
                FilterChip(
                    selected = true,
                    onClick = onFieldOfStudyRemove,
                    label = { Text("Field: $it") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove field filter"
                        )
                    }
                )
            }

            selectedAmount?.let {
                FilterChip(
                    selected = true,
                    onClick = onAmountRemove,
                    label = { Text("Amount: $it") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove amount filter"
                        )
                    }
                )
            }

            if (sortByDeadline) {
                FilterChip(
                    selected = true,
                    onClick = onSortToggle,
                    label = { Text("Sort: Deadline") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Disable deadline sort"
                        )
                    }
                )
            }
        }
    }
}

