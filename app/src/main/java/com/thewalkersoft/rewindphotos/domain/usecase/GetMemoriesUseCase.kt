package com.thewalkersoft.rewindphotos.domain.usecase

import com.thewalkersoft.rewindphotos.domain.model.MemoryGroup
import com.thewalkersoft.rewindphotos.domain.model.Photo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Builds "On This Day" memories grouped by year.
 */
class GetMemoriesUseCase @Inject constructor() {
    operator fun invoke(
        photos: List<Photo>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<MemoryGroup> {
        if (photos.isEmpty()) return emptyList()

        val today = LocalDate.now(zoneId)
        val grouped = photos.asSequence().mapNotNull { photo ->
            if (photo.dateTaken <= 0L) return@mapNotNull null
            val photoDate = runCatching {
                Instant.ofEpochMilli(photo.dateTaken).atZone(zoneId).toLocalDate()
            }.getOrNull() ?: return@mapNotNull null

            if (photoDate.month != today.month || photoDate.dayOfMonth != today.dayOfMonth) {
                return@mapNotNull null
            }

            photoDate.year to photo
        }.groupBy(
            keySelector = { it.first },
            valueTransform = { it.second }
        )

        if (grouped.isEmpty()) return emptyList()

        return grouped.keys.sortedDescending().map { year ->
            val groupPhotos = grouped[year].orEmpty()
            MemoryGroup(
                year = year,
                yearsAgo = today.year - year,
                photos = groupPhotos.toList()
            )
        }
    }
}

