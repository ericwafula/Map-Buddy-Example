package com.example.mapbuddy.presentation

import android.location.Location

sealed interface MainAction {
    data class SubmitLocationPermissionInfo(
        val acceptedLocationPermission: Boolean,
        val showLocationRationale: Boolean
    ) : MainAction

    data object DismissRationaleDialog : MainAction

    data object OnNavigate : MainAction
}