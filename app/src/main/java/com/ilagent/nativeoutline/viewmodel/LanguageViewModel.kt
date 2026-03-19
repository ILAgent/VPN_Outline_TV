package com.ilagent.nativeoutline.viewmodel

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Stable
class LanguageViewModel(context: Context) : ViewModel() {
    private val preferencesManager = PreferencesManager(context)

    private val _selectedLanguage = MutableStateFlow<String?>(null)
    val selectedLanguage: StateFlow<String?> = _selectedLanguage.asStateFlow()

    private val _currentLanguage = MutableStateFlow(
        preferencesManager.getSelectedLanguage() ?: "system"
    )
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _languageCode = mutableStateOf(
        preferencesManager.getSelectedLanguage() ?: "system"
    )
    val languageCode: State<String> = _languageCode

    init {
        viewModelScope.launch {
            val savedLanguage = preferencesManager.getSelectedLanguage() ?: "system"
            _currentLanguage.value = savedLanguage
            _languageCode.value = savedLanguage
        }
    }

    fun setLanguage(languageCode: String) {
        val currentLanguage = _currentLanguage.value
        if (currentLanguage == languageCode) {
            return // Language hasn't changed, don't trigger restart
        }

        preferencesManager.saveSelectedLanguage(languageCode)
        _currentLanguage.value = languageCode
        _languageCode.value = languageCode
        _selectedLanguage.value = languageCode
    }

    companion object {
        fun getSupportedLanguages(context: android.content.Context): List<Language> {
            return listOf(
                Language(
                    "system",
                    context.getString(com.ilagent.nativeoutline.R.string.system_language)
                ),
                Language("en", "English"),
                Language("ru", "Русский"),
                Language("zh-rCN", "简体中文"),
                Language("zh-rTW", "繁體中文"),
                Language("es", "Español"),
                Language("fr", "Français"),
                Language("de", "Deutsch")
            )
        }
    }
}

data class Language(
    val code: String,
    val displayName: String
)