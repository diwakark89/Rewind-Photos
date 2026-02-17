package com.thewalkersoft.rewindphotos

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.thewalkersoft.rewindphotos.ui.cleanup.CleanupScreen
import com.thewalkersoft.rewindphotos.ui.gallery.GALLERY_ROUTE
import com.thewalkersoft.rewindphotos.ui.gallery.GalleryScreen
import com.thewalkersoft.rewindphotos.ui.gallery.PHOTO_DETAIL_ROUTE
import com.thewalkersoft.rewindphotos.ui.gallery.PHOTO_URI_ARG
import com.thewalkersoft.rewindphotos.ui.gallery.PHOTO_DATE_ARG
import com.thewalkersoft.rewindphotos.ui.gallery.photoDetailScreen
import com.thewalkersoft.rewindphotos.ui.selection.SelectionScreen
import com.thewalkersoft.rewindphotos.ui.settings.SettingsScreen
import com.thewalkersoft.rewindphotos.ui.timeline.TimelineScreen
import com.thewalkersoft.rewindphotos.ui.rewind.RewindScreen

private const val REWIND_ROUTE = "rewind"
private const val CLEANUP_ROUTE = "cleanup"
private const val TIMELINE_ROUTE = "timeline"
private const val SETTINGS_ROUTE = "settings"
private const val SELECTION_ROUTE = "selection"

/**
 * Main app navigation setup with bottom navigation.
 * Sets up the NavHost and navigation graph for the application.
 *
 * This composable manages:
 * - Bottom navigation with Gallery and Cleanup tabs
 * - Navigation state with NavHostController
 * - Navigation graph setup
 * - Screen routing
 *
 * @param navController NavHostController for navigation management
 * @param hasPermission Boolean indicating if storage permission is granted
 */
@Composable
fun RewindPhotosApp(
    navController: NavHostController = rememberNavController(),
    hasPermission: Boolean = false
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var selectedTab by remember { mutableStateOf(0) }
    val selectedTabState: State<Int> = rememberUpdatedState(selectedTab)

    LaunchedEffect(currentRoute) {
        selectedTab = when (currentRoute) {
            REWIND_ROUTE -> 0
            TIMELINE_ROUTE -> 1
            GALLERY_ROUTE -> 2
            CLEANUP_ROUTE -> 3
            else -> selectedTabState.value
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (hasPermission) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.History, contentDescription = null) },
                        label = { Text(stringResource(R.string.rewind_nav_label)) },
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            navController.navigate(REWIND_ROUTE) {
                                popUpTo(REWIND_ROUTE) { inclusive = true }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Timeline, contentDescription = null) },
                        label = { Text(stringResource(R.string.timeline_nav_label)) },
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            navController.navigate(TIMELINE_ROUTE) {
                                popUpTo(REWIND_ROUTE)
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) },
                        label = { Text(stringResource(R.string.gallery_nav_label)) },
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            navController.navigate(GALLERY_ROUTE) {
                                popUpTo(REWIND_ROUTE)
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.CleaningServices, contentDescription = null) },
                        label = { Text(stringResource(R.string.cleanup_nav_label)) },
                        selected = selectedTab == 3,
                        onClick = {
                            selectedTab = 3
                            navController.navigate(CLEANUP_ROUTE) {
                                popUpTo(REWIND_ROUTE)
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = REWIND_ROUTE,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(REWIND_ROUTE) {
                RewindScreen()
            }

            // Timeline Screen - New main screen with year filtering
            composable(TIMELINE_ROUTE) {
                TimelineScreen(
                    onSettingsClick = {
                        navController.navigate(SETTINGS_ROUTE)
                    },
                    onPhotoClick = { photoId ->
                        // Navigate to photo detail if needed
                        // navController.navigate("$PHOTO_DETAIL_ROUTE?photoId=$photoId")
                    },
                    onPhotosLongPress = {
                        // Enter selection mode
                        navController.navigate(SELECTION_ROUTE)
                    }
                )
            }

            // Settings Screen - App settings
            composable(SETTINGS_ROUTE) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Selection Screen - Multi-select mode for photos
            composable(SELECTION_ROUTE) {
                SelectionScreen(
                    onExitSelectionMode = {
                        navController.popBackStack()
                    },
                    onShareSelected = { photoIds ->
                        // Handle share functionality
                        // TODO: Implement share intent with selected photos
                        navController.popBackStack()
                    },
                    onDeleteSelected = { photoIds ->
                        // Handle delete functionality
                        // TODO: Show confirmation dialog, then delete photos
                        navController.popBackStack()
                    }
                )
            }

            // Gallery Screen - Grid view of all photos
            composable(GALLERY_ROUTE) {
                GalleryScreen(
                    hasPermission = hasPermission,
                    onPhotoClick = { photo ->
                        navController.navigate("$PHOTO_DETAIL_ROUTE?$PHOTO_URI_ARG=${photo.uri}&$PHOTO_DATE_ARG=${photo.dateTaken}")
                    }
                )
            }

            // Cleanup Screen - Duplicate photo management
            composable(CLEANUP_ROUTE) {
                CleanupScreen()
            }

            // Photo Detail Screen - Full screen photo viewer
            photoDetailScreen(navController = navController)
        }
    }
}
