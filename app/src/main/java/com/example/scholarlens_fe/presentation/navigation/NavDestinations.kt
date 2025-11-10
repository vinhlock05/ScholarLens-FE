package com.example.scholarlens_fe.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing all possible navigation destinations in the app
 */
sealed class NavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    // Authentication screens
    data object Login : NavDestination(
        route = "login",
        title = "Login"
    )

    data object Register : NavDestination(
        route = "register",
        title = "Register"
    )

    data object ForgotPassword : NavDestination(
        route = "forgot_password",
        title = "Forgot Password"
    )

    // Main app screens
    data object Home : NavDestination(
        route = "home",
        title = "Home",
        icon = Icons.Default.Home
    )

    data object Search : NavDestination(
        route = "search",
        title = "Search",
        icon = Icons.Default.Search
    )

    data object Profile : NavDestination(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.Person
    )
}

/**
 * List of bottom navigation destinations
 */
val bottomNavDestinations = listOf(
    NavDestination.Home,
    NavDestination.Search,
    NavDestination.Profile
)
