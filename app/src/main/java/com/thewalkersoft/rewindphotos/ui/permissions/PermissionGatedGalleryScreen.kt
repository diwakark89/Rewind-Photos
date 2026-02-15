package com.thewalkersoft.rewindphotos.ui.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.thewalkersoft.rewindphotos.domain.util.PermissionUtils
import com.thewalkersoft.rewindphotos.ui.gallery.GalleryScreen

/**
 * Composable that handles permission requests and shows appropriate UI.
 * If permission is granted, shows the gallery. Otherwise, shows permission request.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionGatedGalleryScreen(modifier: Modifier = Modifier) {
    val requiredPermission = PermissionUtils.getRequiredPhotoPermission()
    val permissionState = rememberPermissionState(permission = requiredPermission)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        // Request permission on first load if not already granted
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    when {
        permissionState.status.isGranted -> {
            // Permission granted, show gallery
            GalleryScreen(modifier = modifier)
        }

        !permissionState.status.isGranted -> {
            // Permission was asked but denied or not granted
            PermissionDeniedScreen(
                modifier = modifier,
                onOpenSettings = {
                    // Open app settings to allow user to grant permission manually
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                onRetry = {
                    permissionState.launchPermissionRequest()
                }
            )
        }

        else -> {
            // Initial state - show request button
            PermissionRequestScreen(
                modifier = modifier,
                onRequestPermission = {
                    permissionState.launchPermissionRequest()
                }
            )
        }
    }
}

/**
 * Screen showing initial permission request.
 */
@Composable
private fun PermissionRequestScreen(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = "Gallery",
            modifier = Modifier
                .padding(bottom = 24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Access Your Photos",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = PermissionUtils.getPermissionRationale(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text("Grant Permission")
        }
    }
}

/**
 * Screen showing permission denied state with options to retry or open settings.
 */
@Composable
private fun PermissionDeniedScreen(
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = "Gallery",
            modifier = Modifier
                .padding(bottom = 24.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Text(
            text = "Permission Denied",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "We need permission to access your photos. You can enable it in the app settings.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onOpenSettings,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text("Open Settings")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

