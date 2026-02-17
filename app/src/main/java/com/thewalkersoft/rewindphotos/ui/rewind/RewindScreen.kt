package com.thewalkersoft.rewindphotos.ui.rewind
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.thewalkersoft.rewindphotos.R
import com.thewalkersoft.rewindphotos.domain.model.MemoryGroup
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.ui.theme.RewindPhotosTheme
import kotlin.math.max
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RewindScreen(
    modifier: Modifier = Modifier,
    viewModel: RewindViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedPhoto by rememberSaveable { mutableStateOf<Photo?>(null) }
    RewindScreenContent(
        uiState = uiState,
        selectedPhoto = selectedPhoto,
        onPhotoSelected = { selectedPhoto = it },
        onBack = { selectedPhoto = null },
        modifier = modifier
    )
}
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalAnimationApi::class)
@Composable
private fun RewindScreenContent(
    uiState: RewindUiState,
    selectedPhoto: Photo?,
    onPhotoSelected: (Photo) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = selectedPhoto,
            transitionSpec = { fadeIn() with fadeOut() },
            label = "rewind-detail-transition"
        ) { targetPhoto ->
            if (targetPhoto == null) {
                RewindListContent(
                    uiState = uiState,
                    onPhotoSelected = onPhotoSelected,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this
                )
            } else {
                DetailScreen(
                    photo = targetPhoto,
                    onBack = onBack,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this
                )
            }
        }
    }
}
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RewindListContent(
    uiState: RewindUiState,
    onPhotoSelected: (Photo) -> Unit,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.rewind_screen_padding)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.memoryGroups.isEmpty() -> {
            EmptyState(modifier = modifier)
        }
        else -> {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn()
            ) {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.rewind_screen_padding)),
                    verticalArrangement = Arrangement.spacedBy(
                        dimensionResource(R.dimen.rewind_section_spacing)
                    )
                ) {
                    items(
                        items = uiState.memoryGroups,
                        key = { it.year }
                    ) { group ->
                        MemoryGroupSection(
                            group = group,
                            onPhotoSelected = onPhotoSelected,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MemoryGroupSection(
    group: MemoryGroup,
    onPhotoSelected: (Photo) -> Unit,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.rewind_header_spacing)
        )
    ) {
        Text(
            text = pluralStringResource(
                R.plurals.rewind_years_ago,
                group.yearsAgo,
                group.yearsAgo
            ),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        AdaptivePhotoGrid(
            photos = group.photos,
            onPhotoSelected = onPhotoSelected,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Suppress("UnusedBoxWithConstraintsScope") // maxWidth is used in remember calculation
private fun AdaptivePhotoGrid(
    photos: List<Photo>,
    onPhotoSelected: (Photo) -> Unit,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    if (photos.isEmpty()) return

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val minCellSize = dimensionResource(R.dimen.rewind_photo_min_size)
        // Calculate columns based on available width from BoxWithConstraintsScope
        val columns = remember(maxWidth, minCellSize) {
            max(1, (maxWidth / minCellSize).toInt())
        }
        val rows by remember(photos, columns) {
            derivedStateOf { photos.chunked(columns) }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.rewind_grid_spacing)
            )
        ) {
            rows.forEach { rowPhotos ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        dimensionResource(R.dimen.rewind_grid_spacing)
                    )
                ) {
                    rowPhotos.forEach { photo ->
                        PhotoCard(
                            photo = photo,
                            onPhotoSelected = onPhotoSelected,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                    if (rowPhotos.size < columns) {
                        repeat(columns - rowPhotos.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PhotoCard(
    photo: Photo,
    onPhotoSelected: (Photo) -> Unit,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    with(sharedTransitionScope) {
        Card(
            onClick = { onPhotoSelected(photo) },
            modifier = modifier
                .sharedElement(
                    state = rememberSharedContentState(key = "photo_${photo.id}"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photo.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = photo.displayPath,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalAnimationApi::class)
@Preview(showBackground = true)
@Composable
private fun RewindScreenPreview() {
    val samplePhotos = listOf(
        Photo(1L, "content://media/external/images/media/1", 0L, "Photo 1"),
        Photo(2L, "content://media/external/images/media/2", 0L, "Photo 2"),
        Photo(3L, "content://media/external/images/media/3", 0L, "Photo 3")
    )
    val groups = listOf(
        MemoryGroup(year = 2024, yearsAgo = 2, photos = samplePhotos)
    )
    RewindPhotosTheme {
        Surface {
            RewindScreenContent(
                uiState = RewindUiState(isLoading = false, memoryGroups = groups),
                selectedPhoto = null,
                onPhotoSelected = {},
                onBack = {}
            )
        }
    }
}
