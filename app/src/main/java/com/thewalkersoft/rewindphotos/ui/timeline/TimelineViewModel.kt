package com.thewalkersoft.rewindphotos.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.domain.usecase.GetAllPhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for the Timeline screen.
 * Manages state for displaying memories organized by timeline with dynamic year filtering.
 */
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getAllPhotosUseCase: GetAllPhotosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TimelineUiState>(TimelineUiState.Loading)
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    init {
        loadPhotos()
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = TimelineUiState.Loading
            try {
                getAllPhotosUseCase().collect { photos ->
                    if (photos.isEmpty()) {
                        _uiState.value = TimelineUiState.Empty
                    } else {
                        // Extract available years from photos and sort in descending order
                        val availableYears = photos
                            .map { photo -> getYearFromTimestamp(photo.dateTaken) }
                            .distinct()
                            .sorted()
                            .reversed()

                        // Get current date
                        val calendar = Calendar.getInstance()
                        val currentMonth = calendar.get(Calendar.MONTH)
                        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                        val currentDate = formatDate(currentMonth, currentDay)

                        _uiState.value = TimelineUiState.Success(
                            allPhotos = photos,
                            availableYears = availableYears,
                            currentDate = currentDate,
                            currentMonth = currentMonth,
                            currentDay = currentDay
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = TimelineUiState.Error("Failed to load photos: ${e.message}")
            }
        }
    }

    private fun getYearFromTimestamp(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.YEAR)
    }

    private fun formatDate(month: Int, day: Int): String {
        val monthNames = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return "${monthNames[month]} $day"
    }

    fun retry() {
        loadPhotos()
    }
}

/**
 * Represents the different states of the Timeline screen UI.
 */
sealed class TimelineUiState {
    data object Loading : TimelineUiState()
    data object Empty : TimelineUiState()
    data class Success(
        val allPhotos: List<Photo>,
        val availableYears: List<Int>,
        val currentDate: String,
        val currentMonth: Int,
        val currentDay: Int
    ) : TimelineUiState()

    data class Error(val message: String) : TimelineUiState()
}

