package com.thirtyhelens.ActiveDispatch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ADResponse(
    val city: String,
    val source: String,
    val fetchedAt: String,
    val places: List<ADPayload>
) {
    companion object {
        val mockData = ADResponse(
            city = "nashville",
            source = "nashvilleMNPD",
            fetchedAt = "2025-08-22T18:07:24.156Z",
            places = listOf(
                ADPayload(
                    id = "y06fz6jnpuh",
                    name = "Incident",
                    lat = 36.039389,
                    lon = -86.645382,
                    address = "5700 Crossings Blvd, Antioch, TN",
                    callTimeReceived = "2025-08-22T17:24:44.000Z",
                    updatedAt = "2025-08-22T17:57:00.000Z",
                    extras = ADPayload.Extras("57P", "FIGHT/ASSAULT")
                ),
                ADPayload(
                    id = "nlb62fd9rk",
                    name = "Incident",
                    lat = 36.060413,
                    lon = -86.640087,
                    address = "660 Bell Rd, Antioch, TN",
                    callTimeReceived = "2025-08-22T17:11:40.000Z",
                    updatedAt = "2025-08-22T17:57:00.000Z",
                    extras = ADPayload.Extras("71A", "NON-RESIDENCE-BURGLARY ALARM")
                ),
                ADPayload(
                    id = "7xrbdyt3pcn",
                    name = "Incident",
                    lat = 36.1493525,
                    lon = -86.6750904,
                    address = "520 Royal Pkwy, Donelson, TN",
                    callTimeReceived = "2025-08-22T17:07:11.000Z",
                    updatedAt = "2025-08-22T17:57:00.000Z",
                    extras = ADPayload.Extras("71A", "NON-RESIDENCE-BURGLARY ALARM")
                ),
                ADPayload(
                    id = "m4r31cwhxfb",
                    name = "Incident",
                    lat = 35.24226,
                    lon = -87.309308,
                    address = "726 Fordham Dr, Brentwood Davidson County, TN",
                    callTimeReceived = "2025-08-22T17:05:18.000Z",
                    updatedAt = "2025-08-22T17:57:00.000Z",
                    extras = ADPayload.Extras("70A", "RESIDENCE-BURGLARY ALARM")
                ),
                ADPayload(
                    id = "f5b6uvpe4x",
                    name = "Incident",
                    lat = 36.120544,
                    lon = -86.717951,
                    address = "E Thompson Ln / Browning Rd, Nashville, TN",
                    callTimeReceived = "2025-08-22T16:53:03.000Z",
                    updatedAt = "2025-08-22T17:57:00.000Z",
                    extras = ADPayload.Extras("57P", "FIGHT/ASSAULT")
                ),
                ADPayload(
                    id = "gcyq7i0fd4t",
                    name = "Incident",
                    lat = 36.0971295,
                    lon = -86.815558,
                    address = "1809 Castleman Dr, Green Hills, TN",
                    callTimeReceived = "2025-08-22T16:37:18.000Z",
                    updatedAt = "2025-08-22T17:57:00.000Z",
                    extras = ADPayload.Extras("70A", "RESIDENCE-BURGLARY ALARM")
                ),
                ADPayload(
                    id = "9sljvgq4dx",
                    name = "Incident",
                    lat = 36.067234,
                    lon = -86.723711,
                    address = "Thompson Ln / Powell Ave, Woodbine, TN",
                    callTimeReceived = "2025-08-22T16:17:34.000Z",
                    updatedAt = "2025-08-22T17:57:00.000Z",
                    extras = ADPayload.Extras("87W", "WIRES DOWN")
                ),
                ADPayload(
                    id = "bvtln0e2dj5",
                    name = "Incident",
                    lat = 36.0958321,
                    lon = -86.7017369,
                    address = "1182 Antioch Pike, Nashville, TN",
                    callTimeReceived = "2025-08-22T16:13:46.000Z",
                    updatedAt = "2025-08-22T17:57:00.000Z",
                    extras = ADPayload.Extras("71A", "NON-RESIDENCE-BURGLARY ALARM")
                ),
                ADPayload(
                    id = "9dsw1gx2juw",
                    name = "Incident",
                    lat = 36.0592518,
                    lon = -86.7099119,
                    address = "350 Tusculum Rd, Tusculum, TN",
                    callTimeReceived = "2025-08-22T15:52:23.000Z",
                    updatedAt = "2025-08-22T17:57:00.000Z",
                    extras = ADPayload.Extras("64P", "CORPSE/D.O.A")
                ),
                ADPayload(
                    id = "8aeofc5xzll",
                    name = "Incident",
                    lat = 36.632615,
                    lon = -86.517642,
                    address = "837 Briley Pkwy, Hermitage, TN",
                    callTimeReceived = "2025-08-22T15:51:40.000Z",
                    updatedAt = "2025-08-22T17:57:00.000Z",
                    extras = ADPayload.Extras("57P", "FIGHT/ASSAULT")
                ),
                ADPayload(
                    id = "j01rhrcal8",
                    name = "Incident",
                    lat = 36.235232,
                    lon = -86.758408,
                    address = "3300 Dickerson Pike, Nashville, TN",
                    callTimeReceived = "2025-08-22T14:58:15.000Z",
                    updatedAt = "2025-08-22T17:57:00.000Z",
                    extras = ADPayload.Extras("64P", "CORPSE/D.O.A")
                ),
                ADPayload(
                    id = "low5hg3xbmc",
                    name = "Incident",
                    lat = 36.112218,
                    lon = -86.741606,
                    address = "2844 Logan St, Woodbine, TN",
                    callTimeReceived = "2025-08-22T14:27:32.000Z",
                    updatedAt = "2025-08-22T17:57:00.000Z",
                    extras = ADPayload.Extras("71A", "NON-RESIDENCE-BURGLARY ALARM")
                ),
                ADPayload(
                    id = "k7tzepfqi9",
                    name = "Incident",
                    lat = 36.2164327,
                    lon = -86.7290493,
                    address = "3904 Gallatin Pike, Inglewood, TN",
                    callTimeReceived = "2025-08-22T12:51:00.000Z",
                    updatedAt = "2025-08-22T17:57:00.000Z",
                    extras = ADPayload.Extras("71A", "NON-RESIDENCE-BURGLARY ALARM")
                ),
                ADPayload(
                    id = "jy64osu5deg",
                    name = "Incident",
                    lat = 35.272418,
                    lon = -87.32052,
                    address = "1984 Carothers Rd, Nolensville Davidson County, TN",
                    callTimeReceived = "2025-08-22T11:53:31.000Z",
                    updatedAt = "2025-08-22T17:57:00.000Z",
                    extras = ADPayload.Extras("70A", "RESIDENCE-BURGLARY ALARM")
                )
            )
        )
    }
}

@Serializable
data class ADPayload(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val address: String,
    val callTimeReceived: String,
    val updatedAt: String,
    val extras: Extras
) {
    @Serializable
    data class Extras(
        val incidentTypeCode: String,
        val incidentTypeName: String
    )
}

val mockADResponse = ADResponse(
    city = "nashville",
    source = "nashvilleMNPD",
    fetchedAt = "2025-08-22T18:07:24.156Z",
    places = listOf(
        ADPayload(
            id = "y06fz6jnpuh",
            name = "Incident",
            lat = 36.039389,
            lon = -86.645382,
            address = "5700 Crossings Blvd, Antioch, TN",
            callTimeReceived = "2025-08-22T17:24:44.000Z",
            updatedAt = "2025-08-22T17:57:00.000Z",
            extras = ADPayload.Extras(
                incidentTypeCode = "57P",
                incidentTypeName = "FIGHT/ASSAULT"
            )
        ),
        ADPayload(
            id = "nlb62fd9rk",
            name = "Incident",
            lat = 36.060413,
            lon = -86.640087,
            address = "660 Bell Rd, Antioch, TN",
            callTimeReceived = "2025-08-22T17:11:40.000Z",
            updatedAt = "2025-08-22T17:57:00.000Z",
            extras = ADPayload.Extras(
                incidentTypeCode = "71A",
                incidentTypeName = "NON-RESIDENCE-BURGLARY ALARM"
            )
        ),
        ADPayload(
            id = "7xrbdyt3pcn",
            name = "Incident",
            lat = 36.1493525,
            lon = -86.6750904,
            address = "520 Royal Pkwy, Donelson, TN",
            callTimeReceived = "2025-08-22T17:07:11.000Z",
            updatedAt = "2025-08-22T17:57:00.000Z",
            extras = ADPayload.Extras(
                incidentTypeCode = "71A",
                incidentTypeName = "NON-RESIDENCE-BURGLARY ALARM"
            )
        )
        // ... add more entries if needed
    )
)