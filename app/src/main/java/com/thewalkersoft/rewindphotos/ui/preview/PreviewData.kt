package com.thewalkersoft.rewindphotos.ui.preview

import com.thewalkersoft.rewindphotos.domain.model.GroupedPhotosData
import com.thewalkersoft.rewindphotos.domain.model.MonthSection
import com.thewalkersoft.rewindphotos.domain.model.Photo

/**
 * Shared deterministic fixtures for Compose previews.
 */
object PreviewData {
    val photos: List<Photo> = listOf(
        Photo(101, "content://preview/photo/101", 1706745600000L, "/storage/emulated/0/DCIM/Camera/IMG_101.jpg", "New York"),
        Photo(102, "content://preview/photo/102", 1706832000000L, "/storage/emulated/0/DCIM/Camera/IMG_102.jpg", "New York"),
        Photo(103, "content://preview/photo/103", 1706918400000L, "/storage/emulated/0/DCIM/Camera/IMG_103.jpg", "New York"),
        Photo(104, "content://preview/photo/104", 1709251200000L, "/storage/emulated/0/DCIM/Camera/IMG_104.jpg", "San Francisco"),
        Photo(105, "content://preview/photo/105", 1709337600000L, "/storage/emulated/0/DCIM/Camera/IMG_105.jpg", "San Francisco"),
        Photo(106, "content://preview/photo/106", 1711929600000L, "/storage/emulated/0/DCIM/Camera/IMG_106.jpg", "Tokyo")
    )

    val groupedPhotosData: GroupedPhotosData = GroupedPhotosData(
        sections = listOf(
            MonthSection(year = 2024, month = 3, location = "Tokyo", photos = photos.takeLast(1)),
            MonthSection(year = 2024, month = 2, location = "San Francisco", photos = photos.subList(3, 5)),
            MonthSection(year = 2024, month = 1, location = "New York", photos = photos.take(3))
        ),
        closestSectionIndex = 1
    )

    val availableYears: List<Int> = listOf(2024, 2023, 2022)
}

