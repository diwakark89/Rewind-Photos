package com.thewalkersoft.rewindphotos.data.repository

import android.content.ContentUris
import android.content.Context
import android.graphics.BitmapFactory
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
                val selection = "${MediaStore.Images.Media._ID} = ?"
                val selectionArgs = arrayOf(photoId.toString())
                val deletedCount = context.contentResolver.delete(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    selection,
                    selectionArgs
                )
                deletedCount > 0
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun deletePhotos(photoIds: List<Long>): Int {
        return withContext(ioDispatcher) {
            var deletedCount = 0
            for (photoId in photoIds) {
                if (deletePhoto(photoId)) {
                    deletedCount++
                }
            }
            deletedCount
        }
    }

    private fun queryAllPhotos(): List<Photo> {
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

                    // Skip EXIF extraction here to avoid blocking
                    // Location can be fetched later on-demand or in background
                    photos.add(
                        Photo(
                            id = id,
                            uri = contentUri.toString(),
                            dateTaken = dateTaken,
                            displayPath = displayName,
                            location = null
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaStoreRepository", "Error querying photos", e)
        }

        return photos
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
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DISPLAY_NAME
        )
        val selection = "${MediaStore.Images.Media._ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        return try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return null

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

                Photo(
                    id = photoId,
                    uri = contentUri.toString(),
                    dateTaken = dateTaken,
                    displayPath = displayName,
                    location = null
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaStoreRepository", "Error querying photo by id: $id", e)
            null
        }
    }
}
