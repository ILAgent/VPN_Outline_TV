package com.ilagent.nativeoutline.viewmodel

import androidx.lifecycle.ViewModel
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AutoConnectViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {

    private val _isAutoConnectEnabled = MutableStateFlow(preferencesManager.isAutoConnectionEnabled())
    val isAutoConnectEnabled = _isAutoConnectEnabled.asStateFlow()

    fun setAutoConnectEnabled(enabled: Boolean) {
        preferencesManager.setAutoConnectionEnabled(enabled)
        _isAutoConnectEnabled.value = enabled
    }
}
