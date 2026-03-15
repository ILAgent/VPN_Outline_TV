package com.ilagent.nativeoutline.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.ilagent.nativeoutline.R
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import com.ilagent.nativeoutline.domain.VpnStateManager

class VpnWidget : GlanceAppWidget() {

    companion object {
        private const val TAG = "VpnWidget"
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d(TAG, "provideGlance called for widget $id")

        provideContent {
            GlanceTheme {
                val isConnected by VpnStateManager.isRunning.collectAsState(initial = false)

                Log.d(TAG, "Widget state: isVpnConnected=$isConnected")

                VpnWidgetContent(
                    isConnected = isConnected,
                    context = context
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun VpnWidgetContent(
    isConnected: Boolean,
    context: Context
) {
    val preferencesManager = PreferencesManager(context)
    val serverName = preferencesManager.selectedServerName ?: "No Server"

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(R.color.vpn_white_overlay)
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left section: VPN Connect Button
            Box(
                modifier = GlanceModifier
                    .width(108.dp)
                    .height(108.dp),
                contentAlignment = Alignment.Center
            ) {
                VpnConnectButtonGlance(
                    isConnected = isConnected,
                    isConnectionLoading = false,
                    context = context
                )
            }

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Right section: Logo and server name
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = GlanceModifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = serverName,
                    style = TextStyle(
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2
                )
            }
        }
    }
}
