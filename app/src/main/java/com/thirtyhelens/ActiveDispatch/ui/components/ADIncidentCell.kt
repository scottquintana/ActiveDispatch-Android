package com.thirtyhelens.ActiveDispatch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import com.google.android.gms.maps.model.LatLng
import com.thirtyhelens.ActiveDispatch.models.ADIncident
import com.thirtyhelens.ActiveDispatch.models.AlertBadge
import com.thirtyhelens.ActiveDispatch.utils.formatDistanceAway
import com.thirtyhelens.ActiveDispatch.ui.theme.AppColors
import com.thirtyhelens.ActiveDispatch.ui.theme.AppIcons

@Composable
fun ADIncidentCell(
    incident: ADIncident,
    modifier: Modifier
) {
    Box(
        modifier = Modifier
            .padding(bottom = 2.dp)
            .fillMaxWidth()
            .height(80.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(AppColors.GradientTop, AppColors.GradientBottom)
                ), shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = incident.timeAgo.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.DetailText,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 0.dp)
                    .padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = incident.badge.icon,
                    contentDescription = null,
                    tint = incident.badge.color,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .padding(end = 12.dp)
                        .size(24.dp)
                )

                Column {
                    Text(
                        text = incident.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Text(
                        text = incident.locationText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.DetailText
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ADIncidentCellPreview() {
    val userLatLng = LatLng(36.1600, -86.7800)
    val incidentLatLng = LatLng(36.1627, -86.7816)

    val incident = ADIncident(
        id = "asdfawefaw",
        title = "Fight/Assault",
        badge = AlertBadge(
            color = Color(0xFFF03261),   // your accentRed
            icon = AppIcons.Shield
        ),
        locationText = formatDistanceAway(
            userLocation = userLatLng,
            incidentLatLng = incidentLatLng,
            neighborhood = "Somewhere"
        ),
        timeAgo = "5 min ago",
        lastUpdated = "2:41 PM",
        coordinates = incidentLatLng
    )

    ADIncidentCell(incident = incident, modifier = Modifier)
}
