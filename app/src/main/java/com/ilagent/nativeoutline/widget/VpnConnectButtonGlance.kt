package com.ilagent.nativeoutline.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.layout.ContentScale
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import com.ilagent.nativeoutline.R

@Composable
fun VpnConnectButtonGlance(
    isConnected: Boolean,
    context: Context? = null,
    modifier: GlanceModifier = GlanceModifier,
) {
    val buttonDrawableRes = when {
        isConnected -> R.drawable.btn_vpn_connected
        else -> R.drawable.btn_vpn_disconnected
    }

    Image(
        provider = ImageProvider(buttonDrawableRes),
        contentDescription = null,
        modifier = modifier
            .let { mod ->
                if (context != null) {
                    mod.clickable(
                        actionSendBroadcast(
                            Intent(context, VpnWidgetActionReceiver::class.java).apply {
                                action = VpnWidgetActionReceiver.ACTION_TOGGLE_VPN
                            }
                        )
                    )
                } else {
                    mod
                }
            },
        contentScale = ContentScale.Fit
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview
private fun VpnConnectButtonGlancePreview() {
    VpnConnectButtonGlance(
        isConnected = false,
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview
private fun VpnConnectButtonGlanceConnectedPreview() {
    VpnConnectButtonGlance(
        isConnected = true,
    )
}