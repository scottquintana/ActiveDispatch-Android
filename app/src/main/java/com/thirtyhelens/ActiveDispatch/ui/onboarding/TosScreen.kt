package com.thirtyhelens.ActiveDispatch.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TosScreen(onAcknowledge: () -> Unit) {
    Scaffold { pv ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pv)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Important Notice",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = """
Active Dispatch is an informational application. It is not an emergency service and must not be relied upon as a primary source for public safety alerts, police dispatch, medical response, or any other life-safety communication.

Data presented may be delayed, incomplete, or inaccurate. You are solely responsible for how you use this information. Active Dispatch and its providers make no warranties and disclaim all liability for damages or losses arising from use of the app.

For emergencies or official guidance, always contact local emergency services (e.g., 911) or your local law-enforcement agency.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = onAcknowledge,
                modifier = Modifier.fillMaxWidth()
            ) { Text("I Understand") }
        }
    }
}
