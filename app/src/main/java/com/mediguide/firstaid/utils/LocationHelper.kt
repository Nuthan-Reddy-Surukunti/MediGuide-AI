package com.mediguide.firstaid.utils

// Location helper: gets current state/region using fused location and geocoder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context, Locale.getDefault())
    private val tag = "LocationHelper"

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        Log.d(tag, "Location permissions - Fine: $fineLocation, Coarse: $coarseLocation")
        return fineLocation || coarseLocation
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        Log.d(tag, "Location services - GPS: $gpsEnabled, Network: $networkEnabled")
        return gpsEnabled || networkEnabled
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun getCurrentState(): String? {
        if (!hasLocationPermission()) {
            Log.e(tag, "Location permission not granted")
            return null
        }

        if (!isLocationEnabled()) {
            Log.e(tag, "Location services are disabled")
            return null
        }

        return try {
            // First try to get last known location
            var location: Location? = null

            try {
                Log.d(tag, "Attempting to get last known location...")
                location = fusedLocationClient.lastLocation.await()
                Log.d(tag, "Last known location: ${location != null}")
            } catch (e: Exception) {
                Log.w(tag, "Failed to get last known location: ${e.message}")
            }

            // If last location is null or too old, request current location
            if (location == null) {
                Log.d(tag, "Last location unavailable, requesting current location...")
                location = getCurrentLocation()
            }

            if (location != null) {
                Log.d(tag, "Got location: ${location.latitude}, ${location.longitude}")
                getStateFromLocation(location)
            } else {
                Log.e(tag, "Could not obtain location")
                null
            }
        } catch (e: SecurityException) {
            Log.e(tag, "Security exception: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(tag, "Error getting location: ${e.message}", e)
            null
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun getCurrentLocation(): Location? {
        return withTimeoutOrNull(10000L) { // 10 second timeout
            suspendCancellableCoroutine { continuation ->
                try {
                    val locationRequest = LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        5000L // 5 seconds
                    ).apply {
                        setMaxUpdates(1)
                        setWaitForAccurateLocation(false)
                    }.build()

                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            val location = result.lastLocation
                            Log.d(tag, "Current location received: ${location != null}")
                            fusedLocationClient.removeLocationUpdates(this)
                            if (continuation.isActive) {
                                continuation.resume(location)
                            }
                        }
                    }

                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                    ).addOnFailureListener { e ->
                        Log.e(tag, "Failed to request location updates: ${e.message}")
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }

                    continuation.invokeOnCancellation {
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Exception in getCurrentLocation: ${e.message}")
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }
        }
    }

    private suspend fun getStateFromLocation(location: Location): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ - Use new geocoding API with coroutine
                suspendCancellableCoroutine { continuation ->
                    try {
                        geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                            if (addresses.isNotEmpty()) {
                                val state = addresses[0].adminArea
                                Log.d(tag, "State from geocoder (API 33+): $state")
                                if (continuation.isActive) {
                                    continuation.resume(state)
                                }
                            } else {
                                Log.w(tag, "No addresses found for location (API 33+)")
                                if (continuation.isActive) {
                                    continuation.resume(null)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Error in geocoder callback: ${e.message}")
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                }
            } else {
                // Legacy API
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val state = addresses[0].adminArea
                    Log.d(tag, "State from geocoder (legacy): $state")
                    state
                } else {
                    Log.w(tag, "No addresses found for location")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error getting state from location: ${e.message}", e)
            null
        }
    }
}
