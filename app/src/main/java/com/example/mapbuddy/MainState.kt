package com.example.mapbuddy

import android.location.Location

data class MainState(
    val showNotificationRationale: Boolean = false,
    val showLocationRationale: Boolean = false,
    val location: Location? = null
)
