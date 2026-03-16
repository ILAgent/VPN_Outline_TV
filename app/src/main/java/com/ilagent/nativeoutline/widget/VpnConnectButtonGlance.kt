package com.ilagent.nativeoutline.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import com.ilagent.nativeoutline.R

@Composable
fun VpnConnectButtonGlance(
    isConnected: Boolean,
    isConnectionLoading: Boolean,
    context: Context? = null,
) {
    // Use compound drawable that scales properly while maintaining aspect ratio
    val buttonDrawableRes = when {
        isConnectionLoading -> R.drawable.btn_vpn_loading
        isConnected -> R.drawable.btn_vpn_connected
        else -> R.drawable.btn_vpn_disconnected
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .let { modifier ->
                if (!isConnectionLoading && context != null) {
                    modifier.clickable(
                        actionSendBroadcast(
                            Intent(context, VpnWidgetActionReceiver::class.java).apply {
                                action = VpnWidgetActionReceiver.ACTION_TOGGLE_VPN
                            }
                        )
                    )
                } else {
                    modifier
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(buttonDrawableRes),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview
private fun VpnConnectButtonGlancePreview() {
    VpnConnectButtonGlance(
        isConnected = false,
        isConnectionLoading = false,
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview
private fun VpnConnectButtonGlanceConnectedPreview() {
    VpnConnectButtonGlance(
        isConnected = true,
        isConnectionLoading = false,
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview
private fun VpnConnectButtonGlanceLoadingPreview() {
    VpnConnectButtonGlance(
        isConnected = false,
        isConnectionLoading = true,
    )
}
