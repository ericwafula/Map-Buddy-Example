package com.example.mapbuddy

import android.location.Location

sealed interface MainAction {
    data class SubmitLocationPermissionInfo(
        val acceptedLocationPermission: Boolean,
        val showLocationRationale: Boolean
    ) : MainAction

    data class SubmitNotificationPermissionInfo(
        val acceptedNotificationPermission: Boolean,
        val showNotificationPermissionRationale: Boolean
    ) : MainAction

    data class OnGetLocationUpdate(val location: Location) : MainAction

    data object DismissRationaleDialog : MainAction
}