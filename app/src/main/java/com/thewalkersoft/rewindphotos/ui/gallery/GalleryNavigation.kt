package com.thewalkersoft.rewindphotos.ui.gallery

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

/**
 * Navigation route for the Gallery screen
 */
const val GALLERY_ROUTE = "gallery"

/**
 * Navigation route for the Photo Detail screen
 */
const val PHOTO_DETAIL_ROUTE = "photo_detail"
const val PHOTO_URI_ARG = "photo_uri"
const val PHOTO_DATE_ARG = "photo_date"

/**
 * Add Photo Detail screen to navigation graph
 */
fun NavGraphBuilder.photoDetailScreen(
    navController: NavController
) {
    composable(
        route = "$PHOTO_DETAIL_ROUTE?$PHOTO_URI_ARG={$PHOTO_URI_ARG}&$PHOTO_DATE_ARG={$PHOTO_DATE_ARG}",
        arguments = listOf(
            navArgument(PHOTO_URI_ARG) {
                type = NavType.StringType
                nullable = false
            },
            navArgument(PHOTO_DATE_ARG) {
                type = NavType.LongType
                defaultValue = 0L
            }
        )
    ) { backStackEntry ->
        val photoUri = backStackEntry.arguments?.getString(PHOTO_URI_ARG) ?: ""
        val photoDate = backStackEntry.arguments?.getLong(PHOTO_DATE_ARG) ?: 0L

        val photo = com.thewalkersoft.rewindphotos.domain.model.Photo(
            id = 0,
            uri = photoUri,
            dateTaken = photoDate,
            displayPath = photoUri
        )

        PhotoDetailScreen(
            photo = photo,
            onClose = { navController.navigateUp() }
        )
    }
}

