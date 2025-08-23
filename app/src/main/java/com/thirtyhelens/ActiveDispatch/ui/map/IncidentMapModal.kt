package com.thirtyhelens.ActiveDispatch.feature.mapmodal

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
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
    onClose: () -> Unit,
    onPromoteToFocus: (incidentId: String) -> Unit
) {
    val initialIndex = remember(mode, incidents) {
        when (mode) {
            MapOpenMode.AllPins -> 0
            is MapOpenMode.Focus ->
                incidents.indexOfFirst { it.id == mode.incidentId }.coerceAtLeast(0)
        }
    }
    var selected by remember(mode, incidents) { mutableStateOf(initialIndex) }
    var controller by remember { mutableStateOf<IncidentMapController?>(null) }

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

    LaunchedEffect(selected, controller, mode) {
        if (mode is MapOpenMode.Focus) {
            controller?.focusPin(selected)
        }
    }

    LaunchedEffect(mode, controller) {
        val c = controller ?: return@LaunchedEffect
        if (mode is MapOpenMode.AllPins) {
            c.fitAllPins()
        } else {
            c.focusPin(selected, animate = false)
        }
    }
    // selected data & gradient
    val defaultAccent = AppColors.GradientTop
    val current = incidents.getOrNull(selected)

    val accent = when (mode) {
        MapOpenMode.AllPins -> defaultAccent
        is MapOpenMode.Focus -> current?.badge?.color ?: defaultAccent
    }

    val gradient = remember(accent) {
        Brush.verticalGradient(
            colors = listOf(
                accent,                                 // mid
                AppColors.GradientBottom               // bottom
            )
        )
    }

    // reset when modal disappears
    DisposableEffect(Unit) { onDispose { selected = 0; controller = null } }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .windowInsetsPadding(WindowInsets.systemBars),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top row: Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                FilledTonalButton(
                    onClick = onClose,
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.height(26.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("CLOSE")
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown Icon",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Middle row: Map (fills remaining space)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),            // <- this makes the map take all remaining height
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.08f)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1221))
            ) {
                IncidentMapHost(
                    modifier = Modifier.fillMaxSize(),
                    onControllerReady = { controller = it }
                )
            }

            // Bottom controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left lane
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        FilledTonalIconButton(
                            onClick = {
                                if (incidents.isEmpty()) return@FilledTonalIconButton
                                val newIndex = (selected - 1 + incidents.size) % incidents.size
                                if (mode is MapOpenMode.Focus) {
                                    selected = newIndex
                                } else {
                                    onPromoteToFocus(incidents[newIndex].id) // elevate to Focus
                                }
                            },
                            enabled = incidents.size > 1
                        ) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Previous") }
                    }

                    // Center lane
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (mode) {
                            is MapOpenMode.Focus -> {
                                Text(
                                    text = current?.locationText.orEmpty(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = current?.timeAgo.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.DetailText,
                                    maxLines = 1
                                )
                            }
                            MapOpenMode.AllPins -> {
                                Text(
                                    text = "All incidents",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.85f),
                                    maxLines = 1
                                )
                                Text(
                                    text = "${incidents.size} total",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.DetailText,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Right lane (fixed)
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        FilledTonalIconButton(
                            onClick = {
                                if (incidents.isEmpty()) return@FilledTonalIconButton
                                val newIndex = (selected + 1) % incidents.size
                                if (mode is MapOpenMode.Focus) {
                                    selected = newIndex
                                } else {
                                    onPromoteToFocus(incidents[newIndex].id) // elevate to Focus
                                }
                            },
                            enabled = incidents.size > 1
                        ) { Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Next") }
                    }
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
        onClose = {},
        onPromoteToFocus = {}
    )
}

@Preview(showBackground = true, name = "Map Modal – Focused")
@Composable
fun IncidentMapModal_Focused_Preview() {
    IncidentMapModal(
        incidents = demoIncidents,
        mode = MapOpenMode.Focus("2"),
        onClose = {},
        onPromoteToFocus = {}
    )
}
