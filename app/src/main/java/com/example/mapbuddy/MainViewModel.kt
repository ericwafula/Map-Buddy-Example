package com.example.mapbuddy

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel : ViewModel() {
    var state by mutableStateOf(MainState())
        private set

    private val _hasLocationPermission = MutableStateFlow(false)

    fun onAction(action: MainAction) {
        when (action) {
            is MainAction.SubmitLocationPermissionInfo -> {
                _hasLocationPermission.value = action.acceptedLocationPermission
                state = state.copy(
                    showLocationRationale = action.showLocationRationale
                )
            }
            is MainAction.SubmitNotificationPermissionInfo -> {
                state = state.copy(
                    showNotificationRationale = action.showNotificationPermissionRationale
                )
            }
            is MainAction.DismissRationaleDialog -> {
                state = state.copy(
                    showNotificationRationale = false,
                    showLocationRationale = false
                )
            }

            is MainAction.OnGetLocationUpdate -> {
                state = state.copy(
                    location = action.location
                )
            }
        }
    }

}