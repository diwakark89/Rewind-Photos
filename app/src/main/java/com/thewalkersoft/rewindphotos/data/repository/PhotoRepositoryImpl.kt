package com.thewalkersoft.rewindphotos.data.repository

import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.domain.model.PhotoGroup
import com.thewalkersoft.rewindphotos.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementation of PhotoRepository.
 * This class delegates to MediaStoreRepository for actual photo data operations.
 */
class PhotoRepositoryImpl @Inject constructor(
    private val mediaStoreRepository: MediaStoreRepository
) : PhotoRepository {

    override fun getAllPhotos(): Flow<List<Photo>> {
        return mediaStoreRepository.getAllPhotos()
    }

    override suspend fun getPhotoById(id: Long): Photo? {
        return mediaStoreRepository.getPhotoById(id)
    }

    override fun groupPhotosBySimilarity(similarityThreshold: Double): Flow<List<PhotoGroup>> {
        return mediaStoreRepository.groupPhotosBySimilarity(similarityThreshold)
    }

    override suspend fun deletePhoto(photoId: Long): Boolean {
        return mediaStoreRepository.deletePhoto(photoId)
    }

    override suspend fun deletePhotos(photoIds: List<Long>): Int {
        return mediaStoreRepository.deletePhotos(photoIds)
    }
}

