package com.ilagent.nativeoutline.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilagent.nativeoutline.R
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

    companion object {
        @Composable
        fun getSupportedThemes(): List<ThemeOption> {
            return listOf(
                ThemeOption(
                    mode = ThemeMode.LIGHT,
                    displayName = stringResource(id = R.string.theme_light)
                ),
                ThemeOption(
                    mode = ThemeMode.DARK,
                    displayName = stringResource(id = R.string.theme_dark)
                ),
                ThemeOption(
                    mode = ThemeMode.SYSTEM,
                    displayName = stringResource(id = R.string.theme_system)
                )
            )
        }
    }
}

data class ThemeOption(
    val mode: ThemeMode,
    val displayName: String
)

@Composable
fun getThemeDisplayName(themeMode: ThemeMode): String {
    return when (themeMode) {
        ThemeMode.LIGHT -> stringResource(id = R.string.theme_light)
        ThemeMode.DARK -> stringResource(id = R.string.theme_dark)
        ThemeMode.SYSTEM -> stringResource(id = R.string.theme_system)
    }
}


