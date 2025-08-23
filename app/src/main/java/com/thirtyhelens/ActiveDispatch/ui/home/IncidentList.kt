package com.thirtyhelens.ActiveDispatch.ui.home

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.model.LatLng
import com.thirtyhelens.ActiveDispatch.models.ADResponse
import com.thirtyhelens.ActiveDispatch.utils.FakeLocationProvider
import com.thirtyhelens.ActiveDispatch.models.ADIncident
import com.thirtyhelens.ActiveDispatch.ui.components.ADIncidentCell
import com.thirtyhelens.ActiveDispatch.ui.mapping.IncidentMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun IncidentList(viewModel: ADIncidentsViewModel) {
    val incidents = viewModel.incidents.collectAsState().value

    LazyColumn {
        items(items = incidents, key = { it.id }) { incident ->
            ADIncidentCell(incident = incident, modifier = Modifier)
        }
    }
}


@Preview(showBackground = false)
@Composable
fun IncidentListPreview() {
    // Fake user location (Downtown Nashville)
    val userLatLng = LatLng(36.1627, -86.7816)

    val mockIncidents = remember {
        ADResponse.mockData.places.map { payload ->
            IncidentMapper.toUi(payload, userLatLng)
        }
    }

    val fakeViewModel = object : ADIncidentsViewModel(
        locationProvider = FakeLocationProvider()
    ) {
        override val incidents: StateFlow<List<ADIncident>> = MutableStateFlow(mockIncidents)
    }

    IncidentList(viewModel = fakeViewModel)
}