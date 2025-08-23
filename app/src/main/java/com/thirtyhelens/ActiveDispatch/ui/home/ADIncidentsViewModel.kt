package com.thirtyhelens.ActiveDispatch.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.thirtyhelens.ActiveDispatch.models.City
import com.thirtyhelens.ActiveDispatch.utils.ADNetworkManager
import com.thirtyhelens.ActiveDispatch.utils.LocationProvider
import com.thirtyhelens.ActiveDispatch.utils.isEmulator
import com.thirtyhelens.ActiveDispatch.models.ADIncident
import com.thirtyhelens.ActiveDispatch.ui.mapping.IncidentMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class ADIncidentsViewModel(
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _incidents = MutableStateFlow<List<ADIncident>>(emptyList())
    open val incidents: StateFlow<List<ADIncident>> = _incidents.asStateFlow()

    init {
        locationProvider.startLocationUpdates(viewModelScope)
    }

    fun getIncidents(city: City) {
        viewModelScope.launch {
            // snapshot; don’t block waiting for GPS
            val snapshot = locationProvider.locationFlow.value

            // if null AND emulator, use city center so distances render nicely
            val userLatLng: LatLng? = snapshot ?: if (isEmulator()) city.fallbackLatLng else null

            val result = runCatching { ADNetworkManager.fetchCity(city) }
            val list = result.getOrNull()
                ?.places
                ?.map { payload -> IncidentMapper.toUi(payload, userLatLng) }
                .orEmpty()

            _incidents.value = list
        }
    }
}