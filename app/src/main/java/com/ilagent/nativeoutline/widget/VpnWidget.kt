package com.ilagent.nativeoutline.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.ilagent.nativeoutline.MainActivity
import com.ilagent.nativeoutline.data.preferences.PreferencesManager

class VpnWidget : GlanceAppWidget() {

    companion object {
        private const val TAG = "VpnWidget"
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d(TAG, "provideGlance called for widget $id")

        provideContent {
            GlanceTheme {
                val preferencesManager = PreferencesManager(context)
                val vpnStartTime by preferencesManager.vpnStartTimeFlow.collectAsState(initial = 0L)
                val isConnected = vpnStartTime > 0
                val serverName =
                    preferencesManager.selectedServerName ?: "No server"

                Log.d(
                    TAG,
                    "Widget state: vpnStartTime=$vpnStartTime, isVpnConnected=$isConnected, serverName=$serverName"
                )

                VpnWidgetContent(
                    isConnected = isConnected,
                    serverName = serverName,
                    context = context
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun VpnWidgetContent(
    isConnected: Boolean,
    serverName: String,
    context: Context
) {
    val backgroundColor = if (isConnected) {
        ColorProvider(day = Color(0xFF4CAF50), night = Color(0xFF4CAF50)) // Green for connected
    } else {
        ColorProvider(day = Color(0xFF757575), night = Color(0xFF757575)) // Gray for disconnected
    }

    val textColor = ColorProvider(day = Color.White, night = Color.White)

    val statusText = if (isConnected) {
        "VPN Connected"
    } else {
        "VPN Disconnected"
    }

    val intent = Intent(context, MainActivity::class.java)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(
                actionStartActivity(intent)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // VPN Icon
            Text(
                text = if (isConnected) "🔒" else "🔓",
                style = TextStyle(color = textColor)
            )

            // Status text
            Text(
                text = statusText,
                style = TextStyle(color = textColor)
            )

            // Server name
            Text(
                text = serverName,
                style = TextStyle(color = textColor)
            )
        }
    }
}