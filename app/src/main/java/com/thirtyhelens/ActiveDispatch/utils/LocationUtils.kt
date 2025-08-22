package com.thirtyhelens.ActiveDispatch.utils

import android.location.Location
import com.google.android.gms.maps.model.LatLng

fun formatDistanceAway(
    userLocation: LatLng?,
    incidentLatLng: LatLng?,
    neighborhood: String
): String {
    if (userLocation == null || incidentLatLng == null) return neighborhood

    val userLoc = userLocation.toLocation()
    val incidentLoc = incidentLatLng.toLocation()

    val distanceMeters = userLoc.distanceTo(incidentLoc)
    val distanceMiles = distanceMeters / 1609.34

    return if (distanceMiles < 1) {
        "${(distanceMiles * 5280).toInt()} ft away"
    } else {
        "%.1f mi away".format(distanceMiles)
    }
}

fun LatLng.toLocation(): Location {
    return Location("LatLng").apply {
        latitude = this@toLocation.latitude
        longitude = this@toLocation.longitude
    }
}