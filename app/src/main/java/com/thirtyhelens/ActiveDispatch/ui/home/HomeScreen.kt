package com.thirtyhelens.ActiveDispatch.ui.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import androidx.compose.ui.res.painterResource
import com.thirtyhelens.ActiveDispatch.R
import com.thirtyhelens.ActiveDispatch.ui.home.ADIncidentsViewModel
import com.thirtyhelens.ActiveDispatch.models.ADResponse
import com.thirtyhelens.ActiveDispatch.models.City
import com.thirtyhelens.ActiveDispatch.ui.mapping.IncidentMapper
import com.thirtyhelens.ActiveDispatch.ui.theme.AppIcons
import com.thirtyhelens.ActiveDispatch.utils.LocationManager
import com.thirtyhelens.ActiveDispatch.utils.LocationProvider
import com.thirtyhelens.ActiveDispatch.models.ADIncident
import com.thirtyhelens.ActiveDispatch.ui.components.ADIncidentCell
import com.thirtyhelens.ActiveDispatch.ui.home.IncidentList
import com.thirtyhelens.ActiveDispatch.ui.theme.AppColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeScreen(
    initialCity: City = City.NASHVILLE,
    providedViewModel: ADIncidentsViewModel? = null,
    onMapClick: () -> Unit = {},
    onIncidentClick: (ADIncident) -> Unit = {}
) {
    val ctx = LocalContext.current.applicationContext
    val viewModel = remember(ctx, providedViewModel) {
        providedViewModel ?: ADIncidentsViewModel(locationProvider = LocationManager(ctx))
    }

    var selectedCity by remember { mutableStateOf(initialCity) }

    // Fetch on start and when city changes (you can re‑add a city switcher later)
    LaunchedEffect(selectedCity) {
        Log.d("HomeScreen", "Fetching ${selectedCity.name}")
        viewModel.getIncidents(selectedCity)
    }

    Scaffold(
        containerColor = AppColors.BackgroundBlue,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onMapClick,
                containerColor = Color(0xFF4C54FF), // tweak to your palette
            ) {
                Icon(
                    imageVector = AppIcons.Map, // or use your map icon alias
                    contentDescription = "Map",
                    tint = Color.White
                )
            }
        }
    ) { _ ->
        val incidents = viewModel.incidents.collectAsState().value

        LazyColumn(
            contentPadding = PaddingValues(
//                top = pv.calculateTopPadding(),
//                bottom = pv.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // HERO HEADER (scrolls with list)
            item(key = "hero") {
                HeroHeader(
                    title = "Active Dispatch",
                    // Replace with your skyline asset (see notes below)
                    imageRes = R.drawable.nashville_header,
                )
                Spacer(Modifier.height(8.dp))
            }

            // INCIDENT CELLS (clickable)
            items(
                items = incidents,
                key = { it.id }
            ) { incident ->
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
                    ADIncidentCell(
                        incident = incident, modifier = Modifier.fillMaxWidth())
                }
            }

            // Optional: empty/placeholder when list is empty
            if (incidents.isEmpty()) {
                item(key = "loading") {
                    Text(
                        text = "Loading incidents…",
                        modifier = Modifier
                            .fillMaxWidth()

                            .padding(top = 24.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally),
                        color = Color(0x99FFFFFF)
                    )
                }
            }

            item(key = "bottomSpacer") {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/** Top hero with skyline image + gradient + big title */
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
        // Background image
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        // Gradient overlay (subtle darkening for text contrast)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xAA0B0E2A), Color(0x660B0E2A), Color(0x330B0E2A))
                    )
                )
        )
        // Title
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

private class FakeLocationProvider(
    latLng: LatLng? = LatLng(36.1627, -86.7816) // downtown Nashville
) : LocationProvider {
    private val _flow = MutableStateFlow(latLng)
    override val locationFlow: StateFlow<LatLng?> = _flow
    override fun startLocationUpdates(scope: CoroutineScope) { /* no-op */ }
    override fun stopLocationUpdates() { /* no-op */ }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0E2A)
@Composable
fun HomeScreenPreview() {
    // A stable user location for realistic distance text
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
        onMapClick = {}
    )
}