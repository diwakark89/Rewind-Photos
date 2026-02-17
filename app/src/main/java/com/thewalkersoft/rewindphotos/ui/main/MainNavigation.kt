package com.thewalkersoft.rewindphotos.ui.main

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * Navigation route for the Main/Rewind screen
 */
const val MAIN_ROUTE = "main"

/**
 * Add Main screen to navigation graph
 */
fun NavGraphBuilder.mainScreen(
    navController: NavController
) {
    composable(route = MAIN_ROUTE) {
        MainScreen(
            modifier = Modifier,
            onSettingsClick = {
                // Navigate to settings if needed
            },
            onPhotoClick = { photoId ->
                // Handle photo click navigation if needed
            }
        )
    }
}

/**
 * Navigate to Main screen
 */
fun NavController.navigateToMain() {
    navigate(MAIN_ROUTE)
}

