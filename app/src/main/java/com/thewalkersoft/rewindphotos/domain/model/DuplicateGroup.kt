package com.thewalkersoft.rewindphotos.domain.model

/**
 * Represents a group of duplicate or near-identical photos.
 *
 * @property representativePhoto The "best" photo in the group (highest priority)
 * @property similarPhotos List of similar photos (including duplicates to remove)
 * @property similarityScore Average similarity score (0.0 to 1.0)
 *
 * The representative photo is selected based on priority:
 * 1. Highest resolution (width * height)
 * 2. Most recent (DATE_TAKEN)
 * 3. Largest file size
 */
data class DuplicateGroup(
    val representativePhoto: Photo,
    val similarPhotos: List<Photo>,
    val similarityScore: Double
) {
    /**
     * Total number of photos in the group (including representative)
     */
    val totalPhotos: Int = similarPhotos.size + 1

    /**
     * Number of potential duplicates (excluding representative)
     */
    val potentialDuplicates: Int = similarPhotos.size

    /**
     * Similarity as percentage (0-100)
     */
    val similarityPercentage: Int = (similarityScore * 100).toInt()
}

