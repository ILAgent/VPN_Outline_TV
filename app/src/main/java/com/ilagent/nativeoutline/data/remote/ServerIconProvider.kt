package com.ilagent.nativeoutline.data.remote

import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import com.ilagent.nativeoutline.utils.CrashlyticsLogger

interface ServerIconProvider {
    suspend fun icon(serverHost: String): String?

    /**
     * Base class for flag URL providers with URL availability checking
     */
    abstract class BaseFlagProvider(
        private val ipCountryCodeProvider: IpCountryCodeProvider,
        private val preferencesManager: PreferencesManager,
        private val urlChecker: UrlChecker,
        private val flagApiUrl: String
    ) : ServerIconProvider {

        // Список устаревших доменов, которые больше не работают
        protected open val deprecatedDomains: List<String> = listOf("flagsapi.com")

        override suspend fun icon(serverHost: String): String? {
            return try {
                // Проверяем кэш
                val savedFlagUrl = preferencesManager.getFlagUrl(serverHost)

                // Если есть сохраненный URL, он не использует устаревший домен и доступен
                if (savedFlagUrl != null && deprecatedDomains.none { savedFlagUrl.contains(it) }) {
                    if (urlChecker.isUrlAvailable(savedFlagUrl)) {
                        return savedFlagUrl
                    }
                    // Если URL недоступен, очищаем кэш и пробуем получить новый
                    preferencesManager.removeFlagUrl(serverHost)
                }

                // Получаем код страны
                val countryCode = ipCountryCodeProvider.countryCode(serverHost)
                    ?: return null

                // Формируем URL флага
                val serverIconUrl = flagApiUrl.format(countryCode)

                // Проверяем доступность URL
                if (urlChecker.isUrlAvailable(serverIconUrl)) {
                    preferencesManager.saveFlagUrl(serverHost, serverIconUrl)
                    serverIconUrl
                } else {
                    null
                }
            } catch (e: Exception) {
                CrashlyticsLogger.logException(
                    e,
                    "${this::class.simpleName} failed for host: $serverHost"
                )
                null
            }
        }
    }

    /**
     * Provider for flag URLs using flagcdn.com
     * Free, no rate limit, reliable CDN
     */
    class FlagCdnProvider(
        ipCountryCodeProvider: IpCountryCodeProvider,
        preferencesManager: PreferencesManager,
        urlChecker: UrlChecker
    ) : BaseFlagProvider(
        ipCountryCodeProvider,
        preferencesManager,
        urlChecker,
        "https://flagcdn.com/w40/%s.png"
    )

    /**
     * Provider for flag URLs using countryflagsapi.com
     * Free tier available
     */
    class CountryFlagsApiProvider(
        ipCountryCodeProvider: IpCountryCodeProvider,
        preferencesManager: PreferencesManager,
        urlChecker: UrlChecker
    ) : BaseFlagProvider(
        ipCountryCodeProvider,
        preferencesManager,
        urlChecker,
        "https://countryflagsapi.com/png/%s"
    )

    /**
     * Provider for flag URLs using flagpedia.net
     * Free, no rate limit
     */
    class FlagpediaProvider(
        ipCountryCodeProvider: IpCountryCodeProvider,
        preferencesManager: PreferencesManager,
        urlChecker: UrlChecker
    ) : BaseFlagProvider(
        ipCountryCodeProvider,
        preferencesManager,
        urlChecker,
        "https://flagpedia.net/data/flags/w40/%s.png"
    )

    /**
     * Provider for flag URLs using jsdelivr CDN with flag-icons
     */
    class FlagIconsIoProvider(
        ipCountryCodeProvider: IpCountryCodeProvider,
        preferencesManager: PreferencesManager,
        urlChecker: UrlChecker
    ) : BaseFlagProvider(
        ipCountryCodeProvider,
        preferencesManager,
        urlChecker,
        "https://cdn.jsdelivr.net/gh/lipis/flag-icons@7.2.3/flags/4x3/%s.svg"
    )

    /**
     * Fallback provider that tries multiple icon providers in order until one succeeds.
     * If all providers fail, returns null.
     */
    class FallbackServerIconProvider(
        private val providers: List<ServerIconProvider>
    ) : ServerIconProvider {

        constructor(vararg providers: ServerIconProvider) : this(providers.toList())

        override suspend fun icon(serverHost: String): String? {
            for (provider in providers) {
                val result = provider.icon(serverHost)
                if (result != null) {
                    return result
                }
            }
            CrashlyticsLogger.logException(
                Exception("All icon providers failed"),
                "Failed to get server icon for host: $serverHost"
            )
            return null
        }
    }
    
    companion object {
        /**
         * Creates a default fallback provider with all available flag providers in recommended order.
         */
        fun createDefault(
            ipCountryCodeProvider: IpCountryCodeProvider,
            preferencesManager: PreferencesManager,
            urlChecker: UrlChecker = UrlChecker.HttpUrlChecker()
        ): ServerIconProvider {
            return FallbackServerIconProvider(
                FlagCdnProvider(ipCountryCodeProvider, preferencesManager, urlChecker),
                CountryFlagsApiProvider(ipCountryCodeProvider, preferencesManager, urlChecker),
                FlagpediaProvider(ipCountryCodeProvider, preferencesManager, urlChecker),
                FlagIconsIoProvider(ipCountryCodeProvider, preferencesManager, urlChecker)
            )
        }
    }
}
