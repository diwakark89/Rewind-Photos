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
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun createDeleteRequest(photoIds: List<Long>): IntentSender? {
        return try {
            val uris = photoIds.map { id ->
                ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
            }

            android.util.Log.d("RequestDeletePhotosUseCase", "Creating delete request for ${uris.size} photos")

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

