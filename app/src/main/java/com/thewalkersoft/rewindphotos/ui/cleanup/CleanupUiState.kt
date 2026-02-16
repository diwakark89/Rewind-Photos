package com.thewalkersoft.rewindphotos.ui.cleanup

import com.thewalkersoft.rewindphotos.domain.model.DuplicateGroup

/**
 * Represents the different UI states for the Cleanup screen.
 *
 * This sealed class ensures type-safe state management in the ViewModel.
 */
sealed class CleanupUiState {
    /**
     * Initial state: analyzing photos for duplicates
     */
    data object Loading : CleanupUiState()

    /**
     * No duplicates found
     */
    data object NoDuplicates : CleanupUiState()

    /**
     * Duplicates detected and ready for review
     *
     * @param groups List of duplicate groups
     * @param totalPhotos Total photos that are duplicates
     * @param potentialSpaceSavings Estimated photos that could be deleted
     */
    data class DuplicatesFound(
        val groups: List<DuplicateGroup>,
        val totalPhotos: Int,
        val potentialSpaceSavings: Int
    ) : CleanupUiState()

    /**
     * Error during duplicate detection
     *
     * @param message Error message to display
     */
    data class Error(val message: String) : CleanupUiState()
}

