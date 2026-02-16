package com.thewalkersoft.rewindphotos.ui.cleanup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.thewalkersoft.rewindphotos.domain.model.DuplicateGroup
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.ui.theme.RewindPhotosTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Displays a group of duplicate photos in a horizontal row.
 */
@Composable
fun DuplicateGroupCard(
    modifier: Modifier = Modifier,
    group: DuplicateGroup,
    onPhotoClick: (photoId: Long) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${group.totalPhotos} similar photos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${group.similarityPercentage}% match",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PhotoThumbnail(
                    photo = group.representativePhoto,
                    isBest = true,
                    onPhotoClick = { onPhotoClick(group.representativePhoto.id) }
                )
            }

            items(
                items = group.similarPhotos,
                key = { photo -> photo.id }
            ) { photo ->
                PhotoThumbnail(
                    photo = photo,
                    isBest = false,
                    onPhotoClick = { onPhotoClick(photo.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

/**
 * Individual photo thumbnail with scale animation and best indicator.
 */
@Composable
private fun PhotoThumbnail(
    modifier: Modifier = Modifier,
    photo: Photo,
    isBest: Boolean = false,
    onPhotoClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .clickable(
                onClickLabel = if (isBest) "Best photo" else "Similar photo",
                onClick = onPhotoClick
            )
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = if (isBest) CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ) else CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            SubcomposeAsyncImage(
                model = photo.uri,
                contentDescription = "Photo from ${formatDate(photo.dateTaken)}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {}
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error", style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
        }

        if (isBest) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Best",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        "Best",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "Unknown"
    }
}

private val mockDuplicateGroup = DuplicateGroup(
    representativePhoto = Photo(
        id = 1,
        uri = "content://media/external/images/media/1",
        dateTaken = 1707873600000,
        displayPath = "IMG_001.jpg"
    ),
    similarPhotos = listOf(
        Photo(2, "content://media/external/images/media/2", 1707873610000, "IMG_001_copy.jpg"),
        Photo(3, "content://media/external/images/media/3", 1707873620000, "IMG_001_compressed.jpg")
    ),
    similarityScore = 0.94
)

@Preview(showBackground = true)
@Composable
fun DuplicateGroupCardPreview() {
    RewindPhotosTheme {
        DuplicateGroupCard(group = mockDuplicateGroup)
    }
}

@Preview(showBackground = true)
@Composable
fun PhotoThumbnailPreview() {
    RewindPhotosTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PhotoThumbnail(
                photo = mockDuplicateGroup.representativePhoto,
                isBest = true
            )
            PhotoThumbnail(
                photo = mockDuplicateGroup.similarPhotos[0],
                isBest = false
            )
        }
    }
}

