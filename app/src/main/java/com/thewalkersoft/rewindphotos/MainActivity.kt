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
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.thewalkersoft.rewindphotos.ui.theme.RewindPhotosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RewindPhotosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PermissionAwareApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * Composable that handles permission requests and displays the app.
 * For Android 13+, requests both READ_MEDIA_IMAGES and READ_MEDIA_VIDEO permissions.
 */
@Composable
@Suppress("DEPRECATION")
private fun PermissionAwareApp(modifier: Modifier = Modifier) {
    val permissionState = remember { mutableStateOf(false) }
    val activity = LocalContext.current as? ComponentActivity
    val lifecycleOwner = LocalLifecycleOwner.current

    // For Android 13+, we need to request multiple permissions
    val requestMultiplePermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if all required permissions are granted
        permissionState.value = permissions.values.all { it }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionState.value = isGranted
    }

    // Function to check permission status
    val checkPermission: () -> Unit = remember {
        {
            if (activity != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13+ requires separate permissions for images and videos
                    val imagePermission = Manifest.permission.READ_MEDIA_IMAGES
                    val videoPermission = Manifest.permission.READ_MEDIA_VIDEO

                    val hasImagePermission = ContextCompat.checkSelfPermission(
                        activity,
                        imagePermission
                    ) == PackageManager.PERMISSION_GRANTED

                    val hasVideoPermission = ContextCompat.checkSelfPermission(
                        activity,
                        videoPermission
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasImagePermission && hasVideoPermission) {
                        permissionState.value = true
                    } else {
                        // Request both permissions
                        requestMultiplePermissionsLauncher.launch(
                            arrayOf(imagePermission, videoPermission)
                        )
                    }
                } else {
                    // Android 12 and below use READ_EXTERNAL_STORAGE
                    val permission = Manifest.permission.READ_EXTERNAL_STORAGE
                    val isGranted = ContextCompat.checkSelfPermission(
                        activity,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED

                    if (isGranted) {
                        permissionState.value = true
                    } else {
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

    RewindPhotosApp(hasPermission = permissionState.value, modifier = modifier)
}


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalAnimationApi::class)
@Preview(showBackground = true)
@Composable
fun RewindPhotosAppPreview() {
    RewindPhotosTheme {
        // Preview shows the permission screen instead of full app to avoid Hilt ViewModel issues
        Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RewindPhotos App Preview\n\nNote: Full app preview requires Hilt context.\nUse individual screen previews instead.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}