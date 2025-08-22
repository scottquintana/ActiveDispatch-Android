package com.thirtyhelens.ActiveDispatch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object NetworkManager {

    private const val baseUrl =
        "https://services2.arcgis.com/HdTo6HJqh92wn4D8/arcgis/rest/services/Metro_Nashville_Police_Department_Active_Dispatch_Table_view/FeatureServer/0/query?outFields=*&where=1%3D1&f=geojson"

    private val jsonDecoder = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getAlerts(): Result<List<ADIncidentData>> = withContext(Dispatchers.IO) {
        try {
            val connection = URL(baseUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                return@withContext Result.failure(ADError.InvalidResponse)
            }

            val stream = connection.inputStream.bufferedReader().use { it.readText() }
            val payload = jsonDecoder.decodeFromString(DispatchPayload.serializer(), stream)
            val alerts = payload.features.map { it.properties }

            Result.success(alerts)

        } catch (e: IOException) {
            Result.failure(ADError.Network)
        } catch (e: Exception) {
            Result.failure(ADError.InvalidData)
        }
    }
}

sealed class ADError(message: String? = null) : Exception(message) {
    object InvalidResponse : ADError("Invalid response from server")
    object InvalidData : ADError("Unable to parse data")
    object Network : ADError("Network error occurred")
}