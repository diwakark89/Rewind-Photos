package com.thewalkersoft.rewindphotos.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thewalkersoft.rewindphotos.domain.usecase.GetAllPhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Gallery screen.
 * Manages UI state and handles photo loading from MediaStore.
 *
 * Key responsibilities:
 * - Load photos using GetAllPhotosUseCase
 * - Manage UI state (Loading, Success, Empty, Error)
 * - Handle retry logic on errors
 * - Ensure smooth scrolling with optimized data flow
 *
 * Performance notes:
 * - Uses StateFlow to avoid unnecessary recompositions
 * - Photos are sorted by date taken (descending) before emission
 * - IO operations run on Dispatchers.IO (configured in GetAllPhotosUseCase)
 * - Compatible with LazyVerticalGrid for efficient rendering
 */
@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val getAllPhotosUseCase: GetAllPhotosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        loadPhotos()
    }

    /**
     * Loads photos from MediaStore and updates UI state accordingly.
     * Photos are sorted by date taken in descending order (newest first).
     */
    private fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = GalleryUiState.Loading
            try {
                getAllPhotosUseCase().collect { photos ->
                    // Sort photos by date taken in descending order (newest first)
                    val sortedPhotos = photos.sortedByDescending { it.dateTaken }

                    _uiState.value = if (sortedPhotos.isEmpty()) {
                        GalleryUiState.Empty
                    } else {
                        GalleryUiState.Success(sortedPhotos)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = GalleryUiState.Error(
                    "Failed to load photos: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    /**
     * Retries loading photos after an error.
     * Useful for network-like errors or temporary failures.
     */
    fun retry() {
        loadPhotos()
    }
}

