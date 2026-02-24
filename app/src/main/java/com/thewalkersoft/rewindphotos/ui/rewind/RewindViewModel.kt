package com.thewalkersoft.rewindphotos.ui.rewind

import android.content.IntentSender
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thewalkersoft.rewindphotos.domain.model.GroupedPhotosData
import com.thewalkersoft.rewindphotos.domain.model.MonthSection
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.domain.repository.PhotoRepository
import com.thewalkersoft.rewindphotos.domain.usecase.GetAllPhotosUseCase
import com.thewalkersoft.rewindphotos.domain.usecase.RequestDeletePhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for the Rewind screen.
 * Manages state for displaying memories grouped by month with dynamic year filtering and auto-scroll.
 * Also manages photo selection, sharing, and deletion.
 */
@HiltViewModel
class RewindViewModel @Inject constructor(
    private val getAllPhotosUseCase: GetAllPhotosUseCase,
    private val photoRepository: PhotoRepository,
    private val requestDeletePhotosUseCase: RequestDeletePhotosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RewindUiState>(RewindUiState.Loading)
    val uiState: StateFlow<RewindUiState> = _uiState.asStateFlow()

    private val _selectedYear = MutableStateFlow<Int?>(null)
    val selectedYear: StateFlow<Int?> = _selectedYear.asStateFlow()

    private val _scrollRequest = MutableStateFlow(ScrollRequest(index = -1, animate = false))
    val scrollRequest: StateFlow<ScrollRequest> = _scrollRequest.asStateFlow()

    private val _selectedPhotos = MutableStateFlow<Set<Long>>(emptySet())
    val selectedPhotos: StateFlow<Set<Long>> = _selectedPhotos.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _deleteIntentSender = MutableStateFlow<IntentSender?>(null)
    val deleteIntentSender: StateFlow<IntentSender?> = _deleteIntentSender.asStateFlow()

    private var photosByYearCache: Map<Int, List<Photo>> = emptyMap()
    private var yearFirstSectionIndexCache: Map<Int, Int> = emptyMap()

    init {
        loadPhotos()
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = RewindUiState.Loading
            try {
                getAllPhotosUseCase().collect { photos ->
                    if (photos.isEmpty()) {
                        _uiState.value = RewindUiState.Empty
                    } else {
                        // Group photos by month and year
                        val groupedPhotosData = groupPhotosByMonth(photos)

                        // Extract available years from grouped data
                        val availableYears = groupedPhotosData.sections
                            .map { it.year }
                            .distinct()
                            .sorted()
                            .reversed()

                        // Get current date
                        val calendar = Calendar.getInstance()
                        val currentYear = calendar.get(Calendar.YEAR)
                        val currentMonth = calendar.get(Calendar.MONTH)
                        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

                        // Set default selected year to current year
                        _selectedYear.value = currentYear

                        // Auto-scroll to current date on initial load
                        _scrollRequest.value = ScrollRequest(
                            index = groupedPhotosData.closestSectionIndex,
                            animate = false
                        )

                        _uiState.value = RewindUiState.Success(
                            groupedPhotosData = groupedPhotosData,
                            availableYears = availableYears,
                            currentYear = currentYear,
                            currentMonth = currentMonth,
                            currentDay = currentDay
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = RewindUiState.Error("Failed to load photos: ${e.message}")
            }
        }
    }

    /**
     * Group photos by month and year in reverse chronological order.
     * Also extracts location info per section.
     */
    private fun groupPhotosByMonth(photos: List<Photo>): GroupedPhotosData {
        val zoneId = ZoneId.systemDefault()
        val monthMap = mutableMapOf<Pair<Int, Int>, MutableList<Photo>>() // (year, month) -> photos

        val photosByYear = photos
            .asSequence()
            .filter { it.dateTaken > 0L }
            .groupBy {
                Instant.ofEpochMilli(it.dateTaken).atZone(zoneId).year
            }

        photosByYearCache = photosByYear

        // Group photos by (year, month)
        for ((_, yearPhotos) in photosByYear) {
            for (photo in yearPhotos) {
                val photoDate = Instant.ofEpochMilli(photo.dateTaken).atZone(zoneId).toLocalDate()
                val key = Pair(photoDate.year, photoDate.monthValue - 1) // month 0-11
                monthMap.getOrPut(key) { mutableListOf() }.add(photo)
            }
        }

        // Create month sections and sort in reverse chronological order
        val sections = monthMap.map { (yearMonth, photoList) ->
            val (year, month) = yearMonth
            // Use location from first photo in section (or null if no location data)
            val location = photoList.firstOrNull()?.location
            MonthSection(year, month, location, photoList)
        }.sortedWith(compareBy<MonthSection> { it.year }.thenBy { it.month }.reversed())

        val yearIndexMap = mutableMapOf<Int, Int>()
        sections.forEachIndexed { index, section ->
            yearIndexMap.putIfAbsent(section.year, index)
        }
        yearFirstSectionIndexCache = yearIndexMap

        // Find closest section to today
        val today = LocalDate.now()
        val closestIndex = findClosestSectionIndex(sections, today.year, today.monthValue - 1)

        return GroupedPhotosData(sections, closestIndex)
    }

    /**
     * Find the index of the section closest to the selected date.
     * Tries to match exact month, then searches nearby months.
     */
    private fun findClosestSectionIndex(
        sections: List<MonthSection>,
        targetYear: Int,
        targetMonth: Int
    ): Int {
        if (sections.isEmpty()) return 0

        // First, try to find exact year-month match
        val exactIndex = sections.indexOfFirst { it.year == targetYear && it.month == targetMonth }
        if (exactIndex != -1) return exactIndex

        // If no exact match, find closest month
        val yearMonthSections = sections.filter { it.year == targetYear }
        if (yearMonthSections.isNotEmpty()) {
            // Find section with month closest to target
            val closestSection = yearMonthSections.minByOrNull {
                kotlin.math.abs(it.month - targetMonth)
            }
            if (closestSection != null) {
                return sections.indexOf(closestSection)
            }
        }

        // If no year match, find closest year
        val closestYearSection = sections.minByOrNull {
            kotlin.math.abs(it.year - targetYear)
        }

        return sections.indexOf(closestYearSection)
    }

    /**
     * Handle date selection and update scroll position
     */
    fun onDateSelected(year: Int, month: Int, day: Int) {
        val state = _uiState.value
        if (state is RewindUiState.Success) {
            val closestIndex = findClosestSectionIndex(state.groupedPhotosData.sections, year, month)
            _scrollRequest.value = ScrollRequest(index = closestIndex, animate = true)
        }
    }

    /**
     * Handle year selection and update scroll position
     */
    fun onYearSelected(year: Int) {
        _selectedYear.value = year
        val state = _uiState.value
        if (state is RewindUiState.Success) {
            val yearIndex = yearFirstSectionIndexCache[year]
                ?: state.groupedPhotosData.sections.indexOfFirst { it.year == year }
            if (yearIndex != -1) {
                _scrollRequest.value = ScrollRequest(index = yearIndex, animate = false)
            }
        }
    }

    fun retry() {
        loadPhotos()
    }

    /**
     * Toggle selection of a single photo
     */
    fun togglePhotoSelection(photoId: Long) {
        val currentSelection = _selectedPhotos.value.toMutableSet()
        if (currentSelection.contains(photoId)) {
            currentSelection.remove(photoId)
            android.util.Log.d("RewindViewModel", "Deselected photo ID: $photoId")
        } else {
            currentSelection.add(photoId)
            android.util.Log.d("RewindViewModel", "Selected photo ID: $photoId")
        }
        _selectedPhotos.value = currentSelection
        android.util.Log.d("RewindViewModel", "Current selection: ${_selectedPhotos.value}")

        // Enter selection mode if not already
        if (currentSelection.isNotEmpty() && !_isSelectionMode.value) {
            _isSelectionMode.value = true
            android.util.Log.d("RewindViewModel", "Entered selection mode")
        }
    }

    /**
     * Select all photos in a specific month
     */
    fun selectAllPhotosInMonth(year: Int, month: Int) {
        val state = _uiState.value as? RewindUiState.Success ?: return
        val currentSelection = _selectedPhotos.value.toMutableSet()
        val section = state.groupedPhotosData.sections.find { it.year == year && it.month == month }
            ?: return

        val sectionPhotoIds = section.photos.map { it.id }.toSet()
        android.util.Log.d("RewindViewModel", "Month section has ${sectionPhotoIds.size} photos")

        // If all photos in this month are already selected, deselect them
        if (sectionPhotoIds.all { it in currentSelection }) {
            currentSelection.removeAll(sectionPhotoIds)
            android.util.Log.d("RewindViewModel", "Deselected all photos in month $month/$year")
        } else {
            // Otherwise select all
            currentSelection.addAll(sectionPhotoIds)
            android.util.Log.d("RewindViewModel", "Selected all photos in month $month/$year")
        }

        _selectedPhotos.value = currentSelection
        android.util.Log.d("RewindViewModel", "Current selection after month toggle: ${_selectedPhotos.value}")

        if (currentSelection.isNotEmpty()) {
            _isSelectionMode.value = true
        }
    }

    /**
     * Select all visible photos
     */
    fun selectAllPhotos() {
        val state = _uiState.value as? RewindUiState.Success ?: return
        val currentSelection = _selectedPhotos.value.toMutableSet()
        val allPhotoIds = state.groupedPhotosData.sections
            .flatMap { it.photos }
            .map { it.id }
            .toSet()

        // If all photos are selected, deselect them
        if (allPhotoIds.all { it in currentSelection }) {
            currentSelection.clear()
            _isSelectionMode.value = false
        } else {
            // Otherwise select all
            currentSelection.addAll(allPhotoIds)
            _isSelectionMode.value = true
        }

        _selectedPhotos.value = currentSelection
    }

    /**
     * Clear all selections and exit selection mode
     */
    fun clearSelection() {
        _selectedPhotos.value = emptySet()
        _isSelectionMode.value = false
    }

    /**
     * Delete selected photos
     * On Android 13+, this will trigger a system permission dialog
     */
    fun deleteSelectedPhotos() {
        val selectedIds = _selectedPhotos.value.toList()
        if (selectedIds.isEmpty()) {
            android.util.Log.w("RewindViewModel", "No photos selected for deletion")
            return
        }

        android.util.Log.d("RewindViewModel", "=== DELETE OPERATION STARTED ===")
        android.util.Log.d("RewindViewModel", "Selected photo IDs to delete: $selectedIds")
        android.util.Log.d("RewindViewModel", "Total photos selected: ${selectedIds.size}")
        android.util.Log.d("RewindViewModel", "Android API Level: ${Build.VERSION.SDK_INT}")

        viewModelScope.launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Android 11+ (API 30+) - Use MediaStore.createDeleteRequest
                    android.util.Log.d("RewindViewModel", "Using MediaStore.createDeleteRequest for Android ${Build.VERSION.SDK_INT}")
                    val intentSender = requestDeletePhotosUseCase.createDeleteRequest(selectedIds)

                    if (intentSender != null) {
                        android.util.Log.d("RewindViewModel", "✓ Delete request created - will show system permission dialog")
                        _deleteIntentSender.value = intentSender
                    } else {
                        android.util.Log.e("RewindViewModel", "✗ Failed to create delete request")
                        android.util.Log.e("RewindViewModel", "User will need to manually delete photos or grant permission")
                    }
                } else {
                    // Android 10 and below - Try direct deletion
                    android.util.Log.d("RewindViewModel", "Using direct deletion for Android ${Build.VERSION.SDK_INT}")
                    val deletedCount = photoRepository.deletePhotos(selectedIds)
                    android.util.Log.d("RewindViewModel", "Repository returned deletedCount: $deletedCount")

                    if (deletedCount == 0) {
                        android.util.Log.e("RewindViewModel", "WARNING: Repository deleted 0 photos! Check permissions.")
                    } else {
                        android.util.Log.d("RewindViewModel", "Successfully deleted $deletedCount photos")
                    }

                    clearSelection()
                    loadPhotos()
                }

                android.util.Log.d("RewindViewModel", "=== DELETE OPERATION COMPLETED ===")
            } catch (e: Exception) {
                android.util.Log.e("RewindViewModel", "Exception in deleteSelectedPhotos: ${e.message}", e)
                android.util.Log.e("RewindViewModel", "Stack trace:", e)
            }
        }
    }

    /**
     * Called after user grants permission via system dialog
     */
    fun onDeletePermissionGranted() {
        android.util.Log.d("RewindViewModel", "Delete permission granted by user")
        _deleteIntentSender.value = null
        clearSelection()
        loadPhotos()
    }

    /**
     * Called when user denies permission via system dialog
     */
    fun onDeletePermissionDenied() {
        android.util.Log.w("RewindViewModel", "Delete permission denied by user")
        _deleteIntentSender.value = null
        // Don't clear selection so user can try again
    }

    /**
     * Clear the delete intent sender (after handling)
     */
    fun clearDeleteIntent() {
        _deleteIntentSender.value = null
    }

    /**
     * Get the list of selected photos for sharing
     */
    fun getSelectedPhotoUris(): List<String> {
        val state = _uiState.value as? RewindUiState.Success ?: return emptyList()
        val selectedIds = _selectedPhotos.value
        return state.groupedPhotosData.sections
            .flatMap { it.photos }
            .filter { it.id in selectedIds }
            .map { it.uri }
    }
}

/**
 * Represents the different states of the Rewind screen UI.
 */
sealed class RewindUiState {
    data object Loading : RewindUiState()
    data object Empty : RewindUiState()
    data class Success(
        val groupedPhotosData: GroupedPhotosData,
        val availableYears: List<Int>,
        val currentYear: Int,
        val currentMonth: Int,
        val currentDay: Int
    ) : RewindUiState()

    data class Error(val message: String) : RewindUiState()
}

/**
 * Scroll request details for jump/animate behavior.
 */
data class ScrollRequest(
    val index: Int,
    val animate: Boolean
)
