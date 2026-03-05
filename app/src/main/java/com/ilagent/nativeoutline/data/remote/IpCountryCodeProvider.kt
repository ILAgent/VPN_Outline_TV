package com.ilagent.nativeoutline.data.remote

import com.ilagent.nativeoutline.utils.CrashlyticsLogger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface IpCountryCodeProvider {
    suspend fun countryCode(serverIp: String): String?

    class IpApiDotCo(private val fetch: RemoteJSONFetch) : IpCountryCodeProvider {

        private val API_URL = "https://ipapi.co/%s/json/"
        private val json = Json { ignoreUnknownKeys = true }

        @Serializable
        private data class IpApiResponse(
            val country_code: String? = null
        )

        override suspend fun countryCode(serverIp: String): String? {
            return try {
                val url = API_URL.format(serverIp)
                val response = fetch.fetch(url)
                val data = json.decodeFromString<IpApiResponse>(response)
                data.country_code?.lowercase()
            } catch (e: Exception) {
                CrashlyticsLogger.logException(e, "Failed to get country code for IP: $serverIp")
                null
            }
        }
    }
}