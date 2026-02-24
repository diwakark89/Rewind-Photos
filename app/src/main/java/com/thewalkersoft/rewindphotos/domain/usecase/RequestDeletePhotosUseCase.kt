package com.thewalkersoft.rewindphotos.domain.usecase

import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for deleting photos on Android 13+ with proper permission handling
 */
class RequestDeletePhotosUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Create a delete request for Android 13+
     * Returns an IntentSender that should be launched by the Activity
     * Handles both images and videos by checking both MediaStore tables
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun createDeleteRequest(photoIds: List<Long>): IntentSender? {
        return try {
            val uris = photoIds.mapNotNull { id ->
                // Try as image first
                val imageUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // Check if it exists as an image
                val isImage = try {
                    context.contentResolver.query(
                        imageUri,
                        arrayOf(MediaStore.Images.Media._ID),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        cursor.count > 0
                    } ?: false
                } catch (e: Exception) {
                    false
                }

                if (isImage) {
                    android.util.Log.d("RequestDeletePhotosUseCase", "Item $id is an IMAGE")
                    imageUri
                } else {
                    // Try as video
                    val videoUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val isVideo = try {
                        context.contentResolver.query(
                            videoUri,
                            arrayOf(MediaStore.Video.Media._ID),
                            null,
                            null,
                            null
                        )?.use { cursor ->
                            cursor.count > 0
                        } ?: false
                    } catch (e: Exception) {
                        false
                    }

                    if (isVideo) {
                        android.util.Log.d("RequestDeletePhotosUseCase", "Item $id is a VIDEO")
                        videoUri
                    } else {
                        android.util.Log.w("RequestDeletePhotosUseCase", "Item $id not found in Images or Video tables")
                        null
                    }
                }
            }

            android.util.Log.d("RequestDeletePhotosUseCase", "Creating delete request for ${uris.size} media items (from ${photoIds.size} IDs)")

            if (uris.isEmpty()) {
                android.util.Log.e("RequestDeletePhotosUseCase", "No valid URIs found for deletion")
                return null
            }

            val pendingIntent = MediaStore.createDeleteRequest(
                context.contentResolver,
                uris
            )

            android.util.Log.d("RequestDeletePhotosUseCase", "Delete request created successfully")
            pendingIntent.intentSender
        } catch (e: Exception) {
            android.util.Log.e("RequestDeletePhotosUseCase", "Error creating delete request: ${e.message}", e)
            null
        }
    }
}

