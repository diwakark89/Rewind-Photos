package com.thewalkersoft.rewindphotos.ui.cleanup

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * Navigation route for the Cleanup screen
 */
const val CLEANUP_ROUTE = "cleanup"

/**
 * Add Cleanup screen to navigation graph
 */
fun NavGraphBuilder.cleanupScreen(
    navController: NavController
) {
    composable(route = CLEANUP_ROUTE) {
        CleanupScreen(
            modifier = Modifier
        )
    }
}

/**
 * Navigate to Cleanup screen
 */
fun NavController.navigateToCleanup() {
    navigate(CLEANUP_ROUTE)
}

