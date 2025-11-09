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
    selectedCountry: String?,
    selectedFundingLevel: String?,
    selectedScholarshipType: String?,
    selectedFieldOfStudy: String?,
    onCountryRemove: () -> Unit,
    onFundingLevelRemove: () -> Unit,
    onScholarshipTypeRemove: () -> Unit,
    onFieldOfStudyRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasFilters = selectedCountry != null || 
                     selectedFundingLevel != null || 
                     selectedScholarshipType != null || 
                     selectedFieldOfStudy != null

    if (hasFilters) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedCountry?.let {
                FilterChip(
                    selected = true,
                    onClick = onCountryRemove,
                    label = { Text("Country: $it") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove country filter"
                        )
                    }
                )
            }

            selectedFundingLevel?.let {
                FilterChip(
                    selected = true,
                    onClick = onFundingLevelRemove,
                    label = { Text("Funding: $it") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove funding level filter"
                        )
                    }
                )
            }

            selectedScholarshipType?.let {
                FilterChip(
                    selected = true,
                    onClick = onScholarshipTypeRemove,
                    label = { Text("Type: $it") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove scholarship type filter"
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
                            contentDescription = "Remove field of study filter"
                        )
                    }
                )
            }
        }
    }
}

