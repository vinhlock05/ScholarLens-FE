package com.example.scholarlens_fe.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.scholarlens_fe.presentation.screens.auth.ForgotPasswordScreen
import com.example.scholarlens_fe.presentation.screens.auth.LoginScreen
import com.example.scholarlens_fe.presentation.screens.auth.RegisterScreen
import com.example.scholarlens_fe.presentation.screens.home.HomeScreen
import com.example.scholarlens_fe.presentation.screens.profile.ProfileScreen
import com.example.scholarlens_fe.presentation.screens.profile.ProfileSetupScreen
import com.example.scholarlens_fe.presentation.screens.search.SearchScreen

/**
 * Navigation graph for the app
 * Defines all navigation routes and their corresponding screens
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavDestination.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Authentication screens
        composable(route = NavDestination.Login.route) {
            LoginScreen(
                onLoginSuccess = { isProfileComplete ->
                    if (isProfileComplete) {
                        navController.navigate(NavDestination.Home.route) {
                            popUpTo(NavDestination.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(NavDestination.ProfileSetup.route) {
                            popUpTo(NavDestination.Login.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(NavDestination.Register.route)
                },
//                onForgotPassword = {
//                    navController.navigate(NavDestination.ForgotPassword.route)
//                }
            )
        }

        composable(route = NavDestination.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(NavDestination.ProfileSetup.route) {
                        popUpTo(NavDestination.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = NavDestination.ProfileSetup.route) {
            ProfileSetupScreen(
                onSetupComplete = {
                    // Navigate to Home after profile setup is complete
                    // MainActivity will verify profile completeness and allow navigation
                    navController.navigate(NavDestination.Home.route) {
                        popUpTo(NavDestination.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = NavDestination.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Main app screens
        composable(route = NavDestination.Home.route) {
            HomeScreen()
        }

        composable(route = NavDestination.Search.route) {
            SearchScreen()
        }

        composable(route = NavDestination.Profile.route) {
            ProfileScreen(navController = navController)
        }
    }
}