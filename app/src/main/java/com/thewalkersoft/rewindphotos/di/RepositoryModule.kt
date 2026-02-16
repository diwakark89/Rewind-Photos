package com.thewalkersoft.rewindphotos.di

import com.thewalkersoft.rewindphotos.data.repository.LocalDuplicateDetector
import com.thewalkersoft.rewindphotos.data.repository.PhotoRepositoryImpl
import com.thewalkersoft.rewindphotos.domain.repository.DuplicateDetector
import com.thewalkersoft.rewindphotos.domain.repository.PhotoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository implementations.
 * Binds concrete implementations to their interfaces:
 * - PhotoRepositoryImpl → PhotoRepository
 * - LocalDuplicateDetector → DuplicateDetector
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPhotoRepository(
        photoRepositoryImpl: PhotoRepositoryImpl
    ): PhotoRepository

    @Binds
    @Singleton
    abstract fun bindDuplicateDetector(
        localDuplicateDetector: LocalDuplicateDetector
    ): DuplicateDetector
}

