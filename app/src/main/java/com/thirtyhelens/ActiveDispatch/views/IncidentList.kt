package com.thirtyhelens.ActiveDispatch.views


import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


// Your app-specific imports (adjust these based on your actual package structure)
import com.thirtyhelens.ActiveDispatch.views.ADIncidentCell
import com.thirtyhelens.ActiveDispatch.views.ADIncident
import com.thirtyhelens.ActiveDispatch.views.AlertBadge
import com.thirtyhelens.ActiveDispatch.ui.theme.AppColors
import com.thirtyhelens.ActiveDispatch.ui.theme.AppIcons
import com.thirtyhelens.ActiveDispatch.ADIncidentsViewModel
import com.thirtyhelens.ActiveDispatch.LocationManager
import com.google.android.gms.maps.model.LatLng
import com.thirtyhelens.ActiveDispatch.mockDispatchPayload

@Composable
fun IncidentList(viewModel: ADIncidentsViewModel) {
    val incidents = viewModel.incidents.collectAsState().value

    LazyColumn {
        items(
            items = incidents,
            key = { it.id }
        ) { incident ->
            ADIncidentCell(incident = incident, modifier = Modifier)
        }
    }
}

@Preview(showBackground = false)
@Composable
fun IncidentListPreview() {
    val context = LocalContext.current.applicationContext
    val locationManager = remember { LocationManager(context) }

    val mockIncidents = mockDispatchPayload.features.map {
        ADIncidentsViewModel(locationManager).mapToUIIncident(it.properties)
    }

    val fakeViewModel = remember {
        object : ADIncidentsViewModel(locationManager) {
            override val incidents: StateFlow<List<ADIncident>> = MutableStateFlow(mockIncidents)
        }
    }

    IncidentList(viewModel = fakeViewModel)
}
