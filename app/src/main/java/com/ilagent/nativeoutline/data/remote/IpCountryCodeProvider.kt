package com.ilagent.nativeoutline.data.remote

import com.ilagent.nativeoutline.utils.CrashlyticsLogger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface IpCountryCodeProvider {
    suspend fun countryCode(serverIp: String): String?

    /**
     * Provider using ipapi.co service
     * Rate limit: 1000 requests/day (free tier)
     */
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
                CrashlyticsLogger.logException(e, "IpApiDotCo failed for IP: $serverIp")
                null
            }
        }
    }

    /**
     * Provider using ip-api.com service
     * Rate limit: 45 requests/minute (free tier)
     */
    class IpApiDotCom(private val fetch: RemoteJSONFetch) : IpCountryCodeProvider {

        private val API_URL = "http://ip-api.com/json/%s?fields=countryCode"
        private val json = Json { ignoreUnknownKeys = true }

        @Serializable
        private data class IpApiComResponse(
            val countryCode: String? = null,
            val status: String? = null
        )

        override suspend fun countryCode(serverIp: String): String? {
            return try {
                val url = API_URL.format(serverIp)
                val response = fetch.fetch(url)
                val data = json.decodeFromString<IpApiComResponse>(response)
                if (data.status == "success") {
                    data.countryCode?.lowercase()
                } else {
                    null
                }
            } catch (e: Exception) {
                CrashlyticsLogger.logException(e, "IpApiDotCom failed for IP: $serverIp")
                null
            }
        }
    }

    /**
     * Provider using ipinfo.io service
     * Rate limit: 50000 requests/month (free tier)
     */
    class IpInfoDotIo(private val fetch: RemoteJSONFetch) : IpCountryCodeProvider {

        private val API_URL = "https://ipinfo.io/%s/json"
        private val json = Json { ignoreUnknownKeys = true }

        @Serializable
        private data class IpInfoResponse(
            val country: String? = null,
            val bogon: Boolean? = null
        )

        override suspend fun countryCode(serverIp: String): String? {
            return try {
                val url = API_URL.format(serverIp)
                val response = fetch.fetch(url)
                val data = json.decodeFromString<IpInfoResponse>(response)
                if (data.bogon != true) {
                    data.country?.lowercase()
                } else {
                    null
                }
            } catch (e: Exception) {
                CrashlyticsLogger.logException(e, "IpInfoDotIo failed for IP: $serverIp")
                null
            }
        }
    }

    /**
     * Provider using ipwhois.app service
     * Rate limit: 10000 requests/month (free tier)
     */
    class IpWhoisApp(private val fetch: RemoteJSONFetch) : IpCountryCodeProvider {

        private val API_URL = "https://ipwhois.app/json/%s"
        private val json = Json { ignoreUnknownKeys = true }

        @Serializable
        private data class IpWhoisResponse(
            val country_code: String? = null,
            val success: Boolean? = null
        )

        override suspend fun countryCode(serverIp: String): String? {
            return try {
                val url = API_URL.format(serverIp)
                val response = fetch.fetch(url)
                val data = json.decodeFromString<IpWhoisResponse>(response)
                if (data.success != false) {
                    data.country_code?.lowercase()
                } else {
                    null
                }
            } catch (e: Exception) {
                CrashlyticsLogger.logException(e, "IpWhoisApp failed for IP: $serverIp")
                null
            }
        }
    }

    /**
     * Fallback provider that tries multiple providers in order until one succeeds.
     * If all providers fail, returns null.
     */
    class FallbackIpCountryCodeProvider(
        private val providers: List<IpCountryCodeProvider>
    ) : IpCountryCodeProvider {

        constructor(vararg providers: IpCountryCodeProvider) : this(providers.toList())

        override suspend fun countryCode(serverIp: String): String? {
            for (provider in providers) {
                val result = provider.countryCode(serverIp)
                if (result != null) {
                    return result
                }
            }
            CrashlyticsLogger.logException(
                Exception("All providers failed"),
                "Failed to get country code for IP: $serverIp"
            )
            return null
        }
    }

    companion object {
        /**
         * Creates a default fallback provider with all available providers in recommended order.
         */
        fun createDefault(fetch: RemoteJSONFetch): IpCountryCodeProvider {
            return FallbackIpCountryCodeProvider(
                IpApiDotCo(fetch),
                IpApiDotCom(fetch),
                IpInfoDotIo(fetch),
                IpWhoisApp(fetch)
            )
        }
    }
}