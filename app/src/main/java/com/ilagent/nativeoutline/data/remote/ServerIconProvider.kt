package com.ilagent.nativeoutline.data.remote

import android.util.Patterns
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import com.ilagent.nativeoutline.utils.CrashlyticsLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.util.regex.Pattern

private val IPV4_PATTERN = Pattern.compile(
    "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
)

private fun isValidIPv4(ip: String): Boolean {
    return IPV4_PATTERN.matcher(ip).matches()
}

interface ServerIconProvider {
    suspend fun icon(serverHost: String): String?

    class FlagsApiDotCom(
        private val ipCountryCodeProvider: IpCountryCodeProvider,
        private val preferencesManager: PreferencesManager
    ) : ServerIconProvider {

        override suspend fun icon(serverHost: String): String? = withContext(Dispatchers.IO) {

            val savedFlagUrl = preferencesManager.getFlagUrl(serverHost)
            if (savedFlagUrl != null) {
                return@withContext savedFlagUrl
            }

            val serverIp = if (Patterns.DOMAIN_NAME.matcher(serverHost).matches()) {
                try {
                    InetAddress.getByName(serverHost).hostAddress
                } catch (e: Exception) {
                    CrashlyticsLogger.logException(e, "Failed to resolve server host: $serverHost")
                    return@withContext null
                }
            } else serverHost

            if (serverIp == "127.0.0.1" || serverIp == "0.0.0.0" || !isValidIPv4(serverIp)) {
                return@withContext null
            }

            val countryCode = ipCountryCodeProvider.countryCode(serverIp) ?: return@withContext null

            val serverIconUrl = API_URL.format(countryCode)
            preferencesManager.saveFlagUrl(serverHost, serverIconUrl)

            return@withContext serverIconUrl
        }

        companion object {
            private const val API_URL = "https://flagsapi.com/%s/flat/64.png"
        }
    }
}