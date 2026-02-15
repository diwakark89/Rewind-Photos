package com.thewalkersoft.rewindphotos.di

import com.thewalkersoft.rewindphotos.data.repository.PhotoRepositoryImpl
import com.thewalkersoft.rewindphotos.domain.repository.PhotoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository implementations.
 * Binds PhotoRepositoryImpl to the PhotoRepository interface
 * for dependency injection across the application.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPhotoRepository(
        photoRepositoryImpl: PhotoRepositoryImpl
    ): PhotoRepository
}

