package com.ilagent.nativeoutline.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ilagent.nativeoutline.data.broadcast.BroadcastVpnServiceAction

class VpnWidgetUpdateReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "VpnWidgetUpdateReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive called with action: ${intent.action}")
        when (intent.action) {
            BroadcastVpnServiceAction.STARTED,
            BroadcastVpnServiceAction.STOPPED,
            BroadcastVpnServiceAction.ERROR -> {
                Log.d(TAG, "VPN state changed, updating all widgets")
                // Update all widgets when VPN state changes
                VpnWidget.updateAllWidgets(context)
            }
        }
    }
}