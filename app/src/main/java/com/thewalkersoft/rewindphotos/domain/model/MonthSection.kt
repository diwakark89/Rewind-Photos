package com.thewalkersoft.rewindphotos.domain.model

/**
 * Represents a month section containing photos grouped by month and optionally by location.
 *
 * @property year Year of the photos in this section
 * @property month Month (0-11) of the photos in this section
 * @property location Optional location information for all photos in this section
 * @property photos List of photos taken in this month/location
 */
data class MonthSection(
    val year: Int,
    val month: Int,
    val location: String? = null,
    val photos: List<Photo>
) {
    val photoCount: Int = photos.size
}

