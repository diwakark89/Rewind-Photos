package com.thewalkersoft.rewindphotos.ui.cleanup

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.domain.model.PhotoGroup

/**
 * CleanupScreen displays duplicate photo groups and allows users to select
 * and delete similar photos based on perceptual hash similarity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanupScreen(
    modifier: Modifier = Modifier,
    viewModel: CleanupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val groups by viewModel.groups.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Duplicates") }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        when (uiState) {
            is CleanupUiState.Loading -> {
                LoadingState(modifier = Modifier.padding(innerPadding))
            }

            is CleanupUiState.Empty -> {
                EmptyState(
                    modifier = Modifier.padding(innerPadding),
                    onRetry = { viewModel.loadDuplicates() }
                )
            }

            is CleanupUiState.Success -> {
                SuccessState(
                    groups = groups,
                    modifier = Modifier.padding(innerPadding),
                    onPhotoSelected = { groupIndex, photoId ->
                        viewModel.togglePhotoSelection(groupIndex, photoId)
                    },
                    onSelectAll = { groupIndex ->
                        viewModel.selectAllInGroup(groupIndex)
                    },
                    onDeselectAll = { groupIndex ->
                        viewModel.deselectAllInGroup(groupIndex)
                    },
                    onKeepBest = { groupIndex ->
                        viewModel.keepBestInGroup(groupIndex)
                    },
                    onDelete = { viewModel.deleteSelected() }
                )
            }

            is CleanupUiState.Deleting -> {
                LoadingState(
                    modifier = Modifier.padding(innerPadding),
                    message = "Deleting photos..."
                )
            }

            is CleanupUiState.DeleteSuccess -> {
                DeleteSuccessState(
                    deletedCount = (uiState as CleanupUiState.DeleteSuccess).deletedCount,
                    modifier = Modifier.padding(innerPadding),
                    onContinue = { viewModel.loadDuplicates() }
                )
            }

            is CleanupUiState.Error -> {
                ErrorState(
                    error = (uiState as CleanupUiState.Error).message,
                    modifier = Modifier.padding(innerPadding),
                    onRetry = { viewModel.loadDuplicates() }
                )
            }
        }
    }
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier,
    message: String = "Finding duplicate photos..."
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "No Duplicates Found",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your photos look unique!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Scan Again")
            }
        }
    }
}

@Composable
private fun SuccessState(
    groups: List<PhotoGroup>,
    modifier: Modifier = Modifier,
    onPhotoSelected: (groupIndex: Int, photoId: Long) -> Unit,
    onSelectAll: (groupIndex: Int) -> Unit,
    onDeselectAll: (groupIndex: Int) -> Unit,
    onKeepBest: (groupIndex: Int) -> Unit,
    onDelete: () -> Unit
) {
    val totalSelected = groups.sumOf { it.selectedForDeletion.size }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(groups) { groupIndex, group ->
                PhotoGroupCard(
                    group = group,
                    groupIndex = groupIndex,
                    onPhotoSelected = onPhotoSelected,
                    onSelectAll = onSelectAll,
                    onDeselectAll = onDeselectAll,
                    onKeepBest = onKeepBest
                )
            }
        }

        // Bottom action bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$totalSelected selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = onDelete,
                    enabled = totalSelected > 0,
                    modifier = Modifier.padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 8.dp)
                    )
                    Text("Delete Selected")
                }
            }
        }
    }
}

@Composable
private fun PhotoGroupCard(
    group: PhotoGroup,
    groupIndex: Int,
    modifier: Modifier = Modifier,
    onPhotoSelected: (groupIndex: Int, photoId: Long) -> Unit,
    onSelectAll: (groupIndex: Int) -> Unit,
    onDeselectAll: (groupIndex: Int) -> Unit,
    onKeepBest: (groupIndex: Int) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${group.photos.size} Similar Photos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Similarity: ${String.format("%.1f", group.similarity)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    Button(
                        onClick = { onKeepBest(groupIndex) },
                        modifier = Modifier.padding(end = 4.dp),
                        colors = ButtonDefaults.textButtonColors()
                    ) {
                        Text("Keep Best", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = { onSelectAll(groupIndex) },
                        modifier = Modifier.padding(end = 4.dp),
                        colors = ButtonDefaults.textButtonColors()
                    ) {
                        Text("Select All", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = { onDeselectAll(groupIndex) },
                        colors = ButtonDefaults.textButtonColors()
                    ) {
                        Text("Clear", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Photos grid
            val previewPhotos = group.photos.take(2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                previewPhotos.forEach { photo ->
                    PhotoCompareCard(
                        photo = photo,
                        isSelected = group.isPhotoSelected(photo.id),
                        onToggleSelection = { onPhotoSelected(groupIndex, photo.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (previewPhotos.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            if (group.photos.size > 2) {
                Text(
                    text = "+${group.photos.size - 2} more",
                    modifier = Modifier.padding(start = 12.dp, bottom = 12.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PhotoCompareCard(
    photo: Photo,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed.value) 0.97f else 1f,
        label = "PhotoCompareCardScale"
    )

    Card(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .border(
                width = 2.dp,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onToggleSelection()
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = photo.uri,
                contentDescription = "Photo preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentScale = ContentScale.Crop
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                )
            }
        }
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed.value = true
                is PressInteraction.Release -> isPressed.value = false
                is PressInteraction.Cancel -> isPressed.value = false
                else -> {}
            }
        }
    }
}

@Composable
private fun DeleteSuccessState(
    deletedCount: Int,
    modifier: Modifier = Modifier,
    onContinue: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "✓ Success",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$deletedCount photo(s) deleted",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onContinue) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Oops, something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
