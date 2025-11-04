package com.example.scholarlens_fe.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.scholarlens_fe.presentation.screens.home.HomeScreen
import com.example.scholarlens_fe.presentation.screens.profile.ProfileScreen
import com.example.scholarlens_fe.presentation.screens.search.SearchScreen

/**
 * Navigation graph for the app
 * Defines all navigation routes and their corresponding screens
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavDestination.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = NavDestination.Home.route) {
            HomeScreen()
        }

        composable(route = NavDestination.Search.route) {
            SearchScreen()
        }

        composable(route = NavDestination.Profile.route) {
            ProfileScreen()
        }
    }
}

