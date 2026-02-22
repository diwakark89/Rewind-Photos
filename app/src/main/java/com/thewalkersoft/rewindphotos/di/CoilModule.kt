package com.thewalkersoft.rewindphotos.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Hilt module for providing Coil ImageLoader with optimized caching configuration.
 *
 * Optimization Strategy:
 * - Memory Cache: 256MB for fast access to recently viewed photos
 * - Disk Cache: 500MB for persistent caching across app sessions
 * - Both caches significantly speed up image loading when navigating between years
 */
@Module
@InstallIn(SingletonComponent::class)
object CoilModule {

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context
    ): ImageLoader {
        return ImageLoader.Builder(context)
            // Memory Cache: Store recently loaded images in RAM
            // 256MB allows caching 100+ high-quality thumbnails
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // Use 25% of available app memory (typically 256MB+)
                    .build()
            }
            // Disk Cache: Store images on device storage for faster reloads
            // When user jumps back to 2026, images load from disk cache instantly
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(500L * 1024L * 1024L) // 500MB disk cache
                    .build()
            }
            // Crossfade for smooth image transitions
            .crossfade(true)
            // Build the ImageLoader
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }
}

