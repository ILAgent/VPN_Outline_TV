package com.ilagent.nativeoutline.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Stable
class ThemeViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _themeMode = MutableStateFlow(
        ThemeMode.valueOf(preferencesManager.getSelectedThemeMode().uppercase())
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.saveSelectedThemeMode(mode.name.lowercase())
            _themeMode.value = mode
        }
    }
}


