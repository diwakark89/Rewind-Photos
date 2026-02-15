package com.thewalkersoft.rewindphotos.domain.repository

import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.domain.model.PhotoGroup
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for photo operations.
 * Defines the contract for accessing photo data.
 */
interface PhotoRepository {
    /**
     * Retrieves all photos as a Flow.
     * @return Flow of list of photos
     */
    fun getAllPhotos(): Flow<List<Photo>>

    /**
     * Retrieves a specific photo by its ID.
     * @param id The unique identifier of the photo
     * @return The photo if found, null otherwise
     */
    suspend fun getPhotoById(id: Long): Photo?

    /**
     * Groups photos based on perceptual hash similarity.
     * Photos with similarity over the threshold are grouped together.
     *
     * @param similarityThreshold Minimum similarity percentage (0-100) for grouping
     * @return Flow of list of photo groups
     */
    fun groupPhotosBySimilarity(similarityThreshold: Double = 90.0): Flow<List<PhotoGroup>>

    /**
     * Deletes a photo from the device storage.
     *
     * @param photoId The ID of the photo to delete
     * @return True if deletion was successful, false otherwise
     */
    suspend fun deletePhoto(photoId: Long): Boolean

    /**
     * Deletes multiple photos from the device storage.
     *
     * @param photoIds List of photo IDs to delete
     * @return Number of successfully deleted photos
     */
    suspend fun deletePhotos(photoIds: List<Long>): Int
}

