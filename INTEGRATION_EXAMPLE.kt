package com.thewalkersoft.rewindphotos.example

import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thewalkersoft.rewindphotos.ui.cleanup.CLEANUP_ROUTE
import com.thewalkersoft.rewindphotos.ui.cleanup.CleanupScreen
import com.thewalkersoft.rewindphotos.ui.cleanup.cleanupScreen

/**
 * Example integration of CleanupScreen into MainActivity
 *
 * This file shows two approaches:
 * 1. Using Navigation with NavGraph
 * 2. Direct UI integration with state management
 */

// ============================================================================
// APPROACH 1: Using Jetpack Navigation (Recommended)
// ============================================================================

@Composable
fun MainScreenWithNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToCleanup = {
                    navController.navigate(CLEANUP_ROUTE)
                }
            )
        }

        // Add cleanup screen to navigation
        cleanupScreen(navController = navController)
    }
}

@Composable
private fun HomeScreen(onNavigateToCleanup: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("RewindPhotos") })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onNavigateToCleanup,
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Find Duplicate Photos")
            }
        }
    }
}

// ============================================================================
// APPROACH 2: Direct Integration with State Management
// ============================================================================

@Composable
fun MainScreenWithDirectIntegration() {
    val (showCleanup, setShowCleanup) = remember { mutableStateOf(false) }

    if (showCleanup) {
        CleanupScreen(
            modifier = Modifier.fillMaxSize()
        )
        // Could add a back button or back press handling here
    } else {
        HomeScreenDirect(
            onNavigateToCleanup = { setShowCleanup(true) }
        )
    }
}

@Composable
private fun HomeScreenDirect(onNavigateToCleanup: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("RewindPhotos") })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onNavigateToCleanup,
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Find Duplicate Photos")
            }
        }
    }
}

// ============================================================================
// PERMISSIONS HANDLING
// ============================================================================

/**
 * Example of how to handle permissions in MainActivity
 */
object PermissionsExample {

    fun setupPermissionHandling(activity: androidx.activity.ComponentActivity) {
        // For Android 13+ (API 33+)
        val readPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        // Request launcher
        val requestPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // Permission granted, can access photos
                // Navigate to CleanupScreen or enable functionality
            } else {
                // Permission denied, show message to user
            }
        }

        // Call this when user clicks "Find Duplicates" button
        fun onFindDuplicatesClicked() {
            when {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    activity,
                    readPermission
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(readPermission)
                }
            }
        }
    }
}

// ============================================================================
// UPDATED AndroidManifest.xml ENTRIES
// ============================================================================

/**
 * Add these permissions to your AndroidManifest.xml:
 *
 * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.DELETE_PHOTOS" />
 *
 * For Android 13+ (Tiramisu):
 * <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
 *
 * Optional (for scoped storage):
 * <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
 */

// ============================================================================
// CONFIGURATION OPTIONS
// ============================================================================

/**
 * To customize the similarity threshold for duplicate detection,
 * you can modify the CleanupViewModel:
 */
object ConfigurationExample {

    // In CleanupViewModel.loadDuplicates():
    fun loadDuplicatesWithCustomThreshold() {
        // Default is 90%, adjust as needed:
        // - Higher value (95%): More strict, fewer matches
        // - Lower value (85%): Less strict, more matches
        val similarityThreshold = 90.0

        // getDuplicatePhotosUseCase(similarityThreshold).collect { ... }
    }
}

// ============================================================================
// EXAMPLE USAGE IN onCreate()
// ============================================================================

/*
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup permissions
        PermissionsExample.setupPermissionHandling(this)

        setContent {
            RewindPhotosTheme {
                // Choose one approach:
                // 1. Navigation-based (recommended)
                MainScreenWithNavigation()

                // 2. Direct integration
                // MainScreenWithDirectIntegration()
            }
        }
    }
}
*/

