package com.thirtyhelens.ActiveDispatch.models
import com.google.android.gms.maps.model.LatLng

// Fall back LatLng is mostly for simulators
enum class City(val path: String, val fallbackLatLng: LatLng) {
    NASHVILLE("nashville", LatLng(36.1627, -86.7816)),
    PDX      ("pdx",       LatLng(45.5152, -122.6784)),
    SF       ("sf",        LatLng(37.7749, -122.4194));
}