package com.thirtyhelens.ActiveDispatch.ui.home

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.thirtyhelens.ActiveDispatch.R
import com.thirtyhelens.ActiveDispatch.models.ADIncident
import com.thirtyhelens.ActiveDispatch.models.ADResponse
import com.thirtyhelens.ActiveDispatch.models.City
import com.thirtyhelens.ActiveDispatch.ui.components.ADIncidentCell
import com.thirtyhelens.ActiveDispatch.ui.components.SlideUpDialog
import com.thirtyhelens.ActiveDispatch.ui.mapping.IncidentMapper
import com.thirtyhelens.ActiveDispatch.ui.theme.AppColors
import com.thirtyhelens.ActiveDispatch.ui.theme.AppIcons
import com.thirtyhelens.ActiveDispatch.utils.LocationManager
import com.thirtyhelens.ActiveDispatch.utils.LocationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import com.thirtyhelens.ActiveDispatch.feature.mapmodal.IncidentMapModal
import com.thirtyhelens.ActiveDispatch.feature.mapmodal.MapOpenMode

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(
    initialCity: City = City.NASHVILLE,
    providedViewModel: ADIncidentsViewModel? = null
) {
    val ctx = LocalContext.current.applicationContext
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* react to grant/deny here */ }

    LaunchedEffect(Unit) {
        launcher.launch(arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    val viewModel = providedViewModel ?: remember {
        ADIncidentsViewModel(locationProvider = LocationManager(ctx))
    }

    var selectedCity by remember { mutableStateOf(initialCity) }

    val incidents by viewModel.incidents.collectAsStateWithLifecycle(initialValue = emptyList())
    val loadState by viewModel.loadState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedCity) {
        Log.d("HomeScreen", "Fetching incidents for ${selectedCity.name}")
        viewModel.getIncidents(selectedCity)
        Log.d("ADVM", "Launch effect went")
    }

    var mapMode by remember { mutableStateOf<MapOpenMode?>(null) }

    val onIncidentClick: (ADIncident) -> Unit = { incident ->
        mapMode = MapOpenMode.Focus(incident.id)
    }

    Scaffold(
        containerColor = AppColors.BackgroundBlue,
        floatingActionButton = {
            if (loadState is ADIncidentsViewModel.LoadState.Success) {
                FloatingActionButton(
                    onClick = { mapMode = MapOpenMode.AllPins },
                    containerColor = Color(0xFF4C54FF),
                ) { Icon(AppIcons.Map, contentDescription = "Map", tint = Color.White) }
            }
        }
    ) { _ ->

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // Header stays at the top, outside the refresh area
            HeroHeader(title = "Active Dispatch", imageRes = R.drawable.nashville_header)
            Spacer(Modifier.height(8.dp))

            when (val s = loadState) {
                ADIncidentsViewModel.LoadState.Idle,
                ADIncidentsViewModel.LoadState.Loading -> {
                    LoadingState()
                }

                is ADIncidentsViewModel.LoadState.Success -> {
                    // Pull‑to‑refresh wraps ONLY the scrolling list content
                    RefreshableIncidents(
                        isLoading = loadState is ADIncidentsViewModel.LoadState.Loading,
                        onRefresh = { viewModel.getIncidents(selectedCity) }
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(s.data, key = { it.id }) { incident ->
                                val interaction = remember { MutableInteractionSource() }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                        .clickable(
                                            interactionSource = interaction,
                                            indication = LocalIndication.current,
                                        ) { onIncidentClick(incident) }
                                ) {
                                    ADIncidentCell(incident = incident, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }

                is ADIncidentsViewModel.LoadState.Error -> {
                    when (s) {
                        ADIncidentsViewModel.LoadState.Error.LocationUnavailable ->
                            ErrorState(
                                message = "We couldn’t access your location.",
                                onRetry = { viewModel.getIncidents(selectedCity) }
                            )

                        is ADIncidentsViewModel.LoadState.Error.Network ->
                            ErrorState(
                                message = "We couldn’t load incidents.",
                                onRetry = { viewModel.getIncidents(selectedCity) }
                            )
                    }
                }
            }
        }

        if (mapMode != null) {
            SlideUpDialog(onRequestClose = { mapMode = null }) {
                IncidentMapModal(
                    incidents = incidents,
                    mode = mapMode!!,
                    onClose = { mapMode = null },
                    selectedCity = selectedCity,
                    onPromoteToFocus = { id -> mapMode = MapOpenMode.Focus(id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshableIncidents(
    isLoading: Boolean,          // e.g., loadState is Loading
    onRefresh: () -> Unit,       // call viewModel.getIncidents(...)
    content: @Composable () -> Unit
) {
    val pullState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        state = pullState,
        modifier = Modifier.fillMaxSize()
    ) {
        content()
    }
}
@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Loading incidents…",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xCCFFFFFF)
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Optional: replace with your own drawable
        Icon(
            imageVector = AppIcons.TriangleExclamation,
            contentDescription = null,
            tint = Color(0xFFFFC107), // amber-ish
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "We couldn’t load incidents.",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Check your internet connection and try again.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xCCFFFFFF)
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Try again")
        }
    }
}

// Top hero with skyline image + gradient + big title
// Will need to be dynamic when multi city is implemented
@Composable
private fun HeroHeader(
    title: String,
    imageRes: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xAA0B0E2A), Color(0x660B0E2A), Color(0x330B0E2A))
                    )
                )
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        )
    }
}

// Preview scaffolding

private class FakeLocationProvider(
    latLng: LatLng? = LatLng(36.1627, -86.7816)
) : LocationProvider {
    private val _flow = MutableStateFlow(latLng)
    override val locationFlow: StateFlow<LatLng?> = _flow
    override fun startLocationUpdates(scope: CoroutineScope) { /* no-op */ }
    override fun stopLocationUpdates() { /* no-op */ }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0E2A)
@Composable
fun HomeScreenPreview() {
    val userLatLng = LatLng(36.1627, -86.7816)
    val mockIncidents = remember {
        ADResponse.mockData.places.map { IncidentMapper.toUi(it, userLatLng) }
    }

    val fakeVm = remember {
        object : ADIncidentsViewModel(locationProvider = object : LocationProvider {
            private val _flow = MutableStateFlow(userLatLng)
            override val locationFlow: StateFlow<LatLng?> = _flow
            override fun startLocationUpdates(scope: CoroutineScope) {}
            override fun stopLocationUpdates() {}
        }) {
            override val incidents: StateFlow<List<ADIncident>> = MutableStateFlow(mockIncidents)
        }
    }

    HomeScreen(
        providedViewModel = fakeVm,
    )
}
