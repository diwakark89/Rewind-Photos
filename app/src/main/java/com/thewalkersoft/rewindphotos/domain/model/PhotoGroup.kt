package com.thewalkersoft.rewindphotos.domain.model

/**
 * Represents a group of duplicate or similar photos based on perceptual hash similarity.
 *
 * @property photos List of photos in this group
 * @property similarity Average similarity percentage within the group
 * @property selectedForDeletion Set of photo IDs marked for deletion
 */
data class PhotoGroup(
    val photos: List<Photo>,
    val similarity: Double = 0.0,
    val selectedForDeletion: Set<Long> = emptySet()
) {
    fun togglePhotoSelection(photoId: Long): PhotoGroup {
        val updatedSelection = selectedForDeletion.toMutableSet()
        if (updatedSelection.contains(photoId)) {
            updatedSelection.remove(photoId)
        } else {
            updatedSelection.add(photoId)
        }
        return this.copy(selectedForDeletion = updatedSelection)
    }

    fun selectAll(): PhotoGroup {
        return this.copy(selectedForDeletion = photos.map { it.id }.toSet())
    }

    fun deselectAll(): PhotoGroup {
        return this.copy(selectedForDeletion = emptySet())
    }

    fun isPhotoSelected(photoId: Long): Boolean {
        return selectedForDeletion.contains(photoId)
    }
}

