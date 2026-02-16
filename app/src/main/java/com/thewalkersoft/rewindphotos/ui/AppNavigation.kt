package com.thewalkersoft.rewindphotos.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thewalkersoft.rewindphotos.ui.detail.DetailScreen
import com.thewalkersoft.rewindphotos.ui.main.MainScreen
import com.thewalkersoft.rewindphotos.ui.settings.SettingsScreen

// Navigation Routes
const val MAIN_SCREEN_ROUTE = "main"
const val SETTINGS_SCREEN_ROUTE = "settings"
const val DETAIL_SCREEN_ROUTE = "detail"

/**
 * App Navigation Graph
 *
 * Sets up navigation between the three main screens:
 * - Main Screen: Gallery view with date/year filtering
 * - Settings Screen: App settings and configuration
 * - Detail Screen: Photo selection and deletion mode
 *
 * @param navController Navigation controller for managing screen transitions
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = MAIN_SCREEN_ROUTE
    ) {
        // Main Gallery Screen
        composable(MAIN_SCREEN_ROUTE) {
            MainScreen(
                onSettingsClick = {
                    navController.navigate(SETTINGS_SCREEN_ROUTE)
                },
                onPhotoClick = { _ ->
                    navController.navigate(DETAIL_SCREEN_ROUTE)
                }
            )
        }

        // Settings Screen
        composable(SETTINGS_SCREEN_ROUTE) {
            SettingsScreen()
        }

        // Detail/Delete Screen
        composable(DETAIL_SCREEN_ROUTE) {
            DetailScreen(
                onCancel = {
                    navController.popBackStack()
                },
                onShare = {
                    // Implement share functionality
                    navController.popBackStack()
                },
                onDelete = {
                    // Implement delete functionality
                    navController.popBackStack()
                },
                onSettingsClick = {
                    navController.navigate(SETTINGS_SCREEN_ROUTE)
                }
            )
        }
    }
}

