package com.thewalkersoft.rewindphotos.domain.usecase

import com.thewalkersoft.rewindphotos.domain.model.PhotoGroup
import com.thewalkersoft.rewindphotos.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving duplicate photos grouped by similarity.
 */
class GetDuplicatePhotosUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    operator fun invoke(similarityThreshold: Double = 90.0): Flow<List<PhotoGroup>> {
        return photoRepository.groupPhotosBySimilarity(similarityThreshold)
    }
}

