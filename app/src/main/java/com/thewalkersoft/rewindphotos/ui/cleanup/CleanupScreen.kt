package com.thewalkersoft.rewindphotos.ui.cleanup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thewalkersoft.rewindphotos.domain.model.DuplicateGroup
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.ui.theme.RewindPhotosTheme

/**
 * CleanupScreen displays duplicate photo groups and allows users to select
 * and delete similar photos based on perceptual hash similarity.
 */

/**
 * Cleanup screen displaying duplicate photo groups for review and cleanup.
 */
@Composable
fun CleanupScreen(
    modifier: Modifier = Modifier,
    viewModel: CleanupViewModel = hiltViewModel(),
    onPhotoClick: (photoId: Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        CleanupScreenContent(
            uiState = uiState,
            onPhotoClick = onPhotoClick,
            onRetry = { viewModel.retry() }
        )
    }
}

/**
 * Content composable for the cleanup screen.
 * Handles different UI states: Loading, NoDuplicates, DuplicatesFound, Error
 */
@Composable
internal fun CleanupScreenContent(
    modifier: Modifier = Modifier,
    uiState: CleanupUiState,
    onPhotoClick: (photoId: Long) -> Unit = {},
    onRetry: () -> Unit = {}
) {
    when (uiState) {
        is CleanupUiState.Loading -> {
            LoadingState(modifier)
        }

        is CleanupUiState.NoDuplicates -> {
            NoDuplicatesState(modifier)
        }

        is CleanupUiState.DuplicatesFound -> {
            DuplicatesFoundState(
                modifier = modifier,
                uiState = uiState,
                onPhotoClick = onPhotoClick
            )
        }

        is CleanupUiState.Error -> {
            ErrorState(
                modifier = modifier,
                message = uiState.message,
                onRetry = onRetry
            )
        }
    }
}

/**
 * Loading state - shows spinner while analyzing photos
 */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Analyzing photos...",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Detecting duplicates with perceptual hashing",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * No duplicates state - shown when analysis completes with no matches
 */
@Composable
private fun NoDuplicatesState(modifier: Modifier = Modifier) {
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
                text = "No duplicates found!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your photos are all unique",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Duplicates found state - shows list of duplicate groups
 */
@Composable
private fun DuplicatesFoundState(
    modifier: Modifier = Modifier,
    uiState: CleanupUiState.DuplicatesFound,
    onPhotoClick: (photoId: Long) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Summary header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Found ${uiState.groups.size} duplicate groups",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${uiState.potentialSpaceSavings} photos could be removed",
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Groups list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = uiState.groups,
                key = { group -> group.representativePhoto.id }
            ) { group ->
                DuplicateGroupCard(
                    group = group,
                    onPhotoClick = onPhotoClick
                )
            }
        }
    }
}

/**
 * Error state - shown when duplicate detection fails
 */
@Composable
private fun ErrorState(
    modifier: Modifier = Modifier,
    message: String = "Failed to analyze photos",
    onRetry: () -> Unit = {}
) {
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
                text = "Error",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
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
}

// ============= PREVIEW COMPOSABLES =============

private val mockDuplicateGroups = listOf(
    DuplicateGroup(
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
    ),
    DuplicateGroup(
        representativePhoto = Photo(
            id = 4,
            uri = "content://media/external/images/media/4",
            dateTaken = 1707787200000,
            displayPath = "IMG_002.jpg"
        ),
        similarPhotos = listOf(
            Photo(5, "content://media/external/images/media/5", 1707787210000, "IMG_002_dup.jpg")
        ),
        similarityScore = 0.92
    )
)

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun CleanupScreenLoadingPreview() {
    RewindPhotosTheme {
        CleanupScreenContent(
            uiState = CleanupUiState.Loading
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun CleanupScreenNoDuplicatesPreview() {
    RewindPhotosTheme {
        CleanupScreenContent(
            uiState = CleanupUiState.NoDuplicates
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun CleanupScreenDuplicatesFoundPreview() {
    RewindPhotosTheme {
        CleanupScreenContent(
            uiState = CleanupUiState.DuplicatesFound(
                groups = mockDuplicateGroups,
                totalPhotos = 5,
                potentialSpaceSavings = 3
            )
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun CleanupScreenErrorPreview() {
    RewindPhotosTheme {
        CleanupScreenContent(
            uiState = CleanupUiState.Error("Failed to detect duplicates due to storage error")
        )
    }
}
