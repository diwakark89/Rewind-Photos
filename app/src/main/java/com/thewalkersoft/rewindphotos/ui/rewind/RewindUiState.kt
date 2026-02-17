package com.thewalkersoft.rewindphotos.ui.rewind

import com.thewalkersoft.rewindphotos.domain.model.MemoryGroup

/**
 * UI state for the Rewind screen.
 */
data class RewindUiState(
    val isLoading: Boolean = true,
    val memoryGroups: List<MemoryGroup> = emptyList()
)

