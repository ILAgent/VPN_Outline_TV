package com.ilagent.nativeoutline.data.remote

import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import com.ilagent.nativeoutline.utils.CrashlyticsLogger

interface ServerIconProvider {
    suspend fun icon(serverHost: String): String?

    class FlagsApiDotCom(
        private val ipCountryCodeProvider: IpCountryCodeProvider,
        private val preferencesManager: PreferencesManager
    ) : ServerIconProvider {

        private val API_URL = "https://flagsapi.com/%s/flat/64.png"

        override suspend fun icon(serverHost: String): String? {
            return try {
                // Сначала проверяем кэш
                val savedFlagUrl = preferencesManager.getFlagUrl(serverHost)
                if (savedFlagUrl != null) {
                    return savedFlagUrl
                }

                // Если нет в кэше, получаем код страны
                val countryCode = ipCountryCodeProvider.countryCode(serverHost)
                    ?: return null

                // Формируем URL флага
                val serverIconUrl = API_URL.format(countryCode)
                preferencesManager.saveFlagUrl(serverHost, serverIconUrl)

                serverIconUrl
            } catch (e: Exception) {
                CrashlyticsLogger.logException(e, "Failed to get server icon for host: $serverHost")
                null
            }
        }
    }
}