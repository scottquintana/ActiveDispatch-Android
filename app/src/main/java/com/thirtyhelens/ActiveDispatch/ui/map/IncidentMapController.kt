// com/thirtyhelens/ActiveDispatch/maps/IncidentMapController.kt
package com.thirtyhelens.ActiveDispatch.maps

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.thirtyhelens.ActiveDispatch.models.City

//import com.google.maps.android.ktx.awaitMap

class IncidentMapController internal constructor(
    private val scope: CoroutineScope,
    var cameraPaddingPx: Int = 80
) {
    data class Pin(
        val id: String,
        val title: String,
        val position: LatLng,
        val colorHue: Float = BitmapDescriptorFactory.HUE_RED,
        val icon: BitmapDescriptor? = null,
        val anchorU: Float = 0.5f,
        val anchorV: Float = 1.0f
    )

    internal val pins: SnapshotStateList<Pin> = mutableStateListOf()

    private var map: GoogleMap? = null
    internal fun attachMap(googleMap: GoogleMap) { map = googleMap }
    internal fun detachMap() { map = null }

    fun setPins(newPins: List<Pin>) {
        pins.clear()
        pins.addAll(newPins)
    }

    /** Zooms/animates camera to include all pins */
    fun fitAllPins() {
        val m = map ?: return
        if (pins.isEmpty()) return
        val b = LatLngBounds.builder()
        pins.forEach { b.include(it.position) }
        val bounds = b.build()
        // CameraUpdateFactory padding is in pixels
        m.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, cameraPaddingPx))
    }

    /** Focuses on a pin by list index */
    fun focusPin(index: Int, animate: Boolean = true, zoom: Float = 14f) {
        val m = map ?: return
        val p = pins.getOrNull(index) ?: return
        val update = CameraUpdateFactory.newLatLngZoom(p.position, zoom)
        if (animate) m.animateCamera(update) else m.moveCamera(update)
    }

    fun moveTo(position: LatLng, zoom: Float = 11f, animate: Boolean = false) {
        val m = map ?: return
        val cu = CameraUpdateFactory.newLatLngZoom(position, zoom)
        if (animate) m.animateCamera(cu) else m.moveCamera(cu)
    }

    fun fitAllPinsOrCenterFallback(center: City, fallbackZoom: Float = 11f, animate: Boolean = true) {
        val m = map ?: return
        val valid = pins.filter { !(it.position.latitude == 0.0 && it.position.longitude == 0.0) }
        when (valid.size) {
            0 -> {
                val cu = CameraUpdateFactory.newLatLngZoom(center.fallbackLatLng, fallbackZoom)
                if (animate) m.animateCamera(cu) else m.moveCamera(cu)
            }
            1 -> {
                val cu = CameraUpdateFactory.newLatLngZoom(valid.first().position, 13f)
                if (animate) m.animateCamera(cu) else m.moveCamera(cu)
            }
            else -> {
                val builder = LatLngBounds.builder()
                valid.forEach { builder.include(it.position) }
                val bounds = builder.build()
                val cu = CameraUpdateFactory.newLatLngBounds(bounds, cameraPaddingPx)
                if (animate) m.animateCamera(cu) else m.moveCamera(cu)
            }
        }
    }
}
