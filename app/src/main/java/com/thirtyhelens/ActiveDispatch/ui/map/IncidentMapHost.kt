package com.thirtyhelens.ActiveDispatch.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** Placeholder until we plug in a real map; exposes a controller via lambda. */
@Composable
fun IncidentMapHost(
    modifier: Modifier = Modifier,
    onControllerReady: (IncidentMapController) -> Unit
) {
    // TODO: replace with real map and a controller that forwards to it
    onControllerReady(object : IncidentMapController {
        private var pins: List<IncidentMapController.Pin> = emptyList()
        override fun setPins(pins: List<IncidentMapController.Pin>) { this.pins = pins }
        override fun fitAllPins() { /* no-op in placeholder */ }
        override fun focusPin(index: Int, animate: Boolean) { /* no-op */ }
    })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1D2230)),
        contentAlignment = Alignment.Center
    ) {
        Text("Map placeholder")
    }
}
