package com.example.scholarlens_fe.presentation.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scholarlens_fe.presentation.components.DatePickerField

/**
 * Profile Setup Screen
 * Shown after registration to collect basic user information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Common countries list (expanded)
    val countries = listOf(
        "United States", "United Kingdom", "Canada", "Australia", "Germany",
        "France", "Japan", "South Korea", "Singapore", "Netherlands",
        "Sweden", "Switzerland", "New Zealand", "Ireland", "Denmark",
        "Norway", "Finland", "Italy", "Spain", "Belgium", "Austria",
        "Poland", "Portugal", "Greece", "Czech Republic", "Hungary",
        "Taiwan", "Hong Kong", "China", "India", "Malaysia", "Thailand",
        "Brazil", "Mexico", "Argentina", "Chile", "South Africa", "Egypt",
        "Turkey", "Israel", "United Arab Emirates", "Saudi Arabia", "Qatar"
    )

    // Degree options
    val degrees = listOf("Bachelor's", "Master's", "PhD", "Other")

    // Field of study options
    val fieldsOfStudy = listOf(
        "Engineering", "Computer Science", "Business", "Medicine", "Arts",
        "Science", "Law", "Education", "Social Sciences", "Humanities",
        "Agriculture", "Architecture", "Design", "Environmental Science",
        "Mathematics", "Physics", "Chemistry", "Biology", "Psychology",
        "Economics", "Finance", "Marketing", "International Relations",
        "Public Health", "Nursing", "Pharmacy", "Dentistry", "Veterinary Science"
    )

    // Show error snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
        }
    }

    // Navigate on success
    LaunchedEffect(uiState.isSetupComplete) {
        if (uiState.isSetupComplete) {
            onSetupComplete()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Complete Your Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Tell us a bit about yourself to get personalized scholarship recommendations",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Display Name Field
            OutlinedTextField(
                value = uiState.displayName,
                onValueChange = viewModel::onDisplayNameChange,
                label = { Text("Display Name") },
                placeholder = { Text("Enter your name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.displayNameError != null,
                supportingText = {
                    uiState.displayNameError?.let { Text(it) }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Birth Date Field
            DatePickerField(
                value = uiState.birthDate,
                onDateSelected = viewModel::onBirthDateChange,
                label = "Birth Date",
                placeholder = "Select your birth date",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // University Field
            OutlinedTextField(
                value = uiState.university ?: "",
                onValueChange = viewModel::onUniversityChange,
                label = { Text("University") },
                placeholder = { Text("Enter your university") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // GPA Field
            OutlinedTextField(
                value = uiState.gpaRange4 ?: "",
                onValueChange = viewModel::onGpaRangeChange,
                label = { Text("GPA (4.0 scale)") },
                placeholder = { Text("e.g., 3.5") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                isError = uiState.gpaError != null,
                supportingText = {
                    uiState.gpaError?.let { Text(it) } ?: Text("Enter your GPA (0.0 - 4.0)")
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Degree Field
            ExpandableFilterSection(
                title = "Degree",
                items = degrees,
                selectedItems = emptyList(),
                onItemToggle = { },
                isSingleSelect = true,
                selectedItem = uiState.degree,
                onItemSelect = { viewModel.onDegreeChange(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Field of Study Field
            ExpandableFilterSection(
                title = "Field of Study",
                items = fieldsOfStudy,
                selectedItems = emptyList(),
                onItemToggle = { },
                isSingleSelect = true,
                selectedItem = uiState.fieldOfStudy,
                onItemSelect = { viewModel.onFieldOfStudyChange(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Desired Countries Section
            ExpandableFilterSection(
                title = "Desired Countries",
                items = countries,
                selectedItems = uiState.desiredCountries,
                onItemToggle = { viewModel.toggleCountry(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Continue Button
            Button(
                onClick = { viewModel.saveProfile() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading && 
                    uiState.displayName.isNotBlank() &&
                    uiState.desiredCountries.isNotEmpty() &&
                    !uiState.degree.isNullOrBlank() &&
                    !uiState.fieldOfStudy.isNullOrBlank() &&
                    !uiState.gpaRange4.isNullOrBlank() &&
                    uiState.gpaError == null,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(24.dp).height(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Continue", fontSize = 16.sp)
                }
            }
        }
    }
}


