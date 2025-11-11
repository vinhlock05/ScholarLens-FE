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
    selectedUniversity: String?,
    selectedFieldOfStudy: String?,
    selectedAmount: String?,
    sortByDeadline: Boolean,
    onUniversitySelected: (String?) -> Unit,
    onFieldOfStudySelected: (String?) -> Unit,
    onAmountSelected: (String?) -> Unit,
    onSortToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val universities = listOf("MIT", "Harvard", "Stanford", "Oxford", "Cambridge", "UC Berkeley")
    val fieldsOfStudy = listOf("Engineering", "Computer Science", "Business", "Medicine", "Arts", "Science", "Law")
    val amountRanges = listOf(
        "< 1,000 USD",
        "1,000 - 5,000 USD",
        "5,000 - 10,000 USD",
        "10,000 - 20,000 USD",
        "> 20,000 USD"
    )
    val universityLabel = stringResource(id = R.string.filter_university)
    val fieldLabel = stringResource(id = R.string.filter_field)
    val amountLabel = stringResource(id = R.string.filter_amount)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterDropdown(
            label = universityLabel,
            selectedValue = selectedUniversity,
            options = universities,
            onOptionSelected = onUniversitySelected,
            modifier = Modifier.weight(1f)
        )

        FilterDropdown(
            label = fieldLabel,
            selectedValue = selectedFieldOfStudy,
            options = fieldsOfStudy,
            onOptionSelected = onFieldOfStudySelected,
            modifier = Modifier.weight(1f)
        )

        FilterDropdown(
            label = amountLabel,
            selectedValue = selectedAmount,
            options = amountRanges,
            onOptionSelected = onAmountSelected,
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = sortByDeadline,
            onClick = { onSortToggle(!sortByDeadline) },
            label = {
                Text(
                    text = if (sortByDeadline) "Deadline ↑" else "Deadline",
                    style = MaterialTheme.typography.labelSmall
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null
                )
            },
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
                text = { Text(stringResource(id = R.string.filter_all, label)) },
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

