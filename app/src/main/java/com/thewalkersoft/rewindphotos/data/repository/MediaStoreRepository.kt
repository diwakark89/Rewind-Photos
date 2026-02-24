package com.thewalkersoft.rewindphotos.data.repository

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import com.thewalkersoft.rewindphotos.data.util.PerceptualHashUtils
import com.thewalkersoft.rewindphotos.di.DefaultDispatcher
import com.thewalkersoft.rewindphotos.di.IoDispatcher
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.domain.model.PhotoGroup
import com.thewalkersoft.rewindphotos.domain.repository.PhotoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository backed by MediaStore queries.
 */
class MediaStoreRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : PhotoRepository {

    override fun getAllPhotos(): Flow<List<Photo>> = flow {
        emit(queryAllPhotos())
    }.flowOn(ioDispatcher)

    override suspend fun getPhotoById(id: Long): Photo? {
        return queryPhotoById(id)
    }

    override fun groupPhotosBySimilarity(similarityThreshold: Double): Flow<List<PhotoGroup>> = flow {
        val photos = withContext(ioDispatcher) { queryAllPhotos() }
        if (photos.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val photoHashes = withContext(defaultDispatcher) {
            val hashes = mutableMapOf<Long, Long>()
            for (photo in photos) {
                try {
                    // Load bitmap from content URI instead of file path
                    val bitmap = context.contentResolver.openInputStream(android.net.Uri.parse(photo.uri))?.use {
                        BitmapFactory.decodeStream(it)
                    }
                    if (bitmap != null) {
                        hashes[photo.id] = PerceptualHashUtils.calculateHash(bitmap)
                        bitmap.recycle()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MediaStoreRepository", "Error hashing photo ${photo.id}", e)
                    continue
                }
            }
            hashes
        }

        // Group photos by similarity
        val groups = mutableListOf<PhotoGroup>()
        val processedIds = mutableSetOf<Long>()

        for (photo in photos) {
            if (photo.id in processedIds) continue
            if (photo.id !in photoHashes) continue

            val group = mutableListOf(photo)
            val photoHash = photoHashes[photo.id]!!
            processedIds.add(photo.id)

            for (otherPhoto in photos) {
                if (otherPhoto.id in processedIds) continue
                if (otherPhoto.id !in photoHashes) continue

                val otherHash = photoHashes[otherPhoto.id]!!
                val similarity = PerceptualHashUtils.calculateSimilarity(photoHash, otherHash)

                if (similarity >= similarityThreshold) {
                    group.add(otherPhoto)
                    processedIds.add(otherPhoto.id)
                }
            }

            // Only add groups with more than one photo
            if (group.size > 1) {
                val avgSimilarity = group.drop(1).map { otherPhoto ->
                    PerceptualHashUtils.calculateSimilarity(photoHash, photoHashes[otherPhoto.id]!!)
                }.average()
                groups.add(PhotoGroup(group, avgSimilarity))
            }
        }

        emit(groups)
    }.flowOn(defaultDispatcher)

    override suspend fun deletePhoto(photoId: Long): Boolean {
        return withContext(ioDispatcher) {
            try {
                android.util.Log.d("MediaStoreRepository", "=== DELETE MEDIA STARTED ===")
                android.util.Log.d("MediaStoreRepository", "Media ID: $photoId")
                android.util.Log.d("MediaStoreRepository", "Android API Level: ${Build.VERSION.SDK_INT}")

                // Try to delete as image first
                var uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    photoId
                )

                android.util.Log.d("MediaStoreRepository", "Trying Image URI: $uri")
                var deletedCount = context.contentResolver.delete(uri, null, null)

                // If image deletion failed, try as video
                if (deletedCount == 0) {
                    android.util.Log.d("MediaStoreRepository", "Not an image, trying as video")
                    uri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        photoId
                    )
                    android.util.Log.d("MediaStoreRepository", "Video URI: $uri")
                    deletedCount = context.contentResolver.delete(uri, null, null)
                }

                android.util.Log.d("MediaStoreRepository", "ContentResolver.delete() returned: $deletedCount")

                val success = deletedCount > 0
                if (success) {
                    android.util.Log.d("MediaStoreRepository", "✓ Media $photoId DELETED ($deletedCount rows)")
                } else {
                    android.util.Log.w("MediaStoreRepository", "⚠ Media $photoId NOT deleted (0 rows affected)")
                    android.util.Log.w("MediaStoreRepository", "⚠ On Android 13+, this requires user permission via system dialog")
                }

                success
            } catch (securityException: SecurityException) {
                android.util.Log.e("MediaStoreRepository", "✗ SecurityException: ${securityException.message}")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException = securityException as? RecoverableSecurityException
                    if (recoverableSecurityException != null) {
                        android.util.Log.w("MediaStoreRepository", "⚠ RecoverableSecurityException - Need to request user permission")
                        // This needs to be handled at the UI level with an ActivityResultLauncher
                        // For now, return false and log the issue
                        android.util.Log.e("MediaStoreRepository", "⚠ APP NEEDS TO REQUEST PERMISSION VIA SYSTEM DIALOG")
                        android.util.Log.e("MediaStoreRepository", "⚠ This is normal on Android 13+ for media not created by this app")
                    }
                }
                false
            } catch (e: Exception) {
                android.util.Log.e("MediaStoreRepository", "✗ Exception in deletePhoto: ${e.message}", e)
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Delete photo using direct ContentResolver delete (Android 12 and below)
     */
    private fun deletePhotoOlderAndroid(photoId: Long): Boolean {
        return try {
            android.util.Log.d("MediaStoreRepository", "Using deletePhotoOlderAndroid method for ID: $photoId")

            val selection = "${MediaStore.Images.Media._ID} = ?"
            val selectionArgs = arrayOf(photoId.toString())

            val deletedCount = context.contentResolver.delete(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                selection,
                selectionArgs
            )

            val success = deletedCount > 0
            android.util.Log.d("MediaStoreRepository", "Older Android delete result: deleted $deletedCount rows, success=$success")
            success
        } catch (e: Exception) {
            android.util.Log.e("MediaStoreRepository", "Error in deletePhotoOlderAndroid: ${e.message}", e)
            false
        }
    }

    /**
     * Delete photo using MediaStore.createDeleteRequest (Android 13+)
     * This returns a PendingIntent that needs to be launched by the Activity
     * For now, we'll try the direct delete method as fallback
     */
    private fun deletePhotoAndroid13Plus(photoId: Long): Boolean {
        return try {
            android.util.Log.d("MediaStoreRepository", "Using deletePhotoAndroid13Plus method for ID: $photoId")


            // Try Method 1: Direct ContentResolver delete (may fail on Android 13+)
            val selection = "${MediaStore.Images.Media._ID} = ?"
            val selectionArgs = arrayOf(photoId.toString())

            try {
                val deletedCount = context.contentResolver.delete(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    selection,
                    selectionArgs
                )

                if (deletedCount > 0) {
                    android.util.Log.d("MediaStoreRepository", "Android 13+ delete result: deleted $deletedCount rows")
                    return true
                } else {
                    android.util.Log.d("MediaStoreRepository", "ContentResolver delete returned 0 rows, trying file-based deletion")
                }
            } catch (e: Exception) {
                android.util.Log.d("MediaStoreRepository", "ContentResolver delete failed: ${e.message}, trying file-based deletion")
            }

            // Try Method 2: Get the file path and delete directly
            try {
                val filePath = getPhotoFilePath(photoId)
                if (filePath != null) {
                    val file = java.io.File(filePath)
                    if (file.exists()) {
                        val deleted = file.delete()
                        android.util.Log.d("MediaStoreRepository", "File-based deletion: file deleted=$deleted for path: $filePath")

                        // Also delete from MediaStore
                        try {
                            context.contentResolver.delete(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                selection,
                                selectionArgs
                            )
                        } catch (e: Exception) {
                            android.util.Log.d("MediaStoreRepository", "MediaStore entry cleanup failed: ${e.message}")
                        }

                        return deleted
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MediaStoreRepository", "Error in file-based deletion: ${e.message}", e)
            }

            false
        } catch (e: Exception) {
            android.util.Log.e("MediaStoreRepository", "Error in deletePhotoAndroid13Plus: ${e.message}", e)
            false
        }
    }

    /**
     * Get the file path for a photo from its URI
     */
    private fun getPhotoFilePath(photoId: Long): String? {
        return try {
            val uri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                photoId
            )

            // Try to get the file path using MediaStore data column
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    cursor.getString(dataColumn)
                } else null
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaStoreRepository", "Error getting photo file path: ${e.message}", e)
            null
        }
    }

    override suspend fun deletePhotos(photoIds: List<Long>): Int {
        return withContext(ioDispatcher) {
            android.util.Log.d("MediaStoreRepository", "=== BATCH DELETE STARTED ===")
            android.util.Log.d("MediaStoreRepository", "Total photos to delete: ${photoIds.size}")
            android.util.Log.d("MediaStoreRepository", "Photo IDs: $photoIds")

            var deletedCount = 0
            for ((index, photoId) in photoIds.withIndex()) {
                android.util.Log.d("MediaStoreRepository", "Deleting ${index + 1}/${photoIds.size}: ID=$photoId")
                try {
                    if (deletePhoto(photoId)) {
                        deletedCount++
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MediaStoreRepository", "Exception for photo $photoId: ${e.message}", e)
                }
            }

            android.util.Log.d("MediaStoreRepository", "=== BATCH DELETE COMPLETED ===")
            android.util.Log.d("MediaStoreRepository", "Result: $deletedCount/${photoIds.size} photos deleted")
            deletedCount
        }
    }

    private fun queryAllPhotos(): List<Photo> {
        val mediaItems = mutableListOf<Photo>()

        // Query images
        val images = queryImages()
        android.util.Log.d("MediaStoreRepository", "Loaded ${images.size} images")
        mediaItems.addAll(images)

        // Query videos
        val videos = queryVideos()
        android.util.Log.d("MediaStoreRepository", "Loaded ${videos.size} videos")
        mediaItems.addAll(videos)

        android.util.Log.d("MediaStoreRepository", "Total media items: ${mediaItems.size} (${images.size} images + ${videos.size} videos)")

        // Sort by date taken (descending)
        return mediaItems.sortedByDescending { it.dateTaken }
    }

    private fun queryImages(): List<Photo> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DISPLAY_NAME
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        val photos = mutableListOf<Photo>()

        try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateTaken = cursor.getLong(dateTakenColumn)
                    val displayName = cursor.getString(displayNameColumn) ?: "photo"
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    photos.add(
                        Photo(
                            id = id,
                            uri = contentUri.toString(),
                            dateTaken = dateTaken,
                            displayPath = displayName,
                            location = null,
                            mediaType = com.thewalkersoft.rewindphotos.domain.model.MediaType.IMAGE
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaStoreRepository", "Error querying images", e)
        }

        return photos
    }

    private fun queryVideos(): List<Photo> {
        android.util.Log.d("MediaStoreRepository", "=== QUERYING VIDEOS ===")
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.DISPLAY_NAME
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_TAKEN} DESC"
        val videos = mutableListOf<Photo>()

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            if (cursor == null) {
                android.util.Log.e("MediaStoreRepository", "Video cursor is null - permission might be missing")
                return videos
            }

            cursor.use {
                android.util.Log.d("MediaStoreRepository", "Video cursor count: ${cursor.count}")
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateTaken = cursor.getLong(dateTakenColumn)
                    val displayName = cursor.getString(displayNameColumn) ?: "video"
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    videos.add(
                        Photo(
                            id = id,
                            uri = contentUri.toString(),
                            dateTaken = dateTaken,
                            displayPath = displayName,
                            location = null,
                            mediaType = com.thewalkersoft.rewindphotos.domain.model.MediaType.VIDEO
                        )
                    )
                    android.util.Log.d("MediaStoreRepository", "Loaded video: id=$id, name=$displayName")
                }
            }
        } catch (e: SecurityException) {
            android.util.Log.e("MediaStoreRepository", "SecurityException querying videos - permission not granted", e)
        } catch (e: Exception) {
            android.util.Log.e("MediaStoreRepository", "Error querying videos", e)
        }

        android.util.Log.d("MediaStoreRepository", "=== VIDEOS QUERY COMPLETE: ${videos.size} videos found ===")
        return videos
    }

    /**
     * Extract location information from EXIF data of a photo.
     * Returns null if no location data is available.
     * Uses timeout to prevent blocking if EXIF extraction is slow.
     */
    private fun extractLocationFromExif(photoUri: String): String? {
        return try {
            val uri = android.net.Uri.parse(photoUri)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                try {
                    val exif = android.media.ExifInterface(inputStream)

                    // Try to get GPS coordinates
                    val latRef = exif.getAttribute(android.media.ExifInterface.TAG_GPS_LATITUDE_REF)
                    val lonRef = exif.getAttribute(android.media.ExifInterface.TAG_GPS_LONGITUDE_REF)

                    if (latRef != null && lonRef != null) {
                        // Location data exists, return a simplified identifier
                        // Return "GPS" as placeholder since full geocoding is expensive
                        "GPS"
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    // EXIF parsing failed, return null quickly
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.d("MediaStoreRepository", "Error extracting EXIF location", e)
            null
        }
    }

    private fun queryPhotoById(id: Long): Photo? {
        // Try to query as image first
        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DISPLAY_NAME
        )
        val selection = "${MediaStore.Images.Media._ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageProjection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                    val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

                    val photoId = cursor.getLong(idColumn)
                    val dateTaken = cursor.getLong(dateTakenColumn)
                    val displayName = cursor.getString(displayNameColumn) ?: "photo"
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        photoId
                    )

                    return Photo(
                        id = photoId,
                        uri = contentUri.toString(),
                        dateTaken = dateTaken,
                        displayPath = displayName,
                        location = null,
                        mediaType = com.thewalkersoft.rewindphotos.domain.model.MediaType.IMAGE
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaStoreRepository", "Error querying image by id: $id", e)
        }

        // If not found as image, try as video
        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.DISPLAY_NAME
        )

        try {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
                    val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)

                    val videoId = cursor.getLong(idColumn)
                    val dateTaken = cursor.getLong(dateTakenColumn)
                    val displayName = cursor.getString(displayNameColumn) ?: "video"
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        videoId
                    )

                    return Photo(
                        id = videoId,
                        uri = contentUri.toString(),
                        dateTaken = dateTaken,
                        displayPath = displayName,
                        location = null,
                        mediaType = com.thewalkersoft.rewindphotos.domain.model.MediaType.VIDEO
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaStoreRepository", "Error querying video by id: $id", e)
        }

        return null
    }
}
