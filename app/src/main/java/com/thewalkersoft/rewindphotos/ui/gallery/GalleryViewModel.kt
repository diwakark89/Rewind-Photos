package com.thewalkersoft.rewindphotos.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thewalkersoft.rewindphotos.domain.model.GroupedPhotosData
import com.thewalkersoft.rewindphotos.domain.model.MonthSection
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.domain.usecase.GetAllPhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/**
 * ViewModel for the Gallery screen.
 * Manages UI state and handles photo loading from MediaStore.
 *
 * Key responsibilities:
 * - Load photos using GetAllPhotosUseCase
 * - Group photos by month and year
 * - Manage UI state (Loading, Success, Empty, Error)
 * - Handle retry logic on errors
 * - Ensure smooth scrolling with optimized data flow
 *
 * Performance notes:
 * - Uses StateFlow to avoid unnecessary recompositions
 * - Photos are grouped by month in reverse chronological order
 * - IO operations run on Dispatchers.IO (configured in GetAllPhotosUseCase)
 * - Compatible with LazyColumn for efficient rendering
 */
@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val getAllPhotosUseCase: GetAllPhotosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        loadPhotos()
    }

    /**
     * Loads photos from MediaStore and updates UI state accordingly.
     * Photos are grouped by month in reverse chronological order (newest first).
     */
    private fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = GalleryUiState.Loading
            try {
                getAllPhotosUseCase().collect { photos ->
                    if (photos.isEmpty()) {
                        _uiState.value = GalleryUiState.Empty
                    } else {
                        // Group photos by month and year
                        val groupedPhotosData = groupPhotosByMonth(photos)
                        _uiState.value = GalleryUiState.Success(groupedPhotosData)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = GalleryUiState.Error(
                    "Failed to load photos: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    /**
     * Group photos by month and year in reverse chronological order.
     */
    private fun groupPhotosByMonth(photos: List<Photo>): GroupedPhotosData {
        val zoneId = ZoneId.systemDefault()
        val monthMap = mutableMapOf<Pair<Int, Int>, MutableList<Photo>>() // (year, month) -> photos

        // Group photos by (year, month)
        for (photo in photos) {
            if (photo.dateTaken <= 0L) continue

            val photoDate = Instant.ofEpochMilli(photo.dateTaken).atZone(zoneId).toLocalDate()
            val key = Pair(photoDate.year, photoDate.monthValue - 1) // month 0-11
            monthMap.getOrPut(key) { mutableListOf() }.add(photo)
        }

        // Create month sections and sort in reverse chronological order
        val sections = monthMap.map { (yearMonth, photoList) ->
            val (year, month) = yearMonth
            // Use location from first photo in section (or null if no location data)
            val location = photoList.firstOrNull()?.location
            MonthSection(year, month, location, photoList.sortedByDescending { it.dateTaken })
        }.sortedWith(compareBy<MonthSection> { it.year }.thenBy { it.month }.reversed())

        return GroupedPhotosData(sections, closestSectionIndex = 0)
    }

    /**
     * Retries loading photos after an error.
     * Useful for network-like errors or temporary failures.
     */
    fun retry() {
        loadPhotos()
    }
}

