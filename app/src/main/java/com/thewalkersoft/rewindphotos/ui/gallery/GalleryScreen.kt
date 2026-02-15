package com.thewalkersoft.rewindphotos.ui.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.ui.photos.PhotosUiState
import com.thewalkersoft.rewindphotos.ui.photos.PhotosViewModel
import com.thewalkersoft.rewindphotos.ui.theme.RewindPhotosTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gallery screen displaying a grid of photos.
 * Uses LazyVerticalGrid for efficient scrolling with 3 columns.
 * Integrates with PhotosViewModel to fetch and display photos from MediaStore.
 */
@Composable
fun GalleryScreen(
    modifier: Modifier = Modifier,
    viewModel: PhotosViewModel = hiltViewModel(),
    hasPermission: Boolean = false,
    onPhotoClick: (Photo) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    if (!hasPermission) {
        // Show permission denied state
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Permission Required",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Storage permission is needed to display your photos",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        GalleryScreenContent(
            modifier = modifier,
            uiState = uiState,
            onPhotoClick = onPhotoClick,
            onRetry = { viewModel.retry() }
        )
    }
}

/**
 * Content composable for the gallery screen.
 * Handles different UI states: Loading, Empty, Success, and Error.
 */
@Composable
private fun GalleryScreenContent(
    modifier: Modifier = Modifier,
    uiState: PhotosUiState,
    onPhotoClick: (Photo) -> Unit = {},
    onRetry: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is PhotosUiState.Loading -> {
                LoadingState()
            }

            is PhotosUiState.Empty -> {
                EmptyState()
            }

            is PhotosUiState.Success -> {
                PhotoGrid(
                    photos = uiState.photos,
                    onPhotoClick = onPhotoClick,
                    modifier = Modifier.fillMaxSize()
                )
            }

            is PhotosUiState.Error -> {
                ErrorState(message = uiState.message, onRetry = onRetry)
            }
        }
    }
}

/**
 * Displays a loading spinner while photos are being fetched.
 */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Loading photos...",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Displays a message when no photos are available.
 */
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No photos found",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Your gallery will appear here",
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Displays an error message with retry option.
 */
@Composable
private fun ErrorState(
    modifier: Modifier = Modifier,
    message: String = "An error occurred while loading photos",
    onRetry: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Retry",
            modifier = Modifier
                .padding(top = 16.dp)
                .clickable { onRetry() },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * LazyVerticalGrid displaying photos in 3 columns.
 * Each photo is displayed as a Material 3 Card with async image loading.
 */
@Composable
private fun PhotoGrid(
    modifier: Modifier = Modifier,
    photos: List<Photo>,
    onPhotoClick: (Photo) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = photos,
            key = { photo -> photo.id }
        ) { photo ->
            PhotoCard(
                photo = photo,
                onPhotoClick = { onPhotoClick(photo) }
            )
        }
    }
}

/**
 * Individual photo card with Material 3 design.
 * Displays the image using Coil's AsyncImage for efficient loading.
 */
@Composable
private fun PhotoCard(
    modifier: Modifier = Modifier,
    photo: Photo,
    onPhotoClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPhotoClick() }
            .size(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Async image loading using Coil
            AsyncImage(
                model = photo.uri,
                contentDescription = "Photo taken on ${formatDate(photo.dateTaken)}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * Formats a timestamp to a readable date string.
 */
private fun formatDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "Unknown date"
    }
}

// ============= PREVIEW COMPOSABLES WITH MOCK DATA =============

/**
 * Mock data for preview
 */
private val mockPhotos = listOf(
    Photo(1, "content://media/external/images/media/1", 1707873600000, "/storage/emulated/0/DCIM/Camera/photo1.jpg"),
    Photo(2, "content://media/external/images/media/2", 1707787200000, "/storage/emulated/0/DCIM/Camera/photo2.jpg"),
    Photo(3, "content://media/external/images/media/3", 1707700800000, "/storage/emulated/0/DCIM/Camera/photo3.jpg"),
    Photo(4, "content://media/external/images/media/4", 1707614400000, "/storage/emulated/0/DCIM/Camera/photo4.jpg"),
    Photo(5, "content://media/external/images/media/5", 1707528000000, "/storage/emulated/0/DCIM/Camera/photo5.jpg"),
    Photo(6, "content://media/external/images/media/6", 1707441600000, "/storage/emulated/0/DCIM/Camera/photo6.jpg"),
    Photo(7, "content://media/external/images/media/7", 1707355200000, "/storage/emulated/0/DCIM/Camera/photo7.jpg"),
    Photo(8, "content://media/external/images/media/8", 1707268800000, "/storage/emulated/0/DCIM/Camera/photo8.jpg"),
    Photo(9, "content://media/external/images/media/9", 1707182400000, "/storage/emulated/0/DCIM/Camera/photo9.jpg"),
)

/**
 * Preview of the gallery screen with mock data in success state
 */
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun GalleryScreenLoadingPreview() {
    RewindPhotosTheme {
        GalleryScreenContent(
            modifier = Modifier.fillMaxSize(),
            uiState = PhotosUiState.Loading
        )
    }
}

/**
 * Preview of the gallery screen with mock data
 */
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun GalleryScreenSuccessPreview() {
    RewindPhotosTheme {
        GalleryScreenContent(
            modifier = Modifier.fillMaxSize(),
            uiState = PhotosUiState.Success(photos = mockPhotos)
        )
    }
}

/**
 * Preview of the gallery screen in empty state
 */
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun GalleryScreenEmptyPreview() {
    RewindPhotosTheme {
        GalleryScreenContent(
            modifier = Modifier.fillMaxSize(),
            uiState = PhotosUiState.Empty
        )
    }
}

/**
 * Preview of the gallery screen in error state
 */
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun GalleryScreenErrorPreview() {
    RewindPhotosTheme {
        GalleryScreenContent(
            modifier = Modifier.fillMaxSize(),
            uiState = PhotosUiState.Error(message = "Failed to load photos from device")
        )
    }
}

/**
 * Preview of individual photo card
 */
@Preview(showBackground = true)
@Composable
fun PhotoCardPreview() {
    RewindPhotosTheme {
        PhotoCard(
            modifier = Modifier.size(120.dp),
            photo = mockPhotos[0]
        )
    }
}
