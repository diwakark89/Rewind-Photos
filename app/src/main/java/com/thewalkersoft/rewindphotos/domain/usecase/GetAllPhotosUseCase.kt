package com.thewalkersoft.rewindphotos.domain.usecase

import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all photos.
 * Encapsulates the business logic for fetching photos.
 */
class GetAllPhotosUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    operator fun invoke(): Flow<List<Photo>> {
        return photoRepository.getAllPhotos()
    }
}

