package com.example.mapbuddy.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.example.mapbuddy.domain.LocationRepository
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DefaultLocationRepository(
    private val context: Context
) : LocationRepository {
    override fun getLocationUpdates(): Flow<Location> {
        return callbackFlow {
            val locationManager = context.getSystemService<LocationManager>()!!
            val client = LocationServices.getFusedLocationProviderClient(context)
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                3000L
            ).build()

            var isGpsEnabled = false
            var isNetworkEnabled = false

            while (!isGpsEnabled && !isNetworkEnabled) {
                isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (!isGpsEnabled && !isNetworkEnabled) {
                    delay(3000)
                }
            }

            val hasFineLocationPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasFineLocationPermission && hasCoarseLocationPermission) {
                val callback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        super.onLocationResult(result)

                        result.locations.lastOrNull()?.let { location ->
                            trySend(location)
                        }
                    }
                }

                client.requestLocationUpdates(
                    request,
                    callback,
                    context.mainLooper
                )

                awaitClose {
                    client.removeLocationUpdates(callback)
                }
            } else {
                close()
            }
        }
    }
}