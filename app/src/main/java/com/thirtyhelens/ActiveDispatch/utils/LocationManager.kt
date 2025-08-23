package com.thirtyhelens.ActiveDispatch.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

interface LocationProvider {
    val locationFlow: StateFlow<LatLng?>
    fun startLocationUpdates(scope: CoroutineScope)
    fun stopLocationUpdates()
}

class LocationManager(private val context: Context) : LocationProvider {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    private val _locationFlow = MutableStateFlow<LatLng?>(null)
    override val locationFlow: StateFlow<LatLng?> = _locationFlow.asStateFlow()

    private var locationCallback: LocationCallback? = null

    private fun Location.toLatLng(): LatLng = LatLng(latitude, longitude)

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates(scope: CoroutineScope) {
        // Already running
        if (locationCallback != null) return

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000L // 10s
        )
            .setMinUpdateDistanceMeters(10f) // throttle by distance a bit
            .build()

        // Try to seed with last known location (non-blocking)
        scope.launch {
            val last = runCatching { fusedClient.lastLocation }.getOrNull()
            last?.addOnSuccessListener { loc -> loc?.let { _locationFlow.value = it.toLatLng() } }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc: Location? = result.lastLocation
                if (loc != null) {
                    _locationFlow.value = loc.toLatLng()
                }
            }
        }

        scope.launch {
            fusedClient.requestLocationUpdates(
                request,
                locationCallback!!,
                Looper.getMainLooper()
            )
        }
    }

    override fun stopLocationUpdates() {
        locationCallback?.let {
            fusedClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    /**
     * You likely don't need this anymore since the API now provides lat/lon.
     * Keeping it for fallback/tools, but aligned to LatLng and marked deprecated.
     */
    @Deprecated("API now provides lat/lon. Prefer using payload coordinates.")
    suspend fun coordinatesForAddress(address: String): LatLng? = withContext(Dispatchers.IO) {
        runCatching {
            val results = Geocoder(context, Locale.getDefault())
                .getFromLocationName(address, 1)
            results?.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
        }.getOrNull()
    }
}

class FakeLocationProvider(
    latLng: LatLng = LatLng(36.1627, -86.7816) // downtown Nashville
) : LocationProvider {
    private val _loc = MutableStateFlow<LatLng?>(latLng)
    override val locationFlow: StateFlow<LatLng?> = _loc
    override fun startLocationUpdates(scope: CoroutineScope) { /* no-op */ }
    override fun stopLocationUpdates() { /* no-op */ }
}