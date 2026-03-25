package com.ilagent.nativeoutline.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Stable
import androidx.core.content.edit
import com.ilagent.nativeoutline.data.model.VpnServerInfo
import com.ilagent.nativeoutline.utils.CrashlyticsLogger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Stable
class PreferencesManager(context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true // Полезно для обратной совместимости
        encodeDefaults = true // Сохранять значения по умолчанию
    }

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveVpnKeys(keys: List<VpnServerInfo>) {
        val jsonString = json.encodeToString(keys)
        preferences.edit { putString(KEY_VPN_LIST, jsonString) }
    }

    fun getVpnKeys(): List<VpnServerInfo> {
        val jsonString = preferences.getString(KEY_VPN_LIST, null)
        return if (jsonString.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                json.decodeFromString<List<VpnServerInfo>>(jsonString)
            } catch (e: Exception) {
                CrashlyticsLogger.logException(e, "Failed to decode VPN keys from preferences")
                emptyList()
            }
        }
    }

    fun saveSelectedThemeMode(themeMode: String) {
        preferences.edit { putString(KEY_SELECTED_THEME, themeMode) }
    }

    fun getSelectedThemeMode(): String {
        return preferences.getString(KEY_SELECTED_THEME, "system") ?: "system"
    }

    fun addOrUpdateVpnKey(serverName: String, key: String) {
        val existingList = getVpnKeys().toMutableList()
        val index = existingList.indexOfFirst { it.name == serverName }
        if (index >= 0) {
            existingList[index] = VpnServerInfo(name = serverName, key = key)
        } else {
            existingList.add(VpnServerInfo(name = serverName, key = key))
        }
        saveVpnKeys(existingList)
    }

    fun deleteVpnKey(serverName: String): VpnServerInfo? {
        val existingList = getVpnKeys().toMutableList()
        val index = existingList.indexOfFirst { it.name == serverName }
        if (index >= 0) {
            existingList.removeAt(index)
            saveVpnKeys(existingList)
            
            // Если удаляемый сервер был выбран, обновляем выбор
            if (selectedServerName == serverName) {
                if (existingList.isNotEmpty()) {
                    // Выбираем первый сервер из списка
                    val firstServer = existingList.first()
                    selectedServerName = firstServer.name
                    return firstServer
                } else {
                    // Если список пуст, очищаем выбранный сервер
                    selectedServerName = null
                    return null
                }
            }
        }
        return null
    }

    fun saveVpnStartTime(startTime: Long) {
        preferences.edit { putLong(KEY_VPN_START_TIME, startTime) }
    }

    fun getVpnStartTime(): Long {
        return preferences.getLong(KEY_VPN_START_TIME, 0L)
    }

    fun clearVpnStartTime() {
        preferences.edit { remove(KEY_VPN_START_TIME) }
    }

    var selectedServerName: String?
        set(name) = preferences.edit { putString(KEY_SERVER_NAME, name) }
        get() = preferences.getString(KEY_SERVER_NAME, null)

    fun saveFlagUrl(ip: String, flagUrl: String) {
        preferences.edit { putString("flag_$ip", flagUrl) }
    }

    fun getFlagUrl(ip: String): String? {
        return preferences.getString("flag_$ip", null)
    }

    fun saveSelectedDns(dns: String) {
        preferences.edit { putString(KEY_SELECTED_DNS, dns) }
    }

    fun getSelectedDns(): String? {
        return preferences.getString(KEY_SELECTED_DNS, null)
    }

    fun saveSelectedApps(apps: List<String>) {
        preferences.edit { putStringSet(KEY_SELECTED_APPS, apps.toSet()) }
    }

    fun getSelectedApps(): Set<String>? {
        return preferences.getStringSet(KEY_SELECTED_APPS, null)
    }

    fun setAutoConnectionEnabled(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_AUTO_CONNECTION, enabled) }
    }

    fun isAutoConnectionEnabled(): Boolean {
        return preferences.getBoolean(KEY_AUTO_CONNECTION, false)
    }

    fun saveSelectedLanguage(languageCode: String) {
        preferences.edit { putString(KEY_SELECTED_LANGUAGE, languageCode) }
    }

    fun getSelectedLanguage(): String? {
        return preferences.getString(KEY_SELECTED_LANGUAGE, null)
    }

    fun saveSystemLanguage(languageCode: String) {
        preferences.edit { putString(KEY_SYSTEM_LANGUAGE, languageCode) }
    }

    fun getSystemLanguage(): String? {
        return preferences.getString(KEY_SYSTEM_LANGUAGE, null)
    }

    /**
     * Generates a unique default server name based on existing servers.
     * Finds the highest number in "Server #N" pattern and returns "Server #(N+1)"
     */
    fun generateDefaultServerName(): String {
        val existingServers = getVpnKeys()
        val serverNumberPattern = Regex("^Server #(\\d+)$")
        
        val maxNumber = existingServers.mapNotNull { server ->
            serverNumberPattern.find(server.name)?.groupValues?.get(1)?.toIntOrNull()
        }.maxOrNull() ?: 0
        
        return "Server #${maxNumber + 1}"
    }

    companion object {
        private const val PREFS_NAME = "outline_vpn_prefs"
        private const val KEY_VPN_LIST = "vpn_keys_list"
        private const val KEY_VPN_START_TIME = "vpn_start_time"
        private const val KEY_SERVER_NAME = "server_name"
        private const val KEY_SELECTED_DNS = "selected_dns"
        private const val KEY_SELECTED_APPS = "selected_apps"
        private const val KEY_SELECTED_THEME = "selected_theme"
        private const val KEY_AUTO_CONNECTION = "auto_connection_enabled"
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
        private const val KEY_SYSTEM_LANGUAGE = "system_language"
    }
}