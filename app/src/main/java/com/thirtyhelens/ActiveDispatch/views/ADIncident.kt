package com.thirtyhelens.ActiveDispatch.views

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.android.gms.maps.model.LatLng

data class ADIncident(
    val id: String,
    val title: String,
    val badge: AlertBadge,
    val locationText: String,
    val timeAgo: String,
    val lastUpdated: String,
    val coordinates: LatLng? // nullable if geocoding fails
)

data class AlertBadge(
    val color: Color,
    val icon: ImageVector // or painter if you're using custom icons
)