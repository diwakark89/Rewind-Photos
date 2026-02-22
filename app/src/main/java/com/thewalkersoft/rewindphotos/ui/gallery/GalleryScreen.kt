package com.thewalkersoft.rewindphotos.ui.gallery

import androidx.compose.foundation.background
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
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.ui.theme.RewindPhotosTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gallery screen displaying a grid of photos.
 * Uses LazyVerticalGrid for efficient scrolling with 3 columns.
 * Integrates with GalleryViewModel to fetch and display photos from MediaStore.
 *
 * Features:
 * - Displays photos in a 3-column grid layout
 * - Shows newest photos first (sorted by date taken descending)
 * - Lazy loading with stable keys for optimal performance
 * - Material 3 card design with proper spacing
 * - Handles permission denial gracefully
 * - Supports navigation on photo click
 */
@Composable
fun GalleryScreen(
    modifier: Modifier = Modifier,
    viewModel: GalleryViewModel = hiltViewModel(),
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
internal fun GalleryScreenContent(
    modifier: Modifier = Modifier,
    uiState: GalleryUiState,
    onPhotoClick: (Photo) -> Unit = {},
    onRetry: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is GalleryUiState.Loading -> {
                LoadingState()
            }

            is GalleryUiState.Empty -> {
                EmptyState()
            }

            is GalleryUiState.Success -> {
                PhotoGrid(
                    photos = uiState.photos,
                    onPhotoClick = onPhotoClick,
                    modifier = Modifier.fillMaxSize()
                )
            }

            is GalleryUiState.Error -> {
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
 * Displays the image using Coil's SubcomposeAsyncImage for efficient loading with progress indicator.
 */
@Composable
private fun PhotoCard(
    modifier: Modifier = Modifier,
    photo: Photo,
    onPhotoClick: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(modifier = modifier.size(120.dp)) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onPhotoClick() },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Async image loading using Coil with progress indicator and cache keys
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(photo.uri)
                        .memoryCacheKey("photo_${photo.id}")
                        .diskCacheKey("photo_${photo.id}")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Photo taken on ${formatDate(photo.dateTaken)}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Failed to load",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                )
            }
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
 * Mock data for gallery preview testing
 */
internal val GalleryGridPreviewMockPhotos = listOf(
    Photo(1, "content://media/external/images/media/1", 1707873600000, "/storage/emulated/0/DCIM/Camera/IMG_20240214_085015.jpg"),
    Photo(2, "content://media/external/images/media/2", 1707787200000, "/storage/emulated/0/DCIM/Camera/IMG_20240213_143022.jpg"),
    Photo(3, "content://media/external/images/media/3", 1707700800000, "/storage/emulated/0/DCIM/Camera/IMG_20240212_092556.jpg"),
    Photo(4, "content://media/external/images/media/4", 1707614400000, "/storage/emulated/0/DCIM/Camera/IMG_20240211_175430.jpg"),
    Photo(5, "content://media/external/images/media/5", 1707528000000, "/storage/emulated/0/DCIM/Camera/IMG_20240210_063141.jpg"),
    Photo(6, "content://media/external/images/media/6", 1707441600000, "/storage/emulated/0/DCIM/Camera/IMG_20240209_154522.jpg"),
    Photo(7, "content://media/external/images/media/7", 1707355200000, "/storage/emulated/0/DCIM/Camera/IMG_20240208_130015.jpg"),
    Photo(8, "content://media/external/images/media/8", 1707268800000, "/storage/emulated/0/DCIM/Camera/IMG_20240207_091842.jpg"),
    Photo(9, "content://media/external/images/media/9", 1707182400000, "/storage/emulated/0/DCIM/Camera/IMG_20240206_175333.jpg"),
)

/**
 * Preview of the gallery grid in loading state
 */
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun GalleryGridLoadingPreview() {
    RewindPhotosTheme {
        GalleryScreenContent(
            modifier = Modifier.fillMaxSize(),
            uiState = GalleryUiState.Loading
        )
    }
}

/**
 * Preview of the gallery grid with mock photos in success state
 */
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun GalleryGridSuccessPreview() {
    RewindPhotosTheme {
        GalleryScreenContent(
            modifier = Modifier.fillMaxSize(),
            uiState = GalleryUiState.Success(photos = GalleryGridPreviewMockPhotos)
        )
    }
}

/**
 * Preview of the gallery grid in empty state
 */
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun GalleryGridEmptyPreview() {
    RewindPhotosTheme {
        GalleryScreenContent(
            modifier = Modifier.fillMaxSize(),
            uiState = GalleryUiState.Empty
        )
    }
}

/**
 * Preview of the gallery grid in error state
 */
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun GalleryGridErrorPreview() {
    RewindPhotosTheme {
        GalleryScreenContent(
            modifier = Modifier.fillMaxSize(),
            uiState = GalleryUiState.Error(message = "Failed to load photos from device storage")
        )
    }
}

/**
 * Preview of individual photo card
 */
@Preview(showBackground = true)
@Composable
fun GalleryPhotoCardPreview() {
    RewindPhotosTheme {
        PhotoCard(
            modifier = Modifier.size(120.dp),
            photo = GalleryGridPreviewMockPhotos[0]
        )
    }
}

