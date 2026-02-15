package com.thewalkersoft.rewindphotos.ui.gallery

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * Navigation route for the Gallery screen
 */
const val GALLERY_ROUTE = "gallery"

/**
 * Add Gallery screen to navigation graph
 */
fun NavGraphBuilder.galleryScreen(
    @Suppress("UNUSED_PARAMETER") navController: NavController,
    hasPermission: Boolean = false
) {
    composable(route = GALLERY_ROUTE) {
        GalleryScreen(
            modifier = Modifier,
            hasPermission = hasPermission
        )
    }
}

/**
 * Navigate to Gallery screen
 */
fun NavController.navigateToGallery() {
    navigate(GALLERY_ROUTE)
}
