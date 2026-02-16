package com.thewalkersoft.rewindphotos.domain.repository

import com.thewalkersoft.rewindphotos.domain.model.DuplicateGroup
import com.thewalkersoft.rewindphotos.domain.model.Photo
import kotlinx.coroutines.flow.Flow

/**
 * Interface for duplicate photo detection.
 * Defines the contract for finding and grouping similar/duplicate photos.
 */
interface DuplicateDetector {
    /**
     * Detects duplicate or near-identical photos from a list.
     *
     * Uses perceptual hashing (pHash) to identify images with >90% similarity.
     *
     * @param photos List of photos to analyze
     * @param similarityThreshold Minimum similarity score (0.0-1.0) to group photos
     *                           Default: 0.90f (90% similarity)
     * @return Flow of detected duplicate groups, sorted by similarity descending
     */
    suspend fun detectDuplicates(
        photos: List<Photo>,
        similarityThreshold: Float = 0.90f
    ): Flow<List<DuplicateGroup>>

    /**
     * Clears any cached hashes or temporary data.
     * Call this when starting a new detection session or to free memory.
     */
    suspend fun clearCache()
}

