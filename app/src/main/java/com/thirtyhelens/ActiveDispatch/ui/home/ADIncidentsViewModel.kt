package com.thirtyhelens.ActiveDispatch.ui.home

import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.thirtyhelens.ActiveDispatch.models.ADIncident
import com.thirtyhelens.ActiveDispatch.models.City
import com.thirtyhelens.ActiveDispatch.ui.mapping.IncidentMapper
import com.thirtyhelens.ActiveDispatch.utils.LocationProvider
import com.thirtyhelens.ActiveDispatch.utils.ADNetworkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

open class ADIncidentsViewModel(
    private val locationProvider: LocationProvider
) : ViewModel() {

    sealed class LoadState {
        data object Idle : LoadState()
        data object Loading : LoadState()
        data class Success(val data: List<ADIncident>) : LoadState()
        sealed class Error : LoadState() {
            data object LocationPermissionRequired : Error()
            data object LocationUnavailable : Error()
            data class Network(val cause: Throwable) : Error()
        }
    }

    private val _incidents = MutableStateFlow<List<ADIncident>>(emptyList())
    open val incidents: StateFlow<List<ADIncident>> = _incidents.asStateFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()
    val waitForLocationMs: Long = 8_000L
    init {
        locationProvider.startLocationUpdates(viewModelScope)
    }

    suspend fun getIncidentsSuspend(
        city: City,
        hasLocationPermission: Boolean
    ) {
        _loadState.value = LoadState.Loading

        val userLatLng: LatLng? = if (hasLocationPermission) {
            // Try GPS first (timeout), then fall back to city center
            withTimeoutOrNull(waitForLocationMs) {
                locationProvider.locationFlow.filterNotNull().first()
            } ?: city.fallbackLatLng
        } else {
            // No permission, use city fallback only
            city.fallbackLatLng
        }

        // If we still don't have a location, error out appropriately
        if (userLatLng == null) {
            _incidents.value = emptyList()
            _loadState.value = if (hasLocationPermission) {
                LoadState.Error.LocationUnavailable
            } else {
                LoadState.Error.LocationPermissionRequired
            }
            return
        }

        if (userLatLng == null) {
            _incidents.value = emptyList()
            _loadState.value = LoadState.Error.LocationUnavailable
            return
        }

        val network = runCatching { ADNetworkManager.fetchCity(city) }
        network.onSuccess { resp ->
            val list = resp.places.map { IncidentMapper.toUi(it, userLatLng) }
            _incidents.value = list
            _loadState.value = LoadState.Success(list)
        }.onFailure { t ->
            _incidents.value = emptyList()
            _loadState.value = LoadState.Error.Network(t)
        }
    }

    fun getIncidents(
        city: City,
        hasLocationPermission: Boolean
    ) {
        viewModelScope.launch {
            getIncidentsSuspend(
                city = city,
                hasLocationPermission = hasLocationPermission
            )
        }
    }

    private fun isEmulator(): Boolean {
        val fp = Build.FINGERPRINT
        return Build.PRODUCT.contains("sdk", ignoreCase = true) ||
                Build.MANUFACTURER.contains(
                    "google",
                    ignoreCase = true
                ) && fp.contains("generic") ||
                fp.startsWith("generic") || fp.startsWith("unknown") ||
                Build.MODEL.contains("Emulator", true) ||
                Build.MODEL.contains("Android SDK built for x86", true)
    }
}