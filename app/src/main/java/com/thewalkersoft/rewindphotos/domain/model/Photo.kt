package com.thewalkersoft.rewindphotos.domain.model

/**
 * Domain model representing a photo in the Rewind Photos app.
 *
 * @property id Unique identifier for the photo
 * @property uri URI string of the photo location
 * @property dateTaken Timestamp (in milliseconds) when the photo was taken
 * @property displayPath Human-readable path for displaying to the user
 */
data class Photo(
    val id: Long,
    val uri: String,
    val dateTaken: Long,
    val displayPath: String
)

