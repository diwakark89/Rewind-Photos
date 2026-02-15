package com.thewalkersoft.rewindphotos.domain.util

import android.Manifest
import android.os.Build

/**
 * Utility object for managing photo access permissions.
 * Handles differences between Android 13+ and older versions.
 */
object PermissionUtils {
    /**
     * Get the required permission for accessing photos based on Android version.
     *
     * - Android 13+: READ_MEDIA_IMAGES (fine-grained access)
     * - Android 12 and below: READ_EXTERNAL_STORAGE (broad access)
     */
    fun getRequiredPhotoPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    /**
     * Check if we need to show the permission rationale.
     * This is used when the user previously denied the permission.
     */
    fun shouldShowPermissionRationale(permission: String): Boolean {
        return permission == getRequiredPhotoPermission()
    }

    /**
     * Get human-readable description of why we need this permission.
     */
    fun getPermissionRationale(): String {
        return "This app needs permission to access your photos to display them in the gallery."
    }
}

