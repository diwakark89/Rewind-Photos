package com.thewalkersoft.rewindphotos.domain.model

/**
 * Represents grouped photo data organized by months for efficient scrolling.
 *
 * @property sections List of month sections sorted in reverse chronological order (newest first)
 * @property closestSectionIndex Index of the section closest to the selected date
 */
data class GroupedPhotosData(
    val sections: List<MonthSection>,
    val closestSectionIndex: Int = 0
)

