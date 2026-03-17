package com.ilagent.nativeoutline.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.ilagent.nativeoutline.data.broadcast.BroadcastVpnServiceAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver для обновления виджета при изменении состояния VPN.
 * Слушает broadcast-сообщения от OutlineVpnService и обновляет все экземпляры виджета.
 */
class VpnWidgetUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BroadcastVpnServiceAction.STARTED,
            BroadcastVpnServiceAction.STOPPED,
            BroadcastVpnServiceAction.ERROR -> {
                // Обновляем все экземпляры виджета в coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    val glanceManager = GlanceAppWidgetManager(context)
                    val glanceIds = glanceManager.getGlanceIds(VpnWidget::class.java)
                    glanceIds.forEach { glanceId ->
                        VpnWidget().update(context, glanceId)
                    }
                }
            }
        }
    }
}