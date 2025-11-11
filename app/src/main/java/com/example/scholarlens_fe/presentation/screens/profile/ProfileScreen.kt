package com.example.scholarlens_fe.presentation.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.scholarlens_fe.R
import com.example.scholarlens_fe.presentation.components.DatePickerField
import com.example.scholarlens_fe.presentation.navigation.NavDestination
import com.example.scholarlens_fe.util.DateTimeUtils

/**
 * Profile Screen
 * User profile and settings screen with edit and logout functionality
 * Redesigned with header similar to HomeScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }


    // Navigate to login on logout
    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            navController?.navigate(NavDestination.Login.route) {
                popUpTo(NavDestination.Login.route) { inclusive = true }
            }
        }
    }

    // Show error snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
        }
    }

    // Handle back button when in edit mode
    BackHandler(enabled = uiState.isEditing) {
        viewModel.toggleEditMode()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
//                if (!uiState.isEditing) {
//                    IconButton(onClick = { viewModel.toggleEditMode() }) {
//                        Icon(
//                            Icons.Default.Edit,
//                            contentDescription = "Edit Profile",
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                }
            }

            // Content with scroll
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.isEditing) {
                    // Edit Mode - Scrollable content with fixed buttons at bottom
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            EditBasicInfoContent(
                                uiState = uiState,
                                viewModel = viewModel,
                                showButtons = false
                            )
                        }
                        
                        // Fixed buttons at bottom (edit mode)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.toggleEditMode() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = { viewModel.saveBasicInfo() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                enabled = !uiState.isLoading && 
                                         uiState.displayName.isNotBlank() &&
                                         !uiState.degree.isNullOrBlank() &&
                                         !uiState.university.isNullOrBlank(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.width(24.dp).height(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Save")
                                }
                            }
                        }
                    }
                } else {
                    // View Mode - Normal scrollable content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ViewProfileContent(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                        
                        // Upload CV button (only in view mode)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { /* TODO: Handle CV upload */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Upload New CV")
                        }
                        
                        // Info Banner
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                buildAnnotatedString {
                                    append("Your profile is ready! Check the ")
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Home")
                                    }
                                    append(" tab to see your scholarship matches.")
                                },
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            
            // Logout button at bottom (only in view mode)
            if (!uiState.isEditing) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.signOut() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(bottom = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.logout))
                }
            }
        }
    }
}

@Composable
fun EditBasicInfoContent(
    uiState: ProfileViewModel.ProfileUiState,
    viewModel: ProfileViewModel,
    showButtons: Boolean = true
) {
    // Degree options
    val degrees = listOf("Bachelor's", "Master's", "PhD", "Other")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Edit Basic Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Display Name
            OutlinedTextField(
                value = uiState.displayName,
                onValueChange = viewModel::onDisplayNameChange,
                label = { Text("Display Name") },
                placeholder = { Text("Enter your name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                isError = uiState.displayNameError != null,
                supportingText = {
                    uiState.displayNameError?.let { Text(it) }
                }
            )

            // Degree
            ExpandableFilterSection(
                title = "Degree",
                items = degrees,
                selectedItems = emptyList(),
                onItemToggle = { },
                isSingleSelect = true,
                selectedItem = uiState.degree,
                onItemSelect = { viewModel.onDegreeChange(it) },
                modifier = Modifier.fillMaxWidth(),
                showSearch = false
            )
            
            // University
            OutlinedTextField(
                value = uiState.university ?: "",
                onValueChange = viewModel::onUniversityChange,
                label = { Text("University") },
                placeholder = { Text("Enter your university") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                isError = uiState.universityError != null,
                supportingText = {
                    uiState.universityError?.let { Text(it) } ?: Text("University is required")
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null
                    )
                }
            )

            // Birth Date
            DatePickerField(
                value = uiState.birthDate,
                onDateSelected = viewModel::onBirthDateChange,
                label = "Birth Date",
                placeholder = "Select your birth date",
                modifier = Modifier.fillMaxWidth(),
                initialYear = 2005
            )

            // Save and Cancel buttons (only show if showButtons is true)
            if (showButtons) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.toggleEditMode() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { viewModel.saveBasicInfo() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        enabled = !uiState.isLoading && 
                                 uiState.displayName.isNotBlank() &&
                                 !uiState.degree.isNullOrBlank() &&
                                 !uiState.university.isNullOrBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(24.dp).height(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ViewProfileContent(
    uiState: ProfileViewModel.ProfileUiState,
    viewModel: ProfileViewModel
) {
    // Card 1: Basic Information
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Basic Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.toggleEditMode() }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Avatar and Name Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.user?.photoUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(uiState.user?.photoUrl),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Name and Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.displayName.ifBlank { "User" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Degree and University
                    val degreeText = uiState.degree ?: ""
                    val universityText = uiState.university ?: ""
                    val infoText = listOfNotNull(
                        degreeText.takeIf { it.isNotBlank() },
                        universityText.takeIf { it.isNotBlank() }
                    ).joinToString(" • ")
                    if (infoText.isNotBlank()) {
                        Text(
                            text = infoText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Divider()
            
            // Email
            ProfileInfoRow(
                icon = Icons.Default.Email,
                label = "Email",
                value = uiState.user?.email ?: "N/A"
            )
            
            // Birth Date
            uiState.birthDate?.let { birthDate ->
                val displayDate = try {
                    DateTimeUtils.parseBirthDate(birthDate)?.let {
                        DateTimeUtils.formatDateForDisplay(it)
                    } ?: birthDate
                } catch (e: Exception) {
                    birthDate
                }
                if (displayDate.isNotBlank()) {
                    ProfileInfoRow(
                        icon = null,
                        label = "Birth Date",
                        value = displayDate
                    )
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Card 2: Profile Summary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Profile Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // GPA - Always show if available
            if (!uiState.gpaRange4.isNullOrBlank()) {
                ProfileSummaryItem(
                    icon = Icons.Default.Star,
                    iconColor = Color(0xFF4CAF50), // Green
                    label = "GPA",
                    value = uiState.gpaRange4
                )
            }
            
            // Major / Field of Study - Only show if available
            if (!uiState.fieldOfStudy.isNullOrBlank()) {
                ProfileSummaryItem(
                    icon = Icons.Default.School,
                    iconColor = Color(0xFF2196F3), // Blue
                    label = "Major",
                    value = uiState.fieldOfStudy
                )
            }
            
            // Key Skills - Only show if available in user data (from CV upload via AI)
            // Note: When CV is uploaded, AI will extract keySkills and add to User model and database
            // We only display fields that exist in the user data from getUser or CV upload
            // For now, keySkills field is not in User model, so it won't be displayed
            // TODO: After CV upload feature is implemented, check for keySkills in user data
        }
    }
}

@Composable
fun ProfileSummaryItem(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 4.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: ImageVector?,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        } ?: Spacer(modifier = Modifier.width(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}