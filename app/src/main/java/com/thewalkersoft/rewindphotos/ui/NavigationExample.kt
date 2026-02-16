package com.thewalkersoft.rewindphotos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thewalkersoft.rewindphotos.ui.selection.SelectionScreen
import com.thewalkersoft.rewindphotos.ui.settings.SettingsScreen
import com.thewalkersoft.rewindphotos.ui.timeline.TimelineScreen

/**
 * Navigation Example for the Three UI Screens
 *
 * This composable demonstrates how to integrate the three main UI screens:
 * 1. TimelineScreen - Main memories timeline view
 * 2. SettingsScreen - App settings
 * 3. SelectionScreen - Multi-select mode for photos
 *
 * Usage in MainActivity:
 * ```kotlin
 * class MainActivity : ComponentActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContent {
 *             RewindPhotosTheme {
 *                 NavigationExample()
 *             }
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun NavigationExample() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "timeline"
    ) {
        // Main Timeline Screen
        composable("timeline") {
            TimelineScreen(
                onSettingsClick = {
                    navController.navigate("settings")
                },
                onPhotoClick = { photoId ->
                    // Handle photo click - navigate to detail view
                    // navController.navigate("photo_detail/$photoId")
                },
                onPhotosLongPress = {
                    // Enter selection mode
                    navController.navigate("selection")
                }
            )
        }

        // Settings Screen
        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Selection/Delete Mode Screen
        composable("selection") {
            SelectionScreen(
                onExitSelectionMode = {
                    navController.popBackStack()
                },
                onShareSelected = { photoIds ->
                    // Handle share functionality
                    // Example: Create share intent with selected photos
                    println("Sharing ${photoIds.size} photos: $photoIds")
                    navController.popBackStack()
                },
                onDeleteSelected = { photoIds ->
                    // Handle delete functionality
                    // Example: Show confirmation dialog, then delete
                    println("Deleting ${photoIds.size} photos: $photoIds")
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Alternative: Stateful Navigation with Selection State
 *
 * This approach maintains selection state at a higher level,
 * allowing for more complex state management.
 */
@Composable
fun StatefulNavigationExample() {
    val navController = rememberNavController()
    var isSelectionMode by remember { mutableStateOf(false) }

    if (isSelectionMode) {
        // Show selection screen
        SelectionScreen(
            onExitSelectionMode = {
                isSelectionMode = false
            },
            onShareSelected = { photoIds ->
                // Handle share
                isSelectionMode = false
            },
            onDeleteSelected = { photoIds ->
                // Handle delete
                isSelectionMode = false
            }
        )
    } else {
        // Show normal navigation
        NavHost(
            navController = navController,
            startDestination = "timeline"
        ) {
            composable("timeline") {
                TimelineScreen(
                    onSettingsClick = {
                        navController.navigate("settings")
                    },
                    onPhotoClick = { photoId ->
                        // Navigate to detail
                    },
                    onPhotosLongPress = {
                        isSelectionMode = true
                    }
                )
            }

            composable("settings") {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

/**
 * Preview Functions for Android Studio
 *
 * These allow you to preview each screen in Android Studio's Compose Preview.
 */

// Uncomment to enable previews
/*
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewTimelineScreen() {
    RewindPhotosTheme {
        TimelineScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewSettingsScreen() {
    RewindPhotosTheme {
        SettingsScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewSelectionScreen() {
    RewindPhotosTheme {
        SelectionScreen()
    }
}
*/

