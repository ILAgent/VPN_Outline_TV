package com.ilagent.nativeoutline.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import com.ilagent.nativeoutline.data.remote.ParseUrlOutline
import com.ilagent.nativeoutline.domain.OutlineVpnManager
import com.ilagent.nativeoutline.domain.update.UpdateManager
import com.ilagent.nativeoutline.viewmodel.state.SingleLiveEvent
import com.ilagent.nativeoutline.viewmodel.state.VpnEvent
import com.ilagent.nativeoutline.viewmodel.state.VpnServerStateUi
import kotlinx.coroutines.launch

class MainViewModel(
    private val preferencesManager: PreferencesManager,
    private val vpnManager: OutlineVpnManager,
    private val parseUrlOutline: ParseUrlOutline,
    private val updateManager: UpdateManager,
) : ViewModel() {

    private val _vpnServerState = MutableLiveData<VpnServerStateUi>()
    val vpnServerState: LiveData<VpnServerStateUi> get() = _vpnServerState

    private val _vpnConnectionState = MutableLiveData<Boolean>()
    val vpnConnectionState: LiveData<Boolean> get() = _vpnConnectionState

    private val _errorEvent = SingleLiveEvent<Unit>()
    val errorEvent: LiveData<Unit> get() = _errorEvent


    private val _isConnecting = MutableLiveData(false)
    val isConnecting: LiveData<Boolean> = _isConnecting

    suspend fun checkForAppUpdates(currentVersion: String, onUpdateAvailable: (String) -> Unit) {
        val updateStatus = updateManager.checkForAppUpdates(currentVersion)
        if (updateStatus is UpdateManager.UpdateStatus.Available) {
            onUpdateAvailable(updateStatus.latestVersion)
        }
    }

    fun updateAppToLatest(
        onProgress: (Int) -> Unit,
        onFinished: () -> Unit,
        onError: (Throwable?) -> Unit
    ) {
        viewModelScope.launch {
            updateManager.downloadAndInstallLatestApk(onProgress, onError)
            onFinished()
        }
    }

    fun startVpn(configString: String) {
        _isConnecting.value = true
        viewModelScope.launch {
            runCatching { parseUrlOutline.parse(configString) }
                .onSuccess { config -> vpnManager.start(config) }
                .onFailure { errorVpnEvent() }
        }
    }

    fun stopVpn() {
        vpnManager.stop()
    }

    fun checkVpnConnectionState() {
        val isVpnConnected = vpnManager.isConnected()

        _vpnConnectionState.value = isVpnConnected
    }

    fun loadLastVpnServerState() {
        val keys = preferencesManager.getVpnKeys()
        val selectedServer = keys.find { it.name == preferencesManager.serverName }

        if (selectedServer == null) {
            _vpnServerState.value = VpnServerStateUi(
                name = "",
                host = "",
                url = "",
                startTime = 0L
            )
            return
        }

        val url = selectedServer.key
        val host = runCatching { parseUrlOutline.extractServerHost(url) ?: "" }.getOrDefault("")
        val startTime = preferencesManager.getVpnStartTime()

        _vpnServerState.value = VpnServerStateUi(
            name = selectedServer.name,
            host = host,
            url = url,
            startTime = startTime
        )
    }

    fun saveVpnServer(name: String, url: String) {
        preferencesManager.addOrUpdateVpnKey(name, url)
        preferencesManager.clearVpnStartTime()
        preferencesManager.serverName = name

        val host = runCatching { parseUrlOutline.extractServerHost(url) ?: "" }.getOrDefault("")
        _vpnServerState.value = VpnServerStateUi(name = name, host = host, url = url)
    }

    fun vpnEvent(event: VpnEvent) {
        _isConnecting.value = false
        when (event) {
            VpnEvent.STARTED -> {
                val started = System.currentTimeMillis()
                preferencesManager.saveVpnStartTime(started)
                _vpnServerState.value = _vpnServerState.value?.copy(startTime = started)
                _vpnConnectionState.value = true
            }

            VpnEvent.STOPPED -> {
                preferencesManager.clearVpnStartTime()
                _vpnServerState.value = _vpnServerState.value?.copy(startTime = 0L)
                _vpnConnectionState.value = false
            }

            VpnEvent.ERROR -> {
                preferencesManager.clearVpnStartTime()
                errorVpnEvent()
            }
        }
    }

    private fun errorVpnEvent() {
        _errorEvent.value = Unit
        checkVpnConnectionState()
    }

    class Factory(
        private val preferencesManager: PreferencesManager,
        private val vpnManager: OutlineVpnManager,
        private val parseUrlOutline: ParseUrlOutline,
        private val updateManager: UpdateManager,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(
                preferencesManager,
                vpnManager,
                parseUrlOutline,
                updateManager
            ) as T
        }
    }
}