package com.thewalkersoft.rewindphotos.ui.gallery

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.thewalkersoft.rewindphotos.domain.model.MediaType
import com.thewalkersoft.rewindphotos.domain.model.Photo

/**
 * Full-screen photo/video detail screen.
 * Displays a photo in full screen with zoom capability and a close button.
 * For videos, launches the system video player.
 */
@Composable
fun PhotoDetailScreen(
    photo: Photo,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    // If it's a video, launch the system video player immediately and close
    LaunchedEffect(photo) {
        if (photo.mediaType == MediaType.VIDEO) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(android.net.Uri.parse(photo.uri), "video/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                android.util.Log.e("PhotoDetailScreen", "Failed to open video", e)
            }
            onClose()
        }
    }

    // Only show image detail for photos, not videos
    if (photo.mediaType != MediaType.VIDEO) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Full screen photo with loading state
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(photo.uri)
                .memoryCacheKey("photo_${photo.id}")
                .diskCacheKey("photo_${photo.id}")
                .crossfade(true)
                .build(),
            contentDescription = "Full screen photo",
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClose() },
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
        )

        // Close button in top-right corner
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
    } // End of if (photo.mediaType != MediaType.VIDEO)
}

