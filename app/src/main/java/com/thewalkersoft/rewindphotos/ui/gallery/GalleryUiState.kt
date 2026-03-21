package com.thewalkersoft.rewindphotos.ui.gallery

import com.thewalkersoft.rewindphotos.domain.model.GroupedPhotosData

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
     * Success state - contains grouped photos organized by month
     */
    data class Success(val groupedPhotosData: GroupedPhotosData) : GalleryUiState()

    /**
     * Error state - contains error message and allows retry
     */
    data class Error(val message: String) : GalleryUiState()
}

