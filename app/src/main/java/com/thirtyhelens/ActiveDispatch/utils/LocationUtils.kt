package com.thirtyhelens.ActiveDispatch.utils

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import android.util.Log

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
        "$neighborhood - ${(distanceMiles * 5280).toInt()} ft away"
    } else {
        "$neighborhood - %.1f mi away".format(distanceMiles)
    }
}

fun LatLng.toLocation(): Location {
    return Location("LatLng").apply {
        latitude = this@toLocation.latitude
        longitude = this@toLocation.longitude
    }
}

fun isEmulator(): Boolean {
    val brand = android.os.Build.BRAND
    val device = android.os.Build.DEVICE
    val product = android.os.Build.PRODUCT
    val model = android.os.Build.MODEL
    val hardware = android.os.Build.HARDWARE
    val fingerprint = android.os.Build.FINGERPRINT

    return fingerprint.contains("generic", ignoreCase = true) ||
            model.contains("Emulator", ignoreCase = true) ||
            model.contains("Android SDK built for x86", ignoreCase = true) ||
            product.contains("sdk_gphone", ignoreCase = true) ||
            product.contains("google_sdk", ignoreCase = true) ||
            brand.startsWith("generic") && device.startsWith("generic") ||
            hardware.contains("goldfish", ignoreCase = true) ||
            hardware.contains("ranchu", ignoreCase = true)
}