package com.ilagent.nativeoutline.utils

import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

/**
 * Утилитный класс для логирования ошибок в Firebase Crashlytics и событий в Firebase Analytics
 */
object CrashlyticsLogger {

    private const val TAG = "CrashlyticsLogger"
    private val crashlytics: FirebaseCrashlytics by lazy {
        Firebase.crashlytics
    }
    private val analytics: FirebaseAnalytics by lazy {
        Firebase.analytics
    }

    /**
     * Логирует исключение как non-fatal ошибку
     * @param throwable Исключение для логирования
     * @param message Дополнительное сообщение (опционально)
     */
    fun logException(throwable: Throwable, message: String? = null) {
        if (message != null) {
            Log.e(TAG, message, throwable)
            crashlytics.setCustomKey("error_message", message)
        } else {
            Log.e(TAG, "Exception logged", throwable)
        }
        crashlytics.recordException(throwable)
    }

    /**
     * Логирует ошибку с сообщением как non-fatal
     * @param message Сообщение об ошибке
     * @param cause Причина ошибки (опционально)
     */
    fun logError(message: String, cause: Throwable? = null) {
        Log.e(TAG, message, cause)
        crashlytics.setCustomKey("error_message", message)
        if (cause != null) {
            crashlytics.recordException(cause)
        } else {
            crashlytics.recordException(Exception(message))
        }
    }

    /**
     * Логирует ошибку с дополнительными параметрами
     * @param throwable Исключение для логирования
     * @param params Карта дополнительных параметров для контекста
     */
    fun logExceptionWithParams(throwable: Throwable, params: Map<String, Any?>) {
        Log.e(TAG, "Exception with params: $params", throwable)
        params.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value?.toString() ?: "null")
        }
        crashlytics.recordException(throwable)
    }

    /**
     * Устанавливает пользовательский ключ для контекста
     * @param key Ключ
     * @param value Значение
     */
    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Устанавливает идентификатор пользователя
     * @param userId Идентификатор пользователя
     */
    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    /**
     * Логирует сообщение (не ошибку)
     * @param message Сообщение для логирования
     */
    fun log(message: String) {
        Log.d(TAG, message)
        crashlytics.log(message)
    }

    // ========== Логирование событий пользователя ==========

    /**
     * Логирует событие подключения VPN
     * @param serverName Имя сервера
     * @param isWhitelistMode Режим белого списка (true - whitelist, false - all apps)
     * @param whitelistCount Количество приложений в белом списке
     */
    fun logVpnConnected(serverName: String, isWhitelistMode: Boolean = false, whitelistCount: Int = 0) {
        Log.i(TAG, "VPN connected to: $serverName, whitelist mode: $isWhitelistMode, apps count: $whitelistCount")
        analytics.logEvent("vpn_connected") {
            param("server_name", serverName)
            param("whitelist_mode", if (isWhitelistMode) 1L else 0L)
            param("whitelist_count", whitelistCount.toLong())
        }
    }

    /**
     * Логирует событие отключения VPN
     * @param serverName Имя сервера
     */
    fun logVpnDisconnected(serverName: String) {
        Log.i(TAG, "VPN disconnected from: $serverName")
        analytics.logEvent("vpn_disconnected") {
            param("server_name", serverName)
        }
    }

    /**
     * Логирует событие добавления сервера
     * @param serverName Имя сервера
     */
    fun logServerAdded(serverName: String) {
        Log.i(TAG, "Server added: $serverName")
        analytics.logEvent("server_added") {
            param("server_name", serverName)
        }
    }

    /**
     * Логирует событие удаления сервера
     * @param serverName Имя сервера
     */
    fun logServerDeleted(serverName: String) {
        Log.i(TAG, "Server deleted: $serverName")
        analytics.logEvent("server_deleted") {
            param("server_name", serverName)
        }
    }

    /**
     * Логирует событие изменения DNS
     * @param dnsServer DNS сервер
     */
    fun logDnsChanged(dnsServer: String) {
        Log.i(TAG, "DNS changed to: $dnsServer")
        analytics.logEvent("dns_changed") {
            param("dns_server", dnsServer)
        }
    }

    /**
     * Логирует событие изменения темы
     * @param theme Тема (dark или light)
     */
    fun logThemeChanged(theme: String) {
        Log.i(TAG, "Theme changed to: $theme")
        analytics.logEvent("theme_changed") {
            param("theme", theme)
        }
    }

    /**
     * Логирует событие изменения автоподключения
     * @param enabled Включено ли автоподключение
     */
    fun logAutoConnectionChanged(enabled: Boolean) {
        Log.i(TAG, "Auto connection changed to: $enabled")
        analytics.logEvent("auto_connection_changed") {
            param("enabled", if (enabled) 1L else 0L)
        }
    }

    /**
     * Логирует событие изменения белого списка приложений
     * @param action Действие (all_apps, whitelist, added_apps, removed_app)
     */
    fun logWhitelistChanged(action: String) {
        Log.i(TAG, "Whitelist changed: $action")
        analytics.logEvent("whitelist_changed") {
            param("action", action)
        }
    }

    /**
     * Логирует событие сканирования QR кода
     */
    fun logQrScanStarted() {
        Log.i(TAG, "QR scan started")
        analytics.logEvent("qr_scan_started") {}
    }

    /**
     * Логирует успешное сканирование QR кода
     */
    fun logQrScanSuccess() {
        Log.i(TAG, "QR scan success")
        analytics.logEvent("qr_scan_success") {}
    }

    /**
     * Логирует неудачное сканирование QR кода
     * @param error Ошибка
     */
    fun logQrScanFailed(error: String) {
        Log.i(TAG, "QR scan failed: $error")
        analytics.logEvent("qr_scan_failed") {
            param("error", error)
        }
    }

    /**
     * Логирует событие обновления приложения
     * @param version Новая версия
     */
    fun logAppUpdate(version: String) {
        Log.i(TAG, "App update to version: $version")
        analytics.logEvent("app_update") {
            param("version", version)
        }
    }

    /**
     * Логирует открытие настроек
     */
    fun logSettingsOpened() {
        Log.i(TAG, "Settings opened")
        analytics.logEvent("settings_opened") {}
    }

    /**
     * Логирует открытие диалога добавления сервера
     */
    fun logServerDialogOpened() {
        Log.i(TAG, "Server dialog opened")
        analytics.logEvent("server_dialog_opened") {}
    }

    /**
     * Логирует импорт сервера из файла
     */
    fun logServerImportedFromFile() {
        Log.i(TAG, "Server imported from file")
        analytics.logEvent("server_imported_from_file") {}
    }

    /**
     * Логирует импорт сервера из буфера обмена
     */
    fun logServerImportedFromClipboard() {
        Log.i(TAG, "Server imported from clipboard")
        analytics.logEvent("server_imported_from_clipboard") {}
    }

    /**
     * Логирует импорт сервера через QR код
     */
    fun logServerImportedFromQr() {
        Log.i(TAG, "Server imported from QR")
        analytics.logEvent("server_imported_from_qr") {}
    }
}