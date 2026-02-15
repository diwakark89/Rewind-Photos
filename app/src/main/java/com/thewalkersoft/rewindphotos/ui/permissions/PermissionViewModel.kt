package com.thewalkersoft.rewindphotos.ui.permissions

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for managing permission state and requests.
 */
@HiltViewModel
class PermissionViewModel @Inject constructor() : ViewModel() {

    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.NotRequested)
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    fun onPermissionGranted() {
        _permissionState.value = PermissionState.Granted
    }

    fun onPermissionDenied() {
        _permissionState.value = PermissionState.Denied
    }

    fun onPermissionRequested() {
        _permissionState.value = PermissionState.Requesting
    }

    fun resetPermissionState() {
        _permissionState.value = PermissionState.NotRequested
    }
}

/**
 * Represents the different states of permission.
 */
sealed class PermissionState {
    data object NotRequested : PermissionState()
    data object Requesting : PermissionState()
    data object Granted : PermissionState()
    data object Denied : PermissionState()
}

