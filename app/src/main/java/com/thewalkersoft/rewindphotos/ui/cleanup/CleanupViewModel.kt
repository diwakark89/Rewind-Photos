package com.thewalkersoft.rewindphotos.ui.cleanup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thewalkersoft.rewindphotos.domain.model.PhotoGroup
import com.thewalkersoft.rewindphotos.domain.repository.PhotoRepository
import com.thewalkersoft.rewindphotos.domain.usecase.CleanupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Cleanup screen.
 * Manages duplicate photo groups and deletion operations.
 */
@HiltViewModel
class CleanupViewModel @Inject constructor(
    private val cleanupUseCase: CleanupUseCase,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CleanupUiState>(CleanupUiState.Loading)
    val uiState: StateFlow<CleanupUiState> = _uiState.asStateFlow()

    private val _groups = MutableStateFlow<List<PhotoGroup>>(emptyList())
    val groups: StateFlow<List<PhotoGroup>> = _groups.asStateFlow()

    init {
        loadDuplicates()
    }

    fun loadDuplicates() {
        viewModelScope.launch {
            _uiState.value = CleanupUiState.Loading
            try {
                cleanupUseCase().collect { photoGroups ->
                    _groups.value = photoGroups
                    _uiState.value = if (photoGroups.isEmpty()) {
                        CleanupUiState.Empty
                    } else {
                        CleanupUiState.Success(photoGroups.size)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = CleanupUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun keepBestInGroup(groupIndex: Int) {
        val currentGroups = _groups.value.toMutableList()
        if (groupIndex < currentGroups.size) {
            val group = currentGroups[groupIndex]
            val bestPhoto = cleanupUseCase.identifyBestPhoto(group)
            val deleteIds = group.photos.map { it.id }.filter { it != bestPhoto.id }.toSet()
            currentGroups[groupIndex] = group.copy(selectedForDeletion = deleteIds)
            _groups.value = currentGroups
        }
    }

    fun togglePhotoSelection(groupIndex: Int, photoId: Long) {
        val currentGroups = _groups.value.toMutableList()
        if (groupIndex < currentGroups.size) {
            currentGroups[groupIndex] = currentGroups[groupIndex].togglePhotoSelection(photoId)
            _groups.value = currentGroups
        }
    }

    fun selectAllInGroup(groupIndex: Int) {
        val currentGroups = _groups.value.toMutableList()
        if (groupIndex < currentGroups.size) {
            currentGroups[groupIndex] = currentGroups[groupIndex].selectAll()
            _groups.value = currentGroups
        }
    }

    fun deselectAllInGroup(groupIndex: Int) {
        val currentGroups = _groups.value.toMutableList()
        if (groupIndex < currentGroups.size) {
            currentGroups[groupIndex] = currentGroups[groupIndex].deselectAll()
            _groups.value = currentGroups
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            _uiState.value = CleanupUiState.Deleting()
            try {
                val photosToDelete = _groups.value.flatMap { it.selectedForDeletion }
                if (photosToDelete.isNotEmpty()) {
                    val deletedCount = photoRepository.deletePhotos(photosToDelete)

                    // Remove deleted photos from groups
                    val deletedSet = photosToDelete.toSet()
                    val updatedGroups = _groups.value.mapNotNull { group ->
                        val remainingPhotos = group.photos.filter { it.id !in deletedSet }
                        if (remainingPhotos.size > 1) {
                            group.copy(
                                photos = remainingPhotos,
                                selectedForDeletion = emptySet()
                            )
                        } else {
                            null
                        }
                    }

                    _groups.value = updatedGroups
                    _uiState.value = CleanupUiState.DeleteSuccess(deletedCount)
                } else {
                    _uiState.value = CleanupUiState.Error("No photos selected for deletion")
                }
            } catch (e: Exception) {
                _uiState.value = CleanupUiState.Error("Failed to delete photos: ${e.message}")
            }
        }
    }

    fun retry() {
        loadDuplicates()
    }
}

/**
 * Represents the different states of the Cleanup UI.
 */
sealed class CleanupUiState {
    data object Loading : CleanupUiState()
    data object Empty : CleanupUiState()
    data class Success(val groupCount: Int) : CleanupUiState()
    data class Deleting(val dummy: Unit = Unit) : CleanupUiState()
    data class DeleteSuccess(val deletedCount: Int) : CleanupUiState()
    data class Error(val message: String) : CleanupUiState()
}
