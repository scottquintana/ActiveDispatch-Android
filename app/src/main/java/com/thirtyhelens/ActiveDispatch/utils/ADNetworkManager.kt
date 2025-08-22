package com.thirtyhelens.ActiveDispatch.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.thirtyhelens.ActiveDispatch.models.ADResponse

private const val BASE_URL = "https://activedispatch-313918647466.us-east4.run.app/v1/city"

enum class City(val path: String) {
    NASHVILLE("nashville"),
    PDX("pdx"),
    SF("sf");
}

object ADNetworkManager {

    // Reusable client; swap OkHttp -> CIO for KMP later, or use expect/actual.
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true  // lets "extras" vary between cities in the future
                    isLenient = true
                    encodeDefaults = false
                }
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 15_000
        }
        // You can add Logging plugin here if you want verbose logging in debug builds.
    }

    /**
     * GET https://.../v1/city/{city}
     */
    suspend fun fetchCity(city: com.thirtyhelens.ActiveDispatch.models.City): ADResponse {
        print("Getting city data")
        val endpoint = "$BASE_URL/${city.path}"
        return client.get {
            print("Got it")
            url(endpoint)
        }.body()
    }
}

sealed class ADError(message: String? = null) : Exception(message) {
    object InvalidResponse : ADError("Invalid response from server")
    object InvalidData : ADError("Unable to parse data")
    object Network : ADError("Network error occurred")
}