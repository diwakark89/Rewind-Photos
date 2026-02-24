package com.thewalkersoft.rewindphotos.domain.model

/**
 * Media types supported by the app
 */
enum class MediaType {
    IMAGE,
    VIDEO
}

/**
 * Domain model representing a photo or video in the Rewind Photos app.
 *
 * @property id Unique identifier for the media item
 * @property uri URI string of the media location
 * @property dateTaken Timestamp (in milliseconds) when the media was taken
 * @property displayPath Human-readable path for displaying to the user
 * @property location Optional location information extracted from EXIF data or geocoding
 * @property mediaType Type of media (IMAGE or VIDEO)
 */
data class Photo(
    val id: Long,
    val uri: String,
    val dateTaken: Long,
    val displayPath: String,
    val location: String? = null,
    val mediaType: MediaType = MediaType.IMAGE
)

