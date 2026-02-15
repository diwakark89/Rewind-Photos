package com.thewalkersoft.rewindphotos.ui.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.domain.usecase.GetAllPhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the photos screen.
 * Manages UI state and business logic for displaying photos.
 */
@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val getAllPhotosUseCase: GetAllPhotosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PhotosUiState>(PhotosUiState.Loading)
    val uiState: StateFlow<PhotosUiState> = _uiState.asStateFlow()

    init {
        loadPhotos()
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = PhotosUiState.Loading
            try {
                getAllPhotosUseCase().collect { photos ->
                    _uiState.value = if (photos.isEmpty()) {
                        PhotosUiState.Empty
                    } else {
                        PhotosUiState.Success(photos)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PhotosUiState.Error("Failed to load photos: ${e.message}")
            }
        }
    }

    fun retry() {
        loadPhotos()
    }
}

/**
 * Represents the different states of the Photos UI.
 */
sealed class PhotosUiState {
    data object Loading : PhotosUiState()
    data object Empty : PhotosUiState()
    data class Success(val photos: List<Photo>) : PhotosUiState()
    data class Error(val message: String) : PhotosUiState()
}

