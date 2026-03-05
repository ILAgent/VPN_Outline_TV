package com.ilagent.nativeoutline.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Менеджер состояния VPN, который предоставляет StateFlow для отслеживания статуса VPN.
 * Используется для подписки на изменения состояния VPN в виджетах и других компонентах.
 */
object VpnStateManager {
    
    private val _isRunning = MutableStateFlow(false)
    
    /**
     * StateFlow, который отражает текущее состояние VPN (true - включен, false - выключен)
     */
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    /**
     * Обновляет состояние VPN
     * @param running новое состояние VPN
     */
    fun setVpnRunning(running: Boolean) {
        _isRunning.value = running
    }
    
    /**
     * Возвращает текущее состояние VPN
     */
    fun isVpnConnected(): Boolean {
        return _isRunning.value
    }
}