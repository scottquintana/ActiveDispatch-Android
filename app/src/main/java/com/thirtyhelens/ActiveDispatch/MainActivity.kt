package com.thirtyhelens.ActiveDispatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.util.Log
import com.thirtyhelens.ActiveDispatch.models.City
import com.thirtyhelens.ActiveDispatch.utils.LocationManager
import com.thirtyhelens.ActiveDispatch.views.IncidentList

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Keep these stable across recompositions
            val locationManager = remember { LocationManager(applicationContext) }
            val viewModel = remember { ADIncidentsViewModel(locationProvider = locationManager) }

            // Kick off the network call once
            LaunchedEffect(Unit) {
                Log.d("MainActivity", "Fetching incidents...")
                try {
                    viewModel.getIncidents(City.NASHVILLE)
                    Log.d("MainActivity", "Fetch successful")
                } catch (e: Exception) {
                    Log.e("MainActivity", "Fetch failed", e)
                }
            }

            Scaffold(modifier = Modifier.fillMaxSize()) { pv: PaddingValues ->
                // Optional: quick empty state while incidents load
                val incidents = viewModel.incidents.collectAsState().value
                if (incidents.isEmpty()) {
                    Text(
                        text = "Loading incidents…",
                        modifier = Modifier
                            .padding(pv)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                } else {
                    IncidentList(viewModel = viewModel)
                }
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

//@Preview(showBackground = true)
//@Composable
//fun AppPreview() {
//    // Use a fake/mock LocationManager since applicationContext is not available
//    val fakeLocationManager = LocationManager(context = LocalContext.current)
//    val viewModel = ADIncidentsViewModel(locationManager = fakeLocationManager)
//
//    IncidentList(viewModel = viewModel)
//}