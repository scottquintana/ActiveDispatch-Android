package com.thirtyhelens.ActiveDispatch.feature.mapmodal

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.thirtyhelens.ActiveDispatch.maps.IncidentMapController
import com.thirtyhelens.ActiveDispatch.maps.IncidentMapHost
import com.thirtyhelens.ActiveDispatch.models.ADIncident
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thirtyhelens.ActiveDispatch.models.AlertBadge
import com.thirtyhelens.ActiveDispatch.ui.theme.AppColors
import com.thirtyhelens.ActiveDispatch.ui.theme.AppIcons
/**
 * Mode the modal opens in:
 *  - AllPins: show all pins (map button)
 *  - Focus(id): same pins, but focus the tapped incident (cell tap)
 */
sealed interface MapOpenMode {
    data object AllPins : MapOpenMode
    data class Focus(val incidentId: String) : MapOpenMode
}

@Composable
fun IncidentMapModal(
    incidents: List<ADIncident>,
    mode: MapOpenMode,
    onClose: () -> Unit
) {
    // selection
    val initialIndex = remember(mode, incidents) {
        when (mode) {
            MapOpenMode.AllPins -> 0
            is MapOpenMode.Focus ->
                incidents.indexOfFirst { it.id == mode.incidentId }.coerceAtLeast(0)
        }
    }
    var selected by remember(mode, incidents) { mutableStateOf(initialIndex) }

    // controller
    var controller by remember { mutableStateOf<IncidentMapController?>(null) }

    // map pins & camera behavior
    LaunchedEffect(controller, incidents) {
        val c = controller ?: return@LaunchedEffect
        val pins = incidents.map {
            IncidentMapController.Pin(
                id = it.id,
                title = it.title,
                position = it.coordinates ?: LatLng(0.0, 0.0)
            )
        }
        c.setPins(pins)
        if (mode is MapOpenMode.AllPins) c.fitAllPins() else c.focusPin(selected, animate = false)
    }
    LaunchedEffect(selected, controller) { controller?.focusPin(selected) }

    // selected data & gradient
    val current = incidents.getOrNull(selected)
    val accent = current?.badge?.color ?: Color(0xFF7C4DFF)
    val gradient = remember(accent) {
        Brush.verticalGradient(
            colors = listOf(
                accent,                         // top matches pin color
                accent.copy(alpha = 0.75f),
                Color(0xFF1A1F2E),              // mid
                Color(0xFF121726)               // bottom
            )
        )
    }

    // reset when modal disappears
    DisposableEffect(Unit) { onDispose { selected = 0; controller = null } }

    // full-screen gradient background
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            // close button (top center)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.Center
            ) {
                FilledTonalButton(
                    onClick = onClose,
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
                ) { Text("CLOSE") }
            }

            // map card centered with rounded corners
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .fillMaxHeight(0.78f), // tune the height
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.08f)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1221))
            ) {
                IncidentMapHost(
                    modifier = Modifier.fillMaxSize(),
                    onControllerReady = { controller = it }
                )
            }

            // bottom controls (address + arrows)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // address / info
                Text(
                    text = current?.locationText ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                )

                // arrows row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp, start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            if (incidents.isNotEmpty())
                                selected = (selected - 1 + incidents.size) % incidents.size
                        }
                    ) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Prev") }

                    AssistChip(
                        onClick = {},
                        label = { Text(current?.timeAgo ?: "") }
                    )

                    FilledTonalIconButton(
                        onClick = {
                            if (incidents.isNotEmpty())
                                selected = (selected + 1) % incidents.size
                        }
                    ) { Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Next") }
                }
            }
        }
    }
}


/* ---------- Previews ---------- */

private val demoIncidents = listOf(
    ADIncident("1","Shots Fired", AlertBadge(AppColors.AccentRed, AppIcons.TriangleExclamation), "Inglewood - 1.2 mi away","5 min ago","7:34 PM", LatLng(36.2125,-86.7330)),
    ADIncident("2","Non-Residence Burglary Alarm", AlertBadge(AppColors.AccentLightPurple, AppIcons.Business), "Berry Hill - 2.4 mi away","12 min ago","7:26 PM", LatLng(36.1195,-86.7690)),
    ADIncident("3","Residence Burglary Alarm", AlertBadge(AppColors.AccentGreen, AppIcons.Bell), "Oak Hill - 4.1 mi away","23 min ago","7:15 PM", LatLng(36.0675,-86.7912)),
)

@Preview(showBackground = true, name = "Map Modal – All Pins")
@Composable
fun IncidentMapModal_AllPins_Preview() {
    IncidentMapModal(
        incidents = demoIncidents,
        mode = MapOpenMode.AllPins,
        onClose = {}
    )
}

@Preview(showBackground = true, name = "Map Modal – Focused")
@Composable
fun IncidentMapModal_Focused_Preview() {
    IncidentMapModal(
        incidents = demoIncidents,
        mode = MapOpenMode.Focus("2"),
        onClose = {}
    )
}
