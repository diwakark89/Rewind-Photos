package com.thewalkersoft.rewindphotos.ui.rewind

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
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for the Rewind screen.
 * Manages state for displaying memories grouped by month with dynamic year filtering and auto-scroll.
 */
@HiltViewModel
class RewindViewModel @Inject constructor(
    private val getAllPhotosUseCase: GetAllPhotosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RewindUiState>(RewindUiState.Loading)
    val uiState: StateFlow<RewindUiState> = _uiState.asStateFlow()

    private val _selectedYear = MutableStateFlow<Int?>(null)
    val selectedYear: StateFlow<Int?> = _selectedYear.asStateFlow()

    private val _scrollToIndex = MutableStateFlow(-1)
    val scrollToIndex: StateFlow<Int> = _scrollToIndex.asStateFlow()

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
                        _scrollToIndex.value = groupedPhotosData.closestSectionIndex

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
            MonthSection(year, month, location, photoList)
        }.sortedWith(compareBy<MonthSection> { it.year }.thenBy { it.month }.reversed())

        // Find closest section to today
        val today = LocalDate.now()
        val closestIndex = findClosestSectionIndex(sections, today.year, today.monthValue - 1, today.dayOfMonth)

        return GroupedPhotosData(sections, closestIndex)
    }

    /**
     * Find the index of the section closest to the selected date.
     * Tries to match exact month, then searches nearby months.
     * The targetDay parameter helps find the best matching section within the same month.
     */
    private fun findClosestSectionIndex(
        sections: List<MonthSection>,
        targetYear: Int,
        targetMonth: Int,
        targetDay: Int
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
            val closestIndex = findClosestSectionIndex(state.groupedPhotosData.sections, year, month, day)
            _scrollToIndex.value = closestIndex
        }
    }

    /**
     * Handle year selection and update scroll position
     */
    fun onYearSelected(year: Int) {
        _selectedYear.value = year
        val state = _uiState.value
        if (state is RewindUiState.Success) {
            // Find first section of selected year
            val yearIndex = state.groupedPhotosData.sections.indexOfFirst { it.year == year }
            if (yearIndex != -1) {
                _scrollToIndex.value = yearIndex
            }
        }
    }

    fun retry() {
        loadPhotos()
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
