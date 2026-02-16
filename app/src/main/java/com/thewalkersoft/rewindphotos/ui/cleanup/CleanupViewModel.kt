package com.thewalkersoft.rewindphotos.ui.cleanup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thewalkersoft.rewindphotos.domain.usecase.DetectDuplicatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Cleanup screen.
 *
 * Manages:
 * - Duplicate photo detection
 * - UI state for loading/results/errors
 * - User interactions (retry, etc.)
 *
 * Uses DetectDuplicatesUseCase to perform duplicate detection.
 */
@HiltViewModel
class CleanupViewModel @Inject constructor(
    private val detectDuplicatesUseCase: DetectDuplicatesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CleanupUiState>(CleanupUiState.Loading)
    val uiState: StateFlow<CleanupUiState> = _uiState.asStateFlow()

    init {
        detectDuplicates()
    }

    /**
     * Triggers duplicate detection.
     * Updates UI state through the state flow.
     */
    fun detectDuplicates() {
        viewModelScope.launch {
            _uiState.value = CleanupUiState.Loading
            try {
                detectDuplicatesUseCase(similarityThreshold = 0.90f).collect { groups ->
                    _uiState.value = if (groups.isEmpty()) {
                        CleanupUiState.NoDuplicates
                    } else {
                        val totalPhotos = groups.sumOf { it.totalPhotos }
                        val potentialSpaceSavings = groups.sumOf { it.potentialDuplicates }

                        CleanupUiState.DuplicatesFound(
                            groups = groups,
                            totalPhotos = totalPhotos,
                            potentialSpaceSavings = potentialSpaceSavings
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = CleanupUiState.Error(
                    "Failed to detect duplicates: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    /**
     * Retries duplicate detection after an error.
     */
    fun retry() {
        detectDuplicates()
    }
}
