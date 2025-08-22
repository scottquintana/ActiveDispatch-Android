package com.thirtyhelens.ActiveDispatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thirtyhelens.ActiveDispatch.utils.LocationManager
import com.thirtyhelens.ActiveDispatch.views.IncidentList

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val locationManager = LocationManager(context = applicationContext)
            val viewModel = ADIncidentsViewModel(locationProvider = locationManager)

            Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                IncidentList(viewModel = viewModel)
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