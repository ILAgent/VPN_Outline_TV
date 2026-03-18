package com.ilagent.nativeoutline.viewmodel

import android.app.Application
import android.content.res.Configuration
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class LanguageViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)

    private val _currentLanguage = MutableStateFlow(
        preferencesManager.getSelectedLanguage() ?: Locale.getDefault().language
    )
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _languageCode = mutableStateOf(
        preferencesManager.getSelectedLanguage() ?: Locale.getDefault().language
    )
    val languageCode: State<String> = _languageCode

    init {
        viewModelScope.launch {
            _currentLanguage.value =
                preferencesManager.getSelectedLanguage() ?: Locale.getDefault().language
            _languageCode.value = _currentLanguage.value
        }
    }

    fun setLanguage(languageCode: String) {
        preferencesManager.saveSelectedLanguage(languageCode)
        _currentLanguage.value = languageCode
        _languageCode.value = languageCode
        updateAppLanguage(languageCode)
    }

    private fun updateAppLanguage(languageCode: String) {
        val locale = when (languageCode) {
            "system" -> {
                val systemLanguageCode = preferencesManager.getSystemLanguage() ?: "en"
                when (systemLanguageCode) {
                    "zh-rTW" -> Locale("zh", "TW")
                    else -> Locale(systemLanguageCode)
                }
            }
            "zh-rTW" -> Locale("zh", "TW")
            else -> Locale(languageCode)
        }
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        val context = getApplication<Application>()
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    companion object {
        fun getSupportedLanguages(context: android.content.Context): List<Language> {
            return listOf(
                Language("system", context.getString(com.ilagent.nativeoutline.R.string.system_language)),
                Language("en", "English"),
                Language("ru", "Русский"),
                Language("zh-rTW", "中文")
            )
        }
    }
}

data class Language(
    val code: String,
    val displayName: String
)