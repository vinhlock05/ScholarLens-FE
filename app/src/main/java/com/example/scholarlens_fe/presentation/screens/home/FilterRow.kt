package com.example.scholarlens_fe.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scholarlens_fe.R

/**
 * Filter Row Component
 * Displays filter dropdowns for Country, Funding Level, Scholarship Type, and Field of Study
 */
@Composable
fun FilterRow(
    selectedCountry: String?,
    selectedFundingLevel: String?,
    selectedScholarshipType: String?,
    selectedFieldOfStudy: String?,
    onCountrySelected: (String?) -> Unit,
    onFundingLevelSelected: (String?) -> Unit,
    onScholarshipTypeSelected: (String?) -> Unit,
    onFieldOfStudySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Mock filter options - In real app, these would come from API or ViewModel
    val countries = listOf("UK", "Hà Lan", "Đức", "USA", "Canada", "Australia", "Finland", "Netherlands")
    val fundingLevels = listOf("Toàn phần", "Bán phần", "Học phí", "Living expenses", "Travel")
    val scholarshipTypes = listOf("Master", "PhD", "Bachelor", "Research", "Postdoctoral")
    val fieldsOfStudy = listOf("Engineering", "Computer Science", "Business", "Medicine", "Arts", "Science", "Law")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Country Filter
        FilterDropdown(
            label = "Country",
            selectedValue = selectedCountry,
            options = countries,
            onOptionSelected = onCountrySelected,
            modifier = Modifier.weight(1f)
        )

        // Funding Level Filter
        FilterDropdown(
            label = "Funding",
            selectedValue = selectedFundingLevel,
            options = fundingLevels,
            onOptionSelected = onFundingLevelSelected,
            modifier = Modifier.weight(1f)
        )

        // Scholarship Type Filter
        FilterDropdown(
            label = "Type",
            selectedValue = selectedScholarshipType,
            options = scholarshipTypes,
            onOptionSelected = onScholarshipTypeSelected,
            modifier = Modifier.weight(1f)
        )

        // Field of Study Filter
        FilterDropdown(
            label = "Field",
            selectedValue = selectedFieldOfStudy,
            options = fieldsOfStudy,
            onOptionSelected = onFieldOfStudySelected,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Filter Dropdown Component
 */
@Composable
fun FilterDropdown(
    label: String,
    selectedValue: String?,
    options: List<String>,
    onOptionSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FilterChip(
            selected = selectedValue != null,
            onClick = { expanded = true },
            label = {
                Text(
                    text = selectedValue ?: label,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            // Clear option
            DropdownMenuItem(
                text = { Text("All $label") },
                onClick = {
                    onOptionSelected(null)
                    expanded = false
                }
            )
            Divider()
            // Options
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

