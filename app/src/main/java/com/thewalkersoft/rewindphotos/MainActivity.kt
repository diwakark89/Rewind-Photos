package com.thewalkersoft.rewindphotos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.thewalkersoft.rewindphotos.ui.gallery.GALLERY_ROUTE
import com.thewalkersoft.rewindphotos.ui.gallery.galleryScreen
import com.thewalkersoft.rewindphotos.ui.theme.RewindPhotosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RewindPhotosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    PermissionAwareApp()
                }
            }
        }
    }
}

/**
 * Composable that handles permission requests and displays the app.
 */
@Composable
@Suppress("DEPRECATION")
private fun PermissionAwareApp() {
    val permissionState = remember { mutableStateOf(false) }
    val activity = LocalContext.current as? ComponentActivity
    val lifecycleOwner = LocalLifecycleOwner.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionState.value = isGranted
    }

    // Function to check permission status
    val checkPermission: () -> Unit = remember {
        {
            if (activity != null) {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                } else {
                    null
                }

                if (permission == null) {
                    permissionState.value = true
                } else {
                    val isGranted = ContextCompat.checkSelfPermission(
                        activity,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED

                    if (isGranted) {
                        permissionState.value = true
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissionLauncher.launch(permission)
                    }
                }
            }
        }
    }

    // Initial permission check
    LaunchedEffect(Unit) {
        checkPermission()
    }

    // Listen for lifecycle changes to re-check permissions when app resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Re-check permission when app resumes (user might have granted it in settings)
                checkPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    RewindPhotosApp(hasPermission = permissionState.value)
}

/**
 * Main app navigation setup.
 * Sets up the NavHost and navigation graph for the application.
 */
@Composable
fun RewindPhotosApp(
    navController: NavHostController = rememberNavController(),
    hasPermission: Boolean = false
) {
    NavHost(
        navController = navController,
        startDestination = GALLERY_ROUTE,
        modifier = Modifier.fillMaxSize()
    ) {
        galleryScreen(navController = navController, hasPermission = hasPermission)
    }
}

@Preview(showBackground = true)
@Composable
fun RewindPhotosAppPreview() {
    RewindPhotosTheme {
        RewindPhotosApp(hasPermission = true)
    }
}