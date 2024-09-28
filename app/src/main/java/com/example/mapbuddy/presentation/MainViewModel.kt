package com.example.mapbuddy.presentation

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapbuddy.domain.LocationRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: LocationRepository
) : ViewModel() {
    var state by mutableStateOf(MainState())
        private set
    private val _event = Channel<MainEvent>()
    val event = _event.receiveAsFlow()

    private val _hasLocationPermission = MutableStateFlow(false)

    private val _locationFlow = MutableStateFlow<Location?>(null)
    val locationFlow = _locationFlow
        .onStart {
            startLocationTracking()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun onAction(action: MainAction) {
        when (action) {
            is MainAction.SubmitLocationPermissionInfo -> {
                _hasLocationPermission.value = action.acceptedLocationPermission
                state = state.copy(
                    showLocationRationale = action.showLocationRationale
                )
            }

            is MainAction.DismissRationaleDialog -> {
                state = state.copy(
                    showLocationRationale = false
                )
            }

            is MainAction.OnNavigate -> viewModelScope.launch {
                _event.send(
                    element = MainEvent.OnSuccess(
                        DisplayViewModel.Location(
                            lat = locationFlow.value?.latitude,
                            lon = locationFlow.value?.longitude
                        )
                    )
                )
            }
        }
    }

    private fun startLocationTracking() {
        viewModelScope.launch {
            repository.getLocationUpdates().collect { location ->
                _locationFlow.update { location }
            }
        }
    }
}