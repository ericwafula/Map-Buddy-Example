package com.example.mapbuddy.presentation.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@RequiresApi(Build.VERSION_CODES.R)
suspend fun Context.getLocation(): Location {
    return suspendCancellableCoroutine { continuation ->
        val locationManager = getSystemService<LocationManager>()!!
        val cancellationSignal = CancellationSignal()

        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocationPermission && hasCoarseLocationPermission) {
            locationManager.getCurrentLocation(
                LocationManager.NETWORK_PROVIDER,
                cancellationSignal,
                mainExecutor
            ) { location ->
                continuation.resume(location)
            }
        } else {
            continuation.resumeWithException(RuntimeException("User has not granted location permissions"))
        }

        continuation.invokeOnCancellation {
            cancellationSignal.cancel()
        }
    }
}