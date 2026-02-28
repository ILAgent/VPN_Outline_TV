package com.ilagent.nativeoutline.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

/**
 * Утилитный класс для логирования ошибок в Firebase Crashlytics
 */
object CrashlyticsLogger {

    private val crashlytics: FirebaseCrashlytics by lazy {
        Firebase.crashlytics
    }

    /**
     * Логирует исключение как non-fatal ошибку
     * @param throwable Исключение для логирования
     * @param message Дополнительное сообщение (опционально)
     */
    fun logException(throwable: Throwable, message: String? = null) {
        if (message != null) {
            crashlytics.setCustomKey("error_message", message)
        }
        crashlytics.recordException(throwable)
    }

    /**
     * Логирует ошибку с сообщением как non-fatal
     * @param message Сообщение об ошибке
     * @param cause Причина ошибки (опционально)
     */
    fun logError(message: String, cause: Throwable? = null) {
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
        crashlytics.log(message)
    }
}