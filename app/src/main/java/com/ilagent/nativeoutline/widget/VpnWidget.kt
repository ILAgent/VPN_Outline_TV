package com.ilagent.nativeoutline.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.ilagent.nativeoutline.MainActivity
import com.ilagent.nativeoutline.R
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
    // Status emojis - shield icon and check/cross
    val statusEmoji = if (isConnected) "🛡️✅" else "🛡️❌"

    // Background color resource based on status
    val bgColorRes = if (isConnected) {
        R.color.vpn_connected_overlay
    } else {
        R.color.vpn_disconnected_overlay
    }

    val intent = Intent(context, MainActivity::class.java)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity(intent)),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(R.color.vpn_white_overlay)
        ) {}
        // Semi-transparent colored overlay
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bgColorRes)
        ) {
            // Status indicator centered in the overlay
            Text(
                text = statusEmoji,
                style = TextStyle(
                    textAlign = TextAlign.Center
                ),
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(4.dp)
            )
        }
        // Background with app logo
        Image(
            provider = ImageProvider(R.drawable.logo),
            contentDescription = "VPN Background",
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
