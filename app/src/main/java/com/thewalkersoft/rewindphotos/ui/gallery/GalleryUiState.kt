package com.thewalkersoft.rewindphotos.ui.gallery

import com.thewalkersoft.rewindphotos.domain.model.Photo

/**
 * Sealed class representing the different UI states for the Gallery screen.
 * This follows the MVVM pattern with clear state separation for different scenarios.
 */
sealed class GalleryUiState {
    /**
     * Loading state - shown when photos are being fetched from MediaStore
     */
    data object Loading : GalleryUiState()

    /**
     * Empty state - shown when no photos are found on the device
     */
    data object Empty : GalleryUiState()

    /**
     * Success state - contains the list of loaded photos sorted by date taken (descending)
     */
    data class Success(val photos: List<Photo>) : GalleryUiState()

    /**
     * Error state - contains error message and allows retry
     */
    data class Error(val message: String) : GalleryUiState()
}

