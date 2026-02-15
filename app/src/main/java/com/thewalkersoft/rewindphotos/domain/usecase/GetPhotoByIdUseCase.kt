package com.thewalkersoft.rewindphotos.domain.usecase

import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.domain.repository.PhotoRepository
import javax.inject.Inject

/**
 * Use case for retrieving a specific photo by ID.
 * Encapsulates the business logic for fetching a single photo.
 */
class GetPhotoByIdUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(photoId: Long): Photo? {
        return photoRepository.getPhotoById(photoId)
    }
}

