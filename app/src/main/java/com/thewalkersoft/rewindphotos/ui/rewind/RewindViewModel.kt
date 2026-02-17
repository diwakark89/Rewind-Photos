package com.thewalkersoft.rewindphotos.ui.rewind

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thewalkersoft.rewindphotos.di.DefaultDispatcher
import com.thewalkersoft.rewindphotos.domain.model.MemoryGroup
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.domain.usecase.GetAllPhotosUseCase
import com.thewalkersoft.rewindphotos.domain.usecase.GetMemoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for the Rewind screen.
 */
@HiltViewModel
class RewindViewModel @Inject constructor(
    private val getAllPhotosUseCase: GetAllPhotosUseCase,
    private val getMemoriesUseCase: GetMemoriesUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(RewindUiState())
    val uiState: StateFlow<RewindUiState> = _uiState.asStateFlow()

    private var cachedMemories: List<MemoryGroup> = emptyList()
    private var lastPhotos: List<Photo> = emptyList()

    init {
        loadMemories()
    }

    private fun loadMemories() {
        viewModelScope.launch {
            getAllPhotosUseCase().collect { photos ->
                if (photos == lastPhotos) {
                    _uiState.value = RewindUiState(
                        isLoading = false,
                        memoryGroups = cachedMemories
                    )
                    return@collect
                }

                lastPhotos = photos
                val memories = withContext(defaultDispatcher) {
                    getMemoriesUseCase(photos)
                }

                cachedMemories = memories
                _uiState.value = RewindUiState(
                    isLoading = false,
                    memoryGroups = memories
                )
            }
        }
    }
}

