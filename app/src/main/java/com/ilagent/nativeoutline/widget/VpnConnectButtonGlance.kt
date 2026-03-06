package com.ilagent.nativeoutline.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.size
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.ilagent.nativeoutline.MainActivity
import com.ilagent.nativeoutline.R

@androidx.compose.runtime.Composable
fun VpnConnectButtonGlance(
    isConnected: Boolean,
    isConnectionLoading: Boolean,
    context: Context? = null,
) {
    val intent = context?.let { Intent(context, MainActivity::class.java) }

    if (isConnectionLoading) {
        // Loading state - show a simple loading indicator
        Box(
            modifier = GlanceModifier
                .size(120.dp)
                //.clickable(actionStartActivity(intent))
                .background(R.color.vpn_disconnected_overlay),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⏳",
                style = TextStyle(textAlign = TextAlign.Center)
            )
        }
    } else {
        // Connected or disconnected state
        val bgColorRes = if (isConnected) {
            R.color.vpn_connected_overlay
        } else {
            R.color.vpn_disconnected_overlay
        }

        val iconEmoji = if (isConnected) "⏸️" else "▶️"
        val statusText = if (isConnected) "OFF" else "ON"

        Box(
            modifier = GlanceModifier
                .size(120.dp)
                //.clickable(actionStartActivity(intent))
                .background(bgColorRes),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = iconEmoji,
                    style = TextStyle(textAlign = TextAlign.Center)
                )
                Text(
                    text = statusText,
                    style = TextStyle(textAlign = TextAlign.Center)
                )
            }
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@androidx.compose.runtime.Composable
@Preview()
private fun VpnConnectButtonGlancePreview() {
    VpnConnectButtonGlance(
        isConnected = false,
        isConnectionLoading = false,
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@androidx.compose.runtime.Composable
@Preview()
private fun VpnConnectButtonGlanceConnectedPreview() {
    VpnConnectButtonGlance(
        isConnected = true,
        isConnectionLoading = false,
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@androidx.compose.runtime.Composable
@Preview()
private fun VpnConnectButtonGlanceLoadingPreview() {
    VpnConnectButtonGlance(
        isConnected = false,
        isConnectionLoading = true,
    )
}