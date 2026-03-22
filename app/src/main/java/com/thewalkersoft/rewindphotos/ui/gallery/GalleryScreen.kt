package com.thewalkersoft.rewindphotos.ui.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.thewalkersoft.rewindphotos.domain.model.GroupedPhotosData
import com.thewalkersoft.rewindphotos.domain.model.MonthSection
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.ui.preview.PreviewData
import com.thewalkersoft.rewindphotos.ui.theme.LightGray
import com.thewalkersoft.rewindphotos.ui.theme.RewindPhotosTheme
import com.thewalkersoft.rewindphotos.ui.theme.TextDark
import com.thewalkersoft.rewindphotos.ui.theme.TextMedium
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gallery screen displaying photos organized by month sections.
 * Uses LazyColumn with month headers for better organization.
 * Integrates with GalleryViewModel to fetch and display photos from MediaStore.
 *
 * Features:
 * - Displays photos grouped by month with headers
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
                PhotosByMonth(
                    groupedPhotosData = uiState.groupedPhotosData,
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
 * LazyColumn displaying photos grouped by month with headers
 */
@Composable
private fun PhotosByMonth(
    modifier: Modifier = Modifier,
    groupedPhotosData: GroupedPhotosData,
    onPhotoClick: (Photo) -> Unit
) {
    val sections = groupedPhotosData.sections

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LightGray),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        items(
            items = sections,
            key = { section -> "${section.year}_${section.month}" }
        ) { section ->
            GalleryMonthSection(
                monthSection = section,
                onPhotoClick = onPhotoClick
            )
        }
    }
}

/**
 * Month section composable showing month header and photo grid
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun GalleryMonthSection(
    monthSection: MonthSection,
    onPhotoClick: (Photo) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val monthName = monthNames[monthSection.month]
    val context = LocalContext.current
    val imageLoader = context.imageLoader

    val totalPhotos = monthSection.photos.size
    var visiblePhotoCount by remember(monthSection.year, monthSection.month) {
        mutableIntStateOf(kotlin.math.min(12, totalPhotos))
    }

    LaunchedEffect(monthSection.year, monthSection.month, totalPhotos) {
        var currentCount = visiblePhotoCount
        while (currentCount < totalPhotos) {
            delay(32)
            currentCount = kotlin.math.min(currentCount + 12, totalPhotos)
            visiblePhotoCount = currentCount
        }
    }

    // Preload first few photos from this month section when it becomes visible
    LaunchedEffect(monthSection.year, monthSection.month) {
        monthSection.photos.take(6).forEach { photo ->
            try {
                val imageRequest = ImageRequest.Builder(context)
                    .data(photo.uri)
                    .memoryCacheKey("photo_${photo.id}")
                    .diskCacheKey("photo_${photo.id}")
                    .size(200, 200)
                    .build()
                imageLoader.enqueue(imageRequest)
            } catch (_: Exception) {
                // Silent fail - preloading is non-critical
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Month Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$monthName ${monthSection.year}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                if (monthSection.location != null) {
                    Text(
                        text = monthSection.location,
                        fontSize = 14.sp,
                        color = TextMedium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Photo count
            Text(
                text = "${monthSection.photoCount} ${if (monthSection.photoCount == 1) "photo" else "photos"}",
                fontSize = 14.sp,
                color = TextMedium
            )
        }

        // Photo Grid using FlowRow for better layout (not nested lazy)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            maxItemsInEachRow = 3
        ) {
            monthSection.photos.take(visiblePhotoCount).forEach { photo ->
                GalleryPhotoCard(
                    photo = photo,
                    onPhotoClick = { onPhotoClick(photo) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual photo card with Material 3 design.
 * Displays the image using AsyncImage for efficient loading.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GalleryPhotoCard(
    modifier: Modifier = Modifier,
    photo: Photo,
    onPhotoClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = {
                    // Handle video vs photo click differently
                    if (photo.mediaType == com.thewalkersoft.rewindphotos.domain.model.MediaType.VIDEO) {
                        // Launch system video player for videos
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                setDataAndType(photo.uri.toUri(), "video/*")
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.util.Log.e("GalleryScreen", "Failed to open video", e)
                        }
                    } else {
                        // Navigate to photo detail for images
                        onPhotoClick()
                    }
                }
            )
    ) {
        // Async image loading using Coil
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(photo.uri)
                .memoryCacheKey("photo_${photo.id}")
                .diskCacheKey("photo_${photo.id}")
                .crossfade(true)
                .build(),
            contentDescription = "Photo taken on ${formatDate(photo.dateTaken)}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Video indicator overlay
        if (photo.mediaType == com.thewalkersoft.rewindphotos.domain.model.MediaType.VIDEO) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Video",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
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
    } catch (_: Exception) {
        "Unknown date"
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
private fun GalleryScreenPreview() {
    RewindPhotosTheme {
        Surface {
            GalleryScreenContent(
                modifier = Modifier.fillMaxSize(),
                uiState = GalleryUiState.Success(
                    groupedPhotosData = PreviewData.groupedPhotosData
                )
            )
        }
    }
}

