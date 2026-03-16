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
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
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
    context: Context? = null,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(R.color.vpn_white_overlay)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // VPN button with defaultWeight for proper sizing
        VpnConnectButtonGlance(
            isConnected = isConnected,
            isConnectionLoading = false,
            context = context,
            modifier = GlanceModifier
                .fillMaxHeight()
                .defaultWeight()
        )
        // Spacer between button and logo
        Spacer(modifier = GlanceModifier.width(8.dp))
        // Logo
        Image(
            provider = ImageProvider(R.drawable.logo),
            contentDescription = "App Logo",
            modifier = GlanceModifier
                .fillMaxHeight()
                .defaultWeight()
                .let {
                    if (context != null) {
                        it.clickable(
                            actionStartActivity(
                                Intent(context, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                            )
                        )
                    } else {
                        it
                    }
                },
            contentScale = ContentScale.Fit
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@androidx.compose.runtime.Composable
@Preview
private fun VpnWidgetContentPreview() {
    GlanceTheme {
        VpnWidgetContent(
            isConnected = false,
            context = null
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@androidx.compose.runtime.Composable
@Preview
private fun VpnWidgetContentConnectedPreview() {
    GlanceTheme {
        VpnWidgetContent(
            isConnected = true,
            context = null
        )
    }
}
