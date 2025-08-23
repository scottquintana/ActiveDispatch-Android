package com.thirtyhelens.ActiveDispatch.maps

import com.google.android.gms.maps.model.LatLng

/** Thin contract your real map impl (GoogleMap, Mapbox, etc.) will satisfy. */
interface IncidentMapController {
    fun setPins(pins: List<Pin>)
    fun fitAllPins()
    fun focusPin(index: Int, animate: Boolean = true)

    data class Pin(
        val id: String,
        val title: String,
        val position: LatLng
    )
}
