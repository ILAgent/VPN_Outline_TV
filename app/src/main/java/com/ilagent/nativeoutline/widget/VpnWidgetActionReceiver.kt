package com.ilagent.nativeoutline.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ilagent.nativeoutline.MainActivity
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import com.ilagent.nativeoutline.data.remote.ParseUrlOutline
import com.ilagent.nativeoutline.data.remote.RemoteJSONFetch
import com.ilagent.nativeoutline.domain.OutlineVpnService
import com.ilagent.nativeoutline.domain.VpnStateManager
import com.ilagent.nativeoutline.utils.CrashlyticsLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VpnWidgetActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_TOGGLE_VPN = "com.ilagent.nativeoutline.widget.TOGGLE_VPN"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TOGGLE_VPN) {
            val isConnected = VpnStateManager.isVpnConnected()

            if (isConnected) {
                // Disconnect VPN
                OutlineVpnService.stop(context)
            } else {
                // Connect VPN
                val preferencesManager = PreferencesManager(context)
                val serverName = preferencesManager.selectedServerName
                val vpnKeys = preferencesManager.getVpnKeys()
                val serverUrl = vpnKeys.find { it.name == serverName }?.key ?: ""

                if (serverUrl.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val parseUrlOutline =
                                ParseUrlOutline.Base(RemoteJSONFetch.HttpURLConnectionJSONFetch())
                            val config = parseUrlOutline.parse(serverUrl)
                            OutlineVpnService.start(context, config)
                        } catch (e: Exception) {
                            CrashlyticsLogger.logException(
                                e,
                                "Error when starting OutlineVpnService in VpnWidgetActionReceiver"
                            )
                            startMainActivity(context)
                        }
                    }
                } else {
                    startMainActivity(context)
                }
            }
        }
    }

    private fun startMainActivity(context: Context) {
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(mainIntent)
    }
}