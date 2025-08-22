package com.thirtyhelens.ActiveDispatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.thirtyhelens.ActiveDispatch.ui.theme.AppColors
import com.thirtyhelens.ActiveDispatch.ui.theme.AppIcons
import com.thirtyhelens.ActiveDispatch.views.ADIncident
import com.thirtyhelens.ActiveDispatch.views.AlertBadge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.text.SimpleDateFormat
import java.util.*

open class ADIncidentsViewModel(
    private val locationManager: LocationManager
) : ViewModel() {

    private val _incidents = MutableStateFlow<List<ADIncident>>(emptyList())
    open val incidents: StateFlow<List<ADIncident>> = _incidents.asStateFlow()

    init {
        locationManager.startLocationUpdates(viewModelScope)
    }

    fun getIncidents() {
        viewModelScope.launch {
            val userLocation = locationManager.locationFlow.firstOrNull { it != null }

            val result = NetworkManager.getAlerts()
            if (result.isSuccess && userLocation != null) {
                val list = result.getOrNull()?.map { data ->
                    val coordinates = locationManager.coordinatesForAddress("${data.location}, Nashville, TN")
                    val latLng = coordinates?.let { LatLng(it.latitude, it.longitude) }

                    val uiIncident = mapToUIIncident(data).copy(
                        coordinates = latLng,
                        locationText = formatDistanceAway(
                            userLocation = userLocation,
                            incidentLatLng = latLng,
                            neighborhood = data.cityName.capitalize()
                        )
                    )

                    uiIncident
                } ?: emptyList()

                _incidents.value = list
            } else {
                // handle error or null location
            }
        }
    }

    fun mapToUIIncident(data: ADIncidentData): ADIncident {
        val incidentTime = Instant.ofEpochMilli(data.callReceivedTime)
        val now = Instant.now()
        val duration = Duration.between(incidentTime, now)

        val badge = when (data.incidentTypeCode) {
            "52P", "53P" -> AlertBadge(AppColors.AccentRed, AppIcons.Bell)
            "70A", "70P" -> AlertBadge(AppColors.AccentGreen, AppIcons.Bell)
            "71A", "71P" -> AlertBadge(AppColors.AccentLightPurple, AppIcons.Business)
            "64P"        -> AlertBadge(AppColors.AccentGold, AppIcons.PersonExclamation)
            "83P", "51P" -> AlertBadge(AppColors.AccentRed, AppIcons.Shield) // check this
            "87T"        -> AlertBadge(AppColors.AccentGreen, AppIcons.TreeDown)
            "87W"        -> AlertBadge(AppColors.AccentGold, AppIcons.WiresDown)
            "8000"       -> AlertBadge(AppColors.AccentRed, AppIcons.TriangleExclamation)
            else         -> AlertBadge(AppColors.AccentGold, AppIcons.Bell)
        }

        val address = "${data.location}, Nashville, TN"

        return ADIncident(
            id = data.objectId,
            title = data.incidentTypeName.replaceFirstChar { it.uppercase(Locale.getDefault()) },
            badge = badge,
            locationText = "${data.cityName.replaceFirstChar { it.uppercase(Locale.getDefault()) }} - ${data.location.replaceFirstChar { it.uppercase(Locale.getDefault()) }}",
            timeAgo = formatTimeAgo(duration),
            lastUpdated = formatDate(data.lastUpdated),
            coordinates = null // initially null, then updated with geocoder
        )
    }

    private fun formatTimeAgo(duration: Duration): String {
        val minutes = duration.toMinutes()
        return when {
            minutes < 60 -> "$minutes min ago"
            else -> {
                val hours = duration.toHours()
                val remainderMinutes = minutes % 60
                "$hours h ${remainderMinutes} m ago"
            }
        }
    }

    private fun formatDate(epochMillis: Long): String {
        val date = Date(epochMillis)
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return formatter.format(date)
    }
}