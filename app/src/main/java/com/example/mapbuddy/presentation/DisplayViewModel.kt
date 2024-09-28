package com.example.mapbuddy.presentation

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

class DisplayViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val location = savedStateHandle.getStateFlow<Location?>("location", null)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    @Parcelize
    @Serializable
    data class Location(val lat: Double?, val lon: Double?) : Parcelable
}