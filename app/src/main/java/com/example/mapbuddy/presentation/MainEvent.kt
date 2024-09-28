package com.example.mapbuddy.presentation

sealed interface MainEvent {
    data class OnSuccess(val location: DisplayViewModel.Location) : MainEvent
}