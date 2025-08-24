package com.thirtyhelens.ActiveDispatch.ui.home

import android.os.Build
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
import kotlinx.coroutines.launch

open class ADIncidentsViewModel(
    private val locationProvider: LocationProvider
) : ViewModel() {

    sealed class LoadState {
        data object Idle : LoadState()
        data object Loading : LoadState()
        data class Success(val data: List<ADIncident>) : LoadState()
        sealed class Error : LoadState() {
            // No user location available (permissions off/denied/GPS off).
            data object LocationUnavailable : Error()

            //Network failed when fetching incidents.
            data class Network(val cause: Throwable) : Error()
        }
    }

    private val _incidents = MutableStateFlow<List<ADIncident>>(emptyList())
    open val incidents: StateFlow<List<ADIncident>> = _incidents.asStateFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    init {
        locationProvider.startLocationUpdates(viewModelScope)
    }

    fun getIncidents(city: City) {
        viewModelScope.launch {
            _loadState.value = LoadState.Loading

            // Snapshot the latest location (don’t suspend waiting on GPS).
            val snapshot: LatLng? = locationProvider.locationFlow.value

            // In emulator we allow a city fallback; on real device we surface a location error.
            val userLatLng: LatLng? = when {
                snapshot != null -> snapshot
                isEmulator() -> city.fallbackLatLng
                else -> null
            }

            if (userLatLng == null) {
                _incidents.value = emptyList()
                _loadState.value = LoadState.Error.LocationUnavailable
                return@launch
            }

            val network = runCatching { ADNetworkManager.fetchCity(city) }
            network.onSuccess { resp ->
                val list = resp.places.map { payload ->
                    IncidentMapper.toUi(payload, userLatLng)
                }
                _incidents.value = list
                _loadState.value = LoadState.Success(list)
            }.onFailure { t ->
                _incidents.value = emptyList()
                _loadState.value = LoadState.Error.Network(t)
            }
        }
    }

    fun retry(city: City) = getIncidents(city)

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