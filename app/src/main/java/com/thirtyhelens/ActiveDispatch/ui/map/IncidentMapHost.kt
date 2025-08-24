// com/thirtyhelens/ActiveDispatch/maps/IncidentMapHost.kt
package com.thirtyhelens.ActiveDispatch.maps

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

import androidx.compose.foundation.isSystemInDarkTheme
import com.google.android.gms.maps.model.MapStyleOptions
import com.thirtyhelens.ActiveDispatch.R

@Composable
fun IncidentMapHost(
    modifier: Modifier = Modifier,
    onControllerReady: (IncidentMapController) -> Unit,
    onMarkerClick: (String) -> Unit
) {
    val ctx = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val hasFine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val canUseMyLocation = hasFine || hasCoarse

    val scope = rememberCoroutineScope()
    val controller = remember(scope) { IncidentMapController(scope) }
    DisposableEffect(controller) { onDispose { controller.detachMap() } }

    val cameraPositionState = rememberCameraPositionState()
    val properties by remember(canUseMyLocation) {
        mutableStateOf(MapProperties(
            isMyLocationEnabled = canUseMyLocation,
            mapStyleOptions = if (isDark)
                MapStyleOptions.loadRawResourceStyle(ctx, R.raw.map_dark)
            else null
        ))
    }
    val uiSettings = remember { MapUiSettings(zoomControlsEnabled = false, compassEnabled = true) }

    LaunchedEffect(Unit) { onControllerReady(controller) }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings
    ) {
        MapEffect(Unit) { map ->
            controller.attachMap(map)
        }

        // Markers
        controller.pins.forEach { pin ->
            Marker(
                state = rememberMarkerState(position = pin.position),
                title = null,
                icon = pin.icon ?: BitmapDescriptorFactory.defaultMarker(pin.colorHue),
                anchor = androidx.compose.ui.geometry.Offset(pin.anchorU, pin.anchorV),
                onClick = {
                    onMarkerClick(pin.id)
                    true
                }
            )
        }
    }
}

fun hueFromComposeColor(color: Color): Float {
    val argb = color.toArgb()
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(argb, hsl)
    return hsl[0] // 0..360
}

@Composable
private fun onDisposeOnce(block: () -> Unit) {
    DisposableEffect(Unit) { onDispose(block) }
}