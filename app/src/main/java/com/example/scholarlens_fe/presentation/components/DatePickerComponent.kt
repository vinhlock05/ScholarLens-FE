package com.example.scholarlens_fe.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Date Picker Component using Material3 DatePickerDialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String?,
    onDateSelected: (String?) -> Unit,
    label: String = "Date",
    placeholder: String = "Select date",
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    error: String? = null,
    initialYear: Int? = null
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
    
    // Parse current value to LocalDate for DatePicker
    val selectedDate = remember(value) {
        value?.let { 
            try {
                com.example.scholarlens_fe.util.DateTimeUtils.parseBirthDate(it)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // Default date: January 1st of initialYear (default 2005) if no value is set
    val defaultDate = remember(initialYear) {
        initialYear?.let { year ->
            LocalDate.of(year, 1, 1)
        } ?: LocalDate.of(2005, 1, 1)
    }
    
    // Display formatted date
    val displayValue = selectedDate?.let { dateFormatter.format(it) } ?: ""

    OutlinedTextField(
        value = displayValue,
        onValueChange = { },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                showDatePicker = true
            },
        enabled = false,
        readOnly = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Select date"
            )
        },
        isError = error != null,
        supportingText = {
            error?.let { Text(it) }
        }
    )

    if (showDatePicker) {
        DatePickerDialogComponent(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { date ->
                if (date != null) {
                    val formattedDate = com.example.scholarlens_fe.util.DateTimeUtils.formatBirthDate(date)
                    onDateSelected(formattedDate)
                } else {
                    onDateSelected(null)
                }
                showDatePicker = false
            },
            initialDate = selectedDate ?: defaultDate
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogComponent(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate?) -> Unit,
    initialDate: LocalDate? = null
) {
    // Use system default timezone for consistency
    // Material3 DatePicker uses system timezone internally
    val systemZone = ZoneId.systemDefault()
    
    // Convert LocalDate to milliseconds using system timezone
    val initialDateMillis = initialDate?.let { date ->
        date.atStartOfDay(systemZone)
            .toInstant()
            .toEpochMilli()
    }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // DatePicker returns milliseconds in system timezone
                        // Convert back to LocalDate using the same system timezone
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(systemZone)
                            .toLocalDate()
                        onDateSelected(date)
                    } ?: onDateSelected(null)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState
        )
    }
}

