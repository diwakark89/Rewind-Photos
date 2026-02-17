package com.thewalkersoft.rewindphotos.domain.model

/**
 * Represents a group of memories for a specific year on today's date.
 */
data class MemoryGroup(
    val year: Int,
    val yearsAgo: Int,
    val photos: List<Photo>
)

