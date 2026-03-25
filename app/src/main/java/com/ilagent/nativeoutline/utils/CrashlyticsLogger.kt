package com.ilagent.nativeoutline.utils

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
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
    fun logVpnConnected(
        serverName: String,
        isWhitelistMode: Boolean = false,
        whitelistCount: Int = 0
    ) {
        Log.i(
            TAG,
            "VPN connected to: $serverName, whitelist mode: $isWhitelistMode, apps count: $whitelistCount"
        )
        val params = Bundle().apply {
            putString("server_name", serverName)
            putLong("whitelist_mode", if (isWhitelistMode) 1L else 0L)
            putLong("whitelist_count", whitelistCount.toLong())
        }
        analytics.logEvent("vpn_connected", params)
    }

    /**
     * Логирует событие отключения VPN
     * @param serverName Имя сервера
     */
    fun logVpnDisconnected(serverName: String) {
        Log.i(TAG, "VPN disconnected from: $serverName")
        analytics.logEvent(
            "vpn_disconnected",
            Bundle().apply { putString("server_name", serverName) })
    }

    /**
     * Логирует событие добавления сервера
     * @param serverName Имя сервера
     */
    fun logServerAdded(serverName: String) {
        Log.i(TAG, "Server added: $serverName")
        analytics.logEvent("server_added", Bundle().apply { putString("server_name", serverName) })
    }

    /**
     * Логирует событие удаления сервера
     * @param serverName Имя сервера
     */
    fun logServerDeleted(serverName: String) {
        Log.i(TAG, "Server deleted: $serverName")
        analytics.logEvent(
            "server_deleted",
            Bundle().apply { putString("server_name", serverName) })
    }

    /**
     * Логирует событие изменения DNS
     * @param dnsServer DNS сервер
     */
    fun logDnsChanged(dnsServer: String) {
        Log.i(TAG, "DNS changed to: $dnsServer")
        analytics.logEvent("dns_changed", Bundle().apply { putString("dns_server", dnsServer) })
    }

    /**
     * Логирует событие изменения темы
     * @param theme Тема (dark или light)
     */
    fun logThemeChanged(theme: String) {
        Log.i(TAG, "Theme changed to: $theme")
        analytics.logEvent("theme_changed", Bundle().apply { putString("theme", theme) })
    }

    /**
     * Логирует событие изменения автоподключения
     * @param enabled Включено ли автоподключение
     */
    fun logAutoConnectionChanged(enabled: Boolean) {
        Log.i(TAG, "Auto connection changed to: $enabled")
        analytics.logEvent(
            "auto_connection_changed",
            Bundle().apply { putLong("enabled", if (enabled) 1L else 0L) })
    }

    /**
     * Логирует событие изменения языка
     * @param languageCode Код языка (en, ru, zh)
     */
    fun logLanguageChanged(languageCode: String) {
        Log.i(TAG, "Language changed to: $languageCode")
        analytics.logEvent(
            "language_changed",
            Bundle().apply { putString("language", languageCode) })
    }

    /**
     * Логирует событие изменения белого списка приложений
     * @param action Действие (all_apps, whitelist, added_apps, removed_app)
     */
    fun logWhitelistChanged(action: String) {
        Log.i(TAG, "Whitelist changed: $action")
        analytics.logEvent("whitelist_changed", Bundle().apply { putString("action", action) })
    }

    /**
     * Логирует событие сканирования QR кода
     */
    fun logQrScanStarted() {
        Log.i(TAG, "QR scan started")
        analytics.logEvent("qr_scan_started", null)
    }

    /**
     * Логирует успешное сканирование QR кода
     */
    fun logQrScanSuccess() {
        Log.i(TAG, "QR scan success")
        analytics.logEvent("qr_scan_success", null)
    }

    /**
     * Логирует неудачное сканирование QR кода
     * @param error Ошибка
     */
    fun logQrScanFailed(error: String) {
        Log.i(TAG, "QR scan failed: $error")
        analytics.logEvent("qr_scan_failed", Bundle().apply { putString("error", error) })
    }

    /**
     * Логирует событие обновления приложения
     * @param status Статус обновления (start, success, failed)
     * @param version Новая версия
     */
    fun logAppUpdate(status: String, version: String) {
        Log.i(TAG, "App update $status to version: $version")
        analytics.logEvent("app_updating", Bundle().apply {
            putString("status", status)
            putString("version", version)
        })
    }

    /**
     * Логирует открытие настроек
     */
    fun logSettingsOpened() {
        Log.i(TAG, "Settings opened")
        analytics.logEvent("settings_opened", null)
    }

    /**
     * Логирует открытие диалога добавления сервера
     */
    fun logServerDialogOpened() {
        Log.i(TAG, "Server dialog opened")
        analytics.logEvent("server_dialog_opened", null)
    }

    /**
     * Логирует открытие диалога выбора сервера
     */
    fun logServerListDialogOpened() {
        Log.i(TAG, "Server list dialog opened")
        analytics.logEvent("server_list_dialog_opened", null)
    }

    /**
     * Логирует выбор сервера из списка
     * @param serverName Имя выбранного сервера
     */
    fun logServerSelected(serverName: String) {
        Log.i(TAG, "Server selected: $serverName")
        analytics.logEvent("server_selected", Bundle().apply { putString("server_name", serverName) })
    }

    /**
     * Логирует нажатие кнопки "Добавить сервер" в диалоге выбора
     */
    fun logAddServerButtonClicked() {
        Log.i(TAG, "Add server button clicked")
        analytics.logEvent("add_server_button_clicked", null)
    }

    /**
     * Логирует отмену добавления сервера
     */
    fun logServerAddCancelled() {
        Log.i(TAG, "Server add cancelled")
        analytics.logEvent("server_add_cancelled", null)
    }

    /**
     * Логирует ошибку валидации ключа сервера
     * @param errorType Тип ошибки (invalid_format, empty_key, etc.)
     */
    fun logServerKeyValidationError(errorType: String) {
        Log.i(TAG, "Server key validation error: $errorType")
        analytics.logEvent("server_key_validation_error", Bundle().apply { putString("error_type", errorType) })
    }

    /**
     * Логирует импорт сервера из файла
     */
    fun logServerImportedFromFile() {
        Log.i(TAG, "Server imported from file")
        analytics.logEvent("server_imported_from_file", null)
    }

    /**
     * Логирует импорт сервера из буфера обмена
     */
    fun logServerImportedFromClipboard() {
        Log.i(TAG, "Server imported from clipboard")
        analytics.logEvent("server_imported_from_clipboard", null)
    }

    /**
     * Логирует импорт сервера через QR код
     */
    fun logServerImportedFromQr() {
        Log.i(TAG, "Server imported from QR")
        analytics.logEvent("server_imported_from_qr", null)
    }

    // ========== Логирование событий виджета ==========

    /**
     * Логирует добавление виджета на домашний экран
     */
    fun logWidgetAdded() {
        Log.i(TAG, "Widget added")
        analytics.logEvent("widget_added", null)
    }

    /**
     * Логирует удаление виджета с домашнего экрана
     */
    fun logWidgetRemoved() {
        Log.i(TAG, "Widget removed")
        analytics.logEvent("widget_removed", null)
    }

    /**
     * Логирует запуск VPN через виджет
     * @param serverName Имя сервера
     */
    fun logWidgetVpnStarted(serverName: String) {
        Log.i(TAG, "Widget VPN started: $serverName")
        analytics.logEvent(
            "widget_vpn_started",
            Bundle().apply { putString("server_name", serverName) })
    }

    /**
     * Логирует остановку VPN через виджет
     * @param serverName Имя сервера
     */
    fun logWidgetVpnStopped(serverName: String) {
        Log.i(TAG, "Widget VPN stopped: $serverName")
        analytics.logEvent(
            "widget_vpn_stopped",
            Bundle().apply { putString("server_name", serverName) })
    }

    /**
     * Логирует открытие приложения через виджет
     */
    fun logWidgetAppOpened() {
        Log.i(TAG, "Widget app opened")
        analytics.logEvent("widget_app_opened", null)
    }
}