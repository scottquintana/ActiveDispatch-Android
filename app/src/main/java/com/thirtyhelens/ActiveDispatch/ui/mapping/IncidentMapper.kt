package com.thirtyhelens.ActiveDispatch.ui.mapping

import com.google.android.gms.maps.model.LatLng
import com.thirtyhelens.ActiveDispatch.models.ADPayload
import com.thirtyhelens.ActiveDispatch.ui.theme.AppColors
import com.thirtyhelens.ActiveDispatch.ui.theme.AppIcons
import com.thirtyhelens.ActiveDispatch.utils.formatDistanceAway
import com.thirtyhelens.ActiveDispatch.models.ADIncident
import com.thirtyhelens.ActiveDispatch.models.AlertBadge
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

object IncidentMapper {

    // Maps a network payload + (optional) user location into a UI-friendly ADIncident.
    // - Computes distance text (if user location is available)
    // - Formats time-ago and last-updated text
    // - Picks the right badge based on incident type code

    fun toUi(
        payload: ADPayload,
        userLatLng: LatLng?
    ): ADIncident {
        val incidentLatLng = LatLng(payload.lat, payload.lon)
        val neighborhood = extractNeighborhoodFromAddress(payload.address)

        val distanceText = if (userLatLng != null) {
            formatDistanceAway(
                userLocation = userLatLng,
                incidentLatLng = incidentLatLng,
                neighborhood = neighborhood
            )
        } else {
            neighborhood
        }

        return ADIncident(
            id = payload.id,
            title = payload.extras.incidentTypeName.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            },
            badge = badgeFor(payload.extras.incidentTypeCode),
            locationText = distanceText,
            timeAgo = formatTimeAgoFromIso(payload.callTimeReceived),
            lastUpdated = formatDateFromIso(payload.updatedAt),
            coordinates = incidentLatLng
        )
    }

    // Helpers

    private fun badgeFor(code: String): AlertBadge = when (code) {
        "52P", "53P" -> AlertBadge(AppColors.AccentRed, AppIcons.Bell)
        "70A", "70P" -> AlertBadge(AppColors.AccentGreen, AppIcons.Bell)
        "71A", "71P" -> AlertBadge(AppColors.AccentLightPurple, AppIcons.Business)
        "64P"        -> AlertBadge(AppColors.AccentGold, AppIcons.PersonExclamation)
        "83P", "51P" -> AlertBadge(AppColors.AccentRed, AppIcons.Shield)
        "87T"        -> AlertBadge(AppColors.AccentGreen, AppIcons.TreeDown)
        "87W"        -> AlertBadge(AppColors.AccentGold, AppIcons.WiresDown)
        "8000"       -> AlertBadge(AppColors.AccentRed, AppIcons.TriangleExclamation)
        else         -> AlertBadge(AppColors.AccentGold, AppIcons.Bell)
    }

    private fun formatTimeAgoFromIso(isoString: String): String {
        val incidentInstant = parseIsoInstant(isoString) ?: return ""
        val now = Instant.now()
        val duration = Duration.between(incidentInstant, now)
        val minutes = duration.toMinutes()
        return if (minutes < 60) {
            "$minutes min ago"
        } else {
            val hours = duration.toHours()
            val remainderMinutes = minutes % 60
            "$hours h ${remainderMinutes} m ago"
        }
    }

    private fun formatDateFromIso(isoString: String): String {
        val instant = parseIsoInstant(isoString) ?: return ""
        val date = Date.from(instant)
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    private fun parseIsoInstant(isoString: String): Instant? =
        try { Instant.parse(isoString) } catch (_: DateTimeParseException) { null }

    private fun extractNeighborhoodFromAddress(address: String): String {
        // "Thompson Ln / Powell Ave, Woodbine, TN" -> "Woodbine"
        // "5700 Crossings Blvd, Antioch, TN"       -> "Antioch"
        val parts = address.split(",").map { it.trim() }
        return if (parts.size >= 2) parts[1] else address
    }
}