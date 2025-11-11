package com.example.scholarlens_fe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewModelScope
import com.example.scholarlens_fe.domain.usecase.GetCurrentUserUseCase
import com.example.scholarlens_fe.presentation.navigation.NavDestination
import com.example.scholarlens_fe.presentation.navigation.NavGraph
import com.example.scholarlens_fe.presentation.navigation.bottomNavDestinations
import com.example.scholarlens_fe.ui.theme.ScholarLensFETheme
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main Activity of the application
 * Entry point for the app with bottom navigation setup
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScholarLensFETheme {
                MainScreen()
            }
        }
    }
}

/**
 * ViewModel for MainActivity to check authentication state and profile completeness
 */
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val authRepository: com.example.scholarlens_fe.data.repository.AuthRepository
) : ViewModel() {
    private val _isProfileComplete = MutableStateFlow<Boolean?>(null)
    val isProfileComplete: StateFlow<Boolean?> = _isProfileComplete.asStateFlow()

    fun isUserLoggedIn(): Boolean {
        return getCurrentUserUseCase.isUserLoggedIn()
    }

    /**
     * Check if user profile is complete
     * First checks from storage, then verifies with API if needed
     */
    fun checkProfileComplete() {
        if (!isUserLoggedIn()) {
            _isProfileComplete.value = false
            return
        }

        // Quick check from storage first
        val fromStorage = authRepository.checkProfileCompleteFromStorage()
        
        // If profile seems complete from storage, verify with API
        // If incomplete from storage, no need to call API
        if (fromStorage) {
            viewModelScope.launch {
                try {
                    val fromApi = authRepository.checkCurrentUserProfileComplete()
                    _isProfileComplete.value = fromApi
                } catch (e: Exception) {
                    // If API fails, trust storage
                    _isProfileComplete.value = true
                }
            }
        } else {
            _isProfileComplete.value = false
        }
    }
}

/**
 * Main screen composable with bottom navigation and nav graph
 * Shows bottom navigation only when user is authenticated
 */
@Composable
fun MainScreen(
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Check if user is authenticated
    val isAuthenticated = viewModel.isUserLoggedIn()
    val isProfileComplete by viewModel.isProfileComplete.collectAsState()

    // Determine if bottom navigation should be shown
    val showBottomNav = isAuthenticated && 
                        isProfileComplete == true && 
                        currentDestination?.route in bottomNavDestinations.map { it.route }

    // Check profile completeness on app start
    LaunchedEffect(Unit) {
        if (isAuthenticated) {
            viewModel.checkProfileComplete()
        }
    }

    // Re-check profile when navigating from ProfileSetup to Home (to verify completion)
    LaunchedEffect(currentDestination?.route) {
        // When user completes ProfileSetup and navigates to Home, re-check profile
        if (currentDestination?.route == NavDestination.Home.route && isAuthenticated) {
            viewModel.checkProfileComplete()
        }
    }

    // Navigate based on authentication and profile completeness
    LaunchedEffect(isAuthenticated, isProfileComplete) {
        val currentRoute = currentDestination?.route
        
        when {
            !isAuthenticated -> {
                // Not authenticated - navigate to login (if not already on auth screens)
                if (currentRoute !in listOf(
                        NavDestination.Login.route,
                        NavDestination.Register.route,
                        NavDestination.ForgotPassword.route,
                        NavDestination.ProfileSetup.route
                    )
                ) {
                    navController.navigate(NavDestination.Login.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            }
            isProfileComplete == false -> {
                // Authenticated but profile incomplete - navigate to profile setup
                // Only navigate if not already on ProfileSetup or auth screens
                // This handles the case when user logs in but profile is incomplete
                if (currentRoute != NavDestination.ProfileSetup.route &&
                    currentRoute !in listOf(
                        NavDestination.Login.route,
                        NavDestination.Register.route,
                        NavDestination.ForgotPassword.route
                    )
                ) {
                    navController.navigate(NavDestination.ProfileSetup.route) {
                        popUpTo(NavDestination.Login.route) { inclusive = true }
                    }
                }
            }
            // If profile is complete, allow navigation to main app screens
            // ProfileSetupScreen will handle navigation to Home after completion
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar {
                    bottomNavDestinations.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == destination.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                destination.icon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = destination.title
                                    )
                                }
                            },
                            label = {
                                Text(text = destination.title)
                            },
                            selected = selected,
                            onClick = {
                                // If already on this destination, do nothing
                                if (selected) {
                                    return@NavigationBarItem
                                }
                                navController.navigate(destination.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}