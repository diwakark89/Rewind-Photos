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
            // Add video frame decoder for video thumbnail support
            .components {
                add(coil.decode.VideoFrameDecoder.Factory())
            }
            // Memory Cache: Store recently loaded images in RAM
            // 30% of app memory allows caching 150+ high-quality thumbnails
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.30) // Use 30% of available app memory
                    .build()
            }
            // Disk Cache: Store images on device storage for faster reloads
            // 1GB disk cache ensures photos from all years remain cached
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(1024L * 1024L * 1024L) // 1GB disk cache
                    .build()
            }
            // Enable crossfade for smooth transitions
            .crossfade(true)
            // Respect cache headers from MediaStore
            .respectCacheHeaders(false) // Ignore cache headers for local files
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

