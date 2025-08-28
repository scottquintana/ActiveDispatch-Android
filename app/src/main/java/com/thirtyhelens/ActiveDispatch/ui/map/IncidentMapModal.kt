package com.thirtyhelens.ActiveDispatch.feature.mapmodal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.thirtyhelens.ActiveDispatch.maps.IncidentMapController
import com.thirtyhelens.ActiveDispatch.maps.IncidentMapHost
import com.thirtyhelens.ActiveDispatch.maps.buildIncidentMarkerDescriptor
import com.thirtyhelens.ActiveDispatch.models.ADIncident
import com.thirtyhelens.ActiveDispatch.models.AlertBadge
import com.thirtyhelens.ActiveDispatch.models.City
import com.thirtyhelens.ActiveDispatch.ui.theme.AppColors
import com.thirtyhelens.ActiveDispatch.ui.theme.AppIcons

// How the modal is invoked by the parent.
sealed interface MapOpenMode {
    data object AllPins : MapOpenMode
    data class Focus(val incidentId: String) : MapOpenMode
}

// Internal UI state (non-nullable selection when focused).
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
    // State
    val ctx = LocalContext.current
    val isDark = isSystemInDarkTheme()
    var controller by remember { mutableStateOf<IncidentMapController?>(null) }

    // Saver encodes AllPins as -1 and Focus(index) as index (Bundle-friendly).
    val mapUiStateSaver = remember {
        Saver<MapUiState, Int>(
            save = { state -> if (state is MapUiState.Focus) state.selectedIndex else -1 },
            restore = { idx -> if (idx >= 0) MapUiState.Focus(idx) else MapUiState.AllPins }
        )
    }
    var uiState by rememberSaveable(stateSaver = mapUiStateSaver) {
        mutableStateOf<MapUiState>(MapUiState.AllPins)
    }

    // If parent opens in Focus mode, align our internal state once.
    LaunchedEffect(mode, incidents) {
        when (mode) {
            is MapOpenMode.Focus -> {
                val idx = incidents.indexOfFirst { it.id == mode.incidentId }
                    .takeIf { it >= 0 } ?: 0
                uiState = MapUiState.Focus(idx)
            }

            MapOpenMode.AllPins -> {
                // Do not force uiState back to AllPins; allow local promotion via arrows/taps.
            }
        }
    }

    val selectedIndex: Int? = (uiState as? MapUiState.Focus)?.selectedIndex
    val current: ADIncident? = selectedIndex?.let(incidents::getOrNull)

    // Pins

    val pinsForUi = incidents.mapIndexedNotNull { index, inc ->
        val pos = inc.coordinates ?: return@mapIndexedNotNull null
        val showLabel = when (val s = uiState) {
            MapUiState.AllPins -> false
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
            title = inc.title,            // not used by Google info window
            position = pos,
            icon = descriptor,
            anchorU = 0.5f,
            anchorV = 1.0f
        )
    }

    // Effects

    // Push pins and move camera when data/state changes.
    LaunchedEffect(controller, pinsForUi, uiState, selectedCity) {
        val c = controller ?: return@LaunchedEffect
        c.setPins(pinsForUi)
        withFrameNanos { } // ensure map has size before moving camera
        when (val s = uiState) {
            MapUiState.AllPins -> c.fitAllPinsOrCenterFallback(selectedCity, 11f)
            is MapUiState.Focus -> c.focusPin(s.selectedIndex, animate = true)
        }
    }

    // Stable mapping id -> index for marker taps.
    val idToIndex = remember(incidents) {
        incidents.mapIndexed { i, it -> it.id to i }.toMap()
    }

    // Accent color follows current selection (or default in AllPins).
    val accent = when (uiState) {
        MapUiState.AllPins -> AppColors.GradientTop
        is MapUiState.Focus -> current?.badge?.color ?: AppColors.GradientTop
    }
    val gradient = remember(accent) {
        Brush.verticalGradient(listOf(accent, AppColors.GradientBottom))
    }

    // Promote to focus + move camera (used by arrows and pin taps).
    val promoteAndFocus: (Int) -> Unit = { idx ->
        onPromoteToFocus(incidents[idx].id)          // keep parent in sync
        uiState = MapUiState.Focus(idx)              // optimistic local update
        controller?.focusPin(idx, animate = true)
    }

    // Clean up controller when modal disposes.
    DisposableEffect(Unit) { onDispose { controller = null } }

    // Layout
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Track controls height
            var controlsHeightPx by remember { mutableStateOf(0) }
            val controlsHeightDp = with(LocalDensity.current) { controlsHeightPx.toDp() }

            // Main content (close row + map), padded by the measured controls height
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = controlsHeightDp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Close row
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

                // Map card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.08f)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1221))
                ) {
                    IncidentMapHost(
                        modifier = Modifier.fillMaxSize(),
                        onControllerReady = { controller = it },
                        onMarkerClick = { pinId ->
                            val newIndex = idToIndex[pinId] ?: return@IncidentMapHost
                            promoteAndFocus(newIndex)
                        }
                    )
                }
            }

            // Bottom controls overlaid and inset-aware
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .onSizeChanged { controlsHeightPx = it.height } // measure real height
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left / center / right blocks — your existing code
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        FilledTonalIconButton(
                            onClick = {
                                if (incidents.isEmpty()) return@FilledTonalIconButton
                                when (val s = uiState) {
                                    MapUiState.AllPins -> promoteAndFocus(incidents.lastIndex)
                                    is MapUiState.Focus -> {
                                        val next = (s.selectedIndex - 1 + incidents.size) % incidents.size
                                        promoteAndFocus(next)
                                    }
                                }
                            },
                            enabled = incidents.size > 1
                        ) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Previous") }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (uiState) {
                            is MapUiState.Focus -> {
                                Text(current?.locationText.orEmpty(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White, maxLines = 1)
                                Text(current?.timeAgo.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.DetailText, maxLines = 1)
                            }
                            MapUiState.AllPins -> {
                                Text("All incidents",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.85f), maxLines = 1)
                                Text("${incidents.size} total",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.DetailText, maxLines = 1)
                            }
                        }
                    }

                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        FilledTonalIconButton(
                            onClick = {
                                if (incidents.isEmpty()) return@FilledTonalIconButton
                                when (val s = uiState) {
                                    MapUiState.AllPins -> promoteAndFocus(0)
                                    is MapUiState.Focus -> {
                                        val next = (s.selectedIndex + 1) % incidents.size
                                        promoteAndFocus(next)
                                    }
                                }
                            },
                            enabled = incidents.size > 1
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Next")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun bottomSafe(fallback: Dp = 24.dp): Dp {
    // Works on modern devices; API 28 dialogs often return 0 → use fallback.
    val pv = WindowInsets.safeDrawing.asPaddingValues()
    val bottom = pv.calculateBottomPadding()
    return if (bottom > 0.dp) bottom else fallback
}

// Previews

private val demoIncidents = listOf(
    ADIncident(
        "1", "Shots Fired",
        AlertBadge(AppColors.AccentRed, AppIcons.TriangleExclamation),
        "Inglewood - 1.2 mi away", "5 min ago", "7:34 PM",
        LatLng(36.2125, -86.7330)
    ),
    ADIncident(
        "2", "Non-Residence Burglary Alarm",
        AlertBadge(AppColors.AccentLightPurple, AppIcons.Business),
        "Berry Hill - 2.4 mi away", "12 min ago", "7:26 PM",
        LatLng(36.1195, -86.7690)
    ),
    ADIncident(
        "3", "Residence Burglary Alarm",
        AlertBadge(AppColors.AccentGreen, AppIcons.Bell),
        "Oak Hill - 4.1 mi away", "23 min ago", "7:15 PM",
        LatLng(36.0675, -86.7912)
    ),
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