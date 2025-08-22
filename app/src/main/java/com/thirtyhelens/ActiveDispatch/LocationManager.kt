package com.thirtyhelens.ActiveDispatch

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class LocationManager(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    private val _locationFlow = MutableStateFlow<Location?>(null)
    val locationFlow: StateFlow<Location?> = _locationFlow.asStateFlow()

    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(scope: CoroutineScope) {
        if (locationCallback != null) return // Already running

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000L
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                _locationFlow.value = result.lastLocation
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

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    suspend fun coordinatesForAddress(address: String): Location? = withContext(Dispatchers.IO) {
        return@withContext try {
            val results = Geocoder(context, Locale.getDefault())
                .getFromLocationName(address, 1)

            results?.firstOrNull()?.let {
                Location("").apply {
                    latitude = it.latitude
                    longitude = it.longitude
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
