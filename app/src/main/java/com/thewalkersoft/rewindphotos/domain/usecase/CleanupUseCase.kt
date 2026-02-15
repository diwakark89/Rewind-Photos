package com.thewalkersoft.rewindphotos.domain.usecase

import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.domain.model.PhotoGroup
import com.thewalkersoft.rewindphotos.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for cleaning up duplicate photos.
 *
 * This use case orchestrates the duplicate detection and cleanup workflow:
 * 1. Groups photos by perceptual hash similarity
 * 2. Filters groups to only include those with >90% similarity
 * 3. Manages selection and deletion of duplicate photos
 *
 * The perceptual hash algorithm (pHash) works by:
 * - Resizing images to 8x8 pixels
 * - Converting to grayscale
 * - Computing average pixel value
 * - Creating a 64-bit hash by comparing each pixel to the average
 * - Measuring similarity as (64 - hammingDistance) / 64 * 100
 *
 * This approach is robust to minor changes like:
 * - Slight color adjustments
 * - Small compression artifacts
 * - Minor crop variations
 * - Slight brightness changes
 *
 * But will detect significant changes like:
 * - Different photos entirely
 * - Major rotations or crops
 * - Significant color shifts
 */
class CleanupUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    /**
     * Retrieves duplicate photos grouped by similarity and filtered by threshold.
     *
     * @param similarityThreshold Minimum similarity percentage (0-100) for grouping.
     *                           Default is 90.0% which catches most practical duplicates.
     * @return Flow of filtered photo groups containing only duplicates
     */
    operator fun invoke(similarityThreshold: Double = 90.0): Flow<List<PhotoGroup>> {
        return photoRepository.groupPhotosBySimilarity(similarityThreshold)
    }

    /**
     * Retrieves all duplicate photos grouped by similarity.
     * More flexible version allowing custom threshold.
     *
     * @param similarityThreshold Custom similarity threshold
     * @return Flow of photo groups matching the threshold
     */
    fun getDuplicatePhotos(similarityThreshold: Double = 90.0): Flow<List<PhotoGroup>> {
        return photoRepository.groupPhotosBySimilarity(similarityThreshold)
    }

    /**
     * Identifies the best photo in each group (typically the largest/best quality).
     * Can be used to automatically keep the best photo and mark others for deletion.
     *
     * @param group The photo group to analyze
     * @return The photo deemed to be the "best" in the group
     */
    fun identifyBestPhoto(group: PhotoGroup): Photo {
        // In a real scenario, you might:
        // - Choose the highest resolution photo
        // - Choose the most recently taken
        // - Use ML to detect best quality
        // For now, return the first (most recent, as photos are sorted DESC by date)
        return group.photos.firstOrNull() ?: throw IllegalArgumentException("Empty photo group")
    }

    /**
     * Calculates how many photos could be safely deleted from each group.
     * Returns groups size - 1 (keeping at least one)
     *
     * @param groups List of duplicate photo groups
     * @return Total number of photos that could be deleted
     */
    fun calculatePotentialDeletions(groups: List<PhotoGroup>): Int {
        return groups.sumOf { maxOf(0, it.photos.size - 1) }
    }
}

