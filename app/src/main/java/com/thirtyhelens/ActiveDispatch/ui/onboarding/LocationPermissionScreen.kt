package com.thirtyhelens.ActiveDispatch.ui.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun LocationPermissionScreen(
    onFinished: () -> Unit
) {
    val ctx = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onFinished()
    }

    Scaffold { pv ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pv)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Use Your Location?",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = """
We use your location to calculate distance to incidents.

• We do not sell your location data.
• We do not share your location for marketing.
• You can change this later in Settings.

The app still works if you choose Not Now.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium
            )
            Column {
                Button(
                    onClick = {
                        launcher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Allow Location") }

                TextButton(
                    onClick = { onFinished() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) { Text("Not Now") }

                // Optional “Open Settings” helper if they want to manage later
                TextButton(
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${ctx.packageName}")
                        )
                        ctx.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) { Text("Open App Settings") }
            }
        }
    }
}