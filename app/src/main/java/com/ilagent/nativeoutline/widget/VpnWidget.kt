package com.ilagent.nativeoutline.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.ilagent.nativeoutline.MainActivity
import com.ilagent.nativeoutline.R
import com.ilagent.nativeoutline.domain.OutlineVpnService
import com.ilagent.nativeoutline.data.preferences.PreferencesManager

class VpnWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate called with ${appWidgetIds.size} widget IDs")
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d(TAG, "onReceive called with action: ${intent.action}")
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, VpnWidget::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        private const val TAG = "VpnWidget"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val preferencesManager = PreferencesManager(context)
            // Use VPN start time to check if VPN is connected (more reliable than static variable)
            val vpnStartTime = preferencesManager.getVpnStartTime()
            val isVpnConnected = vpnStartTime > 0
            val serverName = preferencesManager.selectedServerName ?: context.getString(R.string.no_server)
            
            Log.d(TAG, "updateAppWidget: vpnStartTime=$vpnStartTime, isVpnConnected=$isVpnConnected, serverName=$serverName")

            val views = RemoteViews(context.packageName, R.layout.vpn_widget_layout)

            // Set background color
            val backgroundColor = if (isVpnConnected) {
                context.getColor(R.color.vpn_connected_background)
            } else {
                context.getColor(R.color.vpn_disconnected_background)
            }
            views.setInt(R.id.widget_container, "setBackgroundColor", backgroundColor)

            // Set VPN icon
            val iconText = if (isVpnConnected) "🔒" else "🔓"
            views.setTextViewText(R.id.vpn_icon, iconText)

            // Set status text
            val statusText = if (isVpnConnected) {
                context.getString(R.string.vpn_connected)
            } else {
                context.getString(R.string.vpn_disconnected)
            }
            views.setTextViewText(R.id.vpn_status, statusText)

            // Set status text color
            val statusColor = if (isVpnConnected) {
                context.getColor(R.color.vpn_connected_text)
            } else {
                context.getColor(R.color.vpn_disconnected_text)
            }
            views.setTextColor(R.id.vpn_status, statusColor)

            // Set server name
            views.setTextViewText(R.id.server_name, serverName)

            // Set click intent
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            Log.d(TAG, "updateAllWidgets called")
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, VpnWidget::class.java)
            )
            Log.d(TAG, "Found ${appWidgetIds.size} widget(s) to update")
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}