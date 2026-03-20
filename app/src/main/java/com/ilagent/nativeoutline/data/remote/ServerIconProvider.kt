package com.ilagent.nativeoutline.data.remote

import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import com.ilagent.nativeoutline.utils.CrashlyticsLogger

interface ServerIconProvider {
    suspend fun icon(serverHost: String): String?

    class FlagsApiDotCom(
        private val ipCountryCodeProvider: IpCountryCodeProvider,
        private val preferencesManager: PreferencesManager
    ) : ServerIconProvider {

        private val FLAG_API_URL = "https://flagcdn.com/w40/%s.png"
        // Список устаревших доменов, которые больше не работают
        private val deprecatedDomains = listOf("flagsapi.com")

        override suspend fun icon(serverHost: String): String? {
            return try {
                // Проверяем кэш
                val savedFlagUrl = preferencesManager.getFlagUrl(serverHost)
                
                // Если есть сохраненный URL и он не использует устаревший домен
                if (savedFlagUrl != null && deprecatedDomains.none { savedFlagUrl.contains(it) }) {
                    return savedFlagUrl
                }

                // Если нет в кэше или URL устарел, получаем код страны
                val countryCode = ipCountryCodeProvider.countryCode(serverHost)
                    ?: return null

                // Формируем URL флага
                val serverIconUrl = FLAG_API_URL.format(countryCode)
                preferencesManager.saveFlagUrl(serverHost, serverIconUrl)

                serverIconUrl
            } catch (e: Exception) {
                CrashlyticsLogger.logException(e, "Failed to get server icon for host: $serverHost")
                null
            }
        }
    }
}
