package com.thirtyhelens.ActiveDispatch

import com.thirtyhelens.ActiveDispatch.formatDistanceAway
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt

fun formatDistanceAway(
    userLocation: Location?,
    incidentLatLng: LatLng?,
    neighborhood: String
): String {
    if (userLocation == null || incidentLatLng == null) return ""

    val targetLocation = Location("").apply {
        latitude = incidentLatLng.latitude
        longitude = incidentLatLng.longitude
    }

    val distanceInMeters = userLocation.distanceTo(targetLocation)
    val miles = distanceInMeters / 1609.344
    val milesString = String.format("%.1f", miles)

    return "$neighborhood - $milesString mi. away"
}