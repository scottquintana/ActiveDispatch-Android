package com.thirtyhelens.ActiveDispatch.feature.mapmodal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.thirtyhelens.ActiveDispatch.maps.buildIncidentMarkerDescriptor
import com.thirtyhelens.ActiveDispatch.models.AlertBadge
import com.thirtyhelens.ActiveDispatch.ui.theme.AppColors
import com.thirtyhelens.ActiveDispatch.ui.theme.AppIcons
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.thirtyhelens.ActiveDispatch.models.City

/**
 * Mode the modal opens in:
 *  - AllPins: show all pins (map button)
 *  - Focus(id): same pins, but focus the tapped incident (cell tap)
 */
sealed interface MapOpenMode {
    data object AllPins : MapOpenMode
    data class Focus(val incidentId: String) : MapOpenMode
}

sealed interface MapUiState {
    data object AllPins : MapUiState
    data class Focus(val selectedIndex: Int) : MapUiState
}

@Composable
fun IncidentMapModal(
    incidents: List<ADIncident>,
    mode: MapOpenMode,
    selectedCity: City,
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
    val ctx = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val selectedId: String? = incidents.getOrNull(selected)?.id

    val MapUiStateSaver = Saver<MapUiState, Int>(
        save = { state -> if (state is MapUiState.Focus) state.selectedIndex else -1 },
        restore = { idx -> if (idx >= 0) MapUiState.Focus(idx) else MapUiState.AllPins }
    )

    var uiState by rememberSaveable(stateSaver = MapUiStateSaver) {
        mutableStateOf<MapUiState>(MapUiState.AllPins)
    }


// react ONLY when parent mode changes to Focus
    LaunchedEffect(mode, incidents) {
        when (mode) {
            is MapOpenMode.Focus -> {
                val idx = incidents.indexOfFirst { it.id == mode.incidentId }.takeIf { it >= 0 } ?: 0
                uiState = MapUiState.Focus(idx)
            }
            MapOpenMode.AllPins -> {
                // do NOT force uiState back to AllPins here;
                // allow arrows/taps to promote locally
            }
        }
    }
    val pinsForUi = incidents.mapIndexedNotNull { index, inc ->
        val pos = inc.coordinates ?: return@mapIndexedNotNull null
        val showLabel = when (val s = uiState) {
            MapUiState.AllPins  -> false
            is MapUiState.Focus -> index == s.selectedIndex
        }
        val descriptor = buildIncidentMarkerDescriptor(
            context = ctx,
            icon = inc.badge.icon,
            fillColor = inc.badge.color,
            label = if (showLabel) inc.title else null,
            labelColor = if (isDark) Color.White else Color.Black
        )
        IncidentMapController.Pin(
            id = inc.id,
            title = inc.title,        // not used by Google info window
            position = pos,
            icon = descriptor,
            anchorU = 0.5f,
            anchorV = 1.0f
        )
    }

    // Push pins to the map
    LaunchedEffect(controller, pinsForUi, uiState, selectedCity) {
        val c = controller ?: return@LaunchedEffect
        c.setPins(pinsForUi)
        withFrameNanos { }
        when (val s = uiState) {
            MapUiState.AllPins  -> c.fitAllPinsOrCenterFallback(selectedCity, 11f)
            is MapUiState.Focus -> c.focusPin(s.selectedIndex, animate = false)
        }
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

    val selectedIndex: Int? = (uiState as? MapUiState.Focus)?.selectedIndex
    val current: ADIncident? = selectedIndex?.let { idx -> incidents.getOrNull(idx) }

    val accent = when (uiState) {
        MapUiState.AllPins  -> AppColors.GradientTop
        is MapUiState.Focus -> current?.badge?.color ?: AppColors.GradientTop
    }

    // derive selection from uiState

// render bottom info from uiState (your block)
    when (uiState) {
        is MapUiState.Focus -> {
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
        MapUiState.AllPins -> {
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

    val gradient = remember(accent) {
        Brush.verticalGradient(
            colors = listOf(
                accent,
                AppColors.GradientBottom
            )
        )
    }

    fun setFocus(index: Int) {
        uiState = MapUiState.Focus(index)
        controller?.focusPin(index, animate = true)
    }

// Left / Right arrows
    val promoteAndFocus: (Int) -> Unit = { idx ->
        // inform parent so 'mode' flips to Focus (this is what pin taps do)
        onPromoteToFocus(incidents[idx].id)
        // optimistic local focus for instant UI update
        uiState = MapUiState.Focus(idx)
        controller?.focusPin(idx, animate = true)
    }

    val leftArrowTapped: () -> Unit = fun() {
        if (incidents.isEmpty()) return
        when (val s = uiState) {
            MapUiState.AllPins  -> promoteAndFocus(incidents.lastIndex)
            is MapUiState.Focus -> promoteAndFocus((s.selectedIndex - 1 + incidents.size) % incidents.size)
        }
    }

    val rightArrowTapped: () -> Unit = fun() {
        if (incidents.isEmpty()) return
        when (val s = uiState) {
            MapUiState.AllPins  -> promoteAndFocus(0)
            is MapUiState.Focus -> promoteAndFocus((s.selectedIndex + 1) % incidents.size)
        }
    }

    val idToIncidentIndex = remember(incidents) {
        incidents.withIndex().associate { it.value.id to it.index }
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
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
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
                val idToIndex = remember(incidents) {
                    incidents.mapIndexed { i, it -> it.id to i }.toMap()
                }

                IncidentMapHost(
                    modifier = Modifier.fillMaxSize(),
                    onControllerReady = { controller = it },
                    onMarkerClick = { pinId ->
                        val newIndex = idToIndex[pinId] ?: return@IncidentMapHost
                        if (mode is MapOpenMode.AllPins) {
                            // Promote to focus at tapped incident
                            onPromoteToFocus(pinId)
                        } else {
                            // Already focused, just change selection and camera
                            selected = newIndex
                            controller?.focusPin(newIndex)
                        }
                    }
                )
            }

            // Bottom controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left lane
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        FilledTonalIconButton(
                            onClick = {
                                if (incidents.isEmpty()) return@FilledTonalIconButton
                                leftArrowTapped()
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
                                rightArrowTapped()
                                      },
                            enabled = incidents.size > 1
                        ) { Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Next") }
                    }
                }
            }

            Spacer(Modifier.height(54.dp))

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
        selectedCity = City.NASHVILLE,
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
        selectedCity = City.NASHVILLE,
        onPromoteToFocus = {}
    )
}
