package com.thewalkersoft.rewindphotos.domain.usecase

import com.thewalkersoft.rewindphotos.domain.model.DuplicateGroup
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.domain.repository.DuplicateDetector
import com.thewalkersoft.rewindphotos.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

/**
 * Use case for detecting duplicate photos.
 *
 * Orchestrates the duplicate detection workflow:
 * 1. Fetches all photos from the repository
 * 2. Passes them to the DuplicateDetector for analysis
 * 3. Returns grouped duplicates sorted by similarity
 *
 * This use case bridges domain logic and data access layers.
 */
class DetectDuplicatesUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val duplicateDetector: DuplicateDetector
) {
    /**
     * Detects duplicate photos from the device gallery.
     *
     * @param similarityThreshold Minimum similarity (0.0-1.0) to consider photos as duplicates
     *                           Default: 0.90f (90% similarity)
     * @return Flow of duplicate groups, sorted by similarity descending
     *
     * Example:
     * ```
     * DetectDuplicatesUseCase().invoke().collect { duplicateGroups ->
     *     duplicateGroups.forEach { group ->
     *         println("${group.totalPhotos} similar photos detected")
     *         println("Best: ${group.representativePhoto.displayPath}")
     *         println("Similarity: ${group.similarityPercentage}%")
     *     }
     * }
     * ```
     */
    operator fun invoke(similarityThreshold: Float = 0.90f): Flow<List<DuplicateGroup>> {
        return photoRepository.getAllPhotos().flatMapLatest { photos ->
            duplicateDetector.detectDuplicates(photos, similarityThreshold)
        }
    }
}

