package com.ilagent.nativeoutline.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.padding
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
    if (isConnectionLoading) {
        // Loading state - show loading indicator with rounded corners
        Box(
            modifier = GlanceModifier
                .size(100.dp)
                .padding(4.dp)
                .apply {
                    context?.let {
                        clickable(
                            actionStartActivity(
                                Intent(
                                    context,
                                    MainActivity::class.java
                                )
                            )
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Inner box with background drawable and rounded corners
            Box(
                modifier = GlanceModifier
                    .size(92.dp)
                    .background(ImageProvider(R.drawable.bg_vpn_button_loading)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_loading_static),
                    contentDescription = null,
                    modifier = GlanceModifier.size(50.dp)
                )
            }
        }
    } else {
        // Connected or disconnected state - use drawable with rounded corners
        val bgDrawableRes = if (isConnected) {
            R.drawable.bg_vpn_button_connected
        } else {
            R.drawable.bg_vpn_button_disconnected
        }

        val iconRes = if (isConnected) R.drawable.ic_pause else R.drawable.ic_play_arrow
        val statusText = if (isConnected) "OFF" else "ON"

        // Outer box with border effect
        Box(
            modifier = GlanceModifier
                .size(100.dp)
                .padding(4.dp)
                .apply {
                    context?.let {
                        clickable(
                            actionStartActivity(
                                Intent(
                                    context,
                                    MainActivity::class.java
                                )
                            )
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Inner box with background drawable and rounded corners
            Box(
                modifier = GlanceModifier
                    .size(92.dp)
                    .background(ImageProvider(bgDrawableRes)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        provider = ImageProvider(iconRes),
                        contentDescription = null,
                        modifier = GlanceModifier.size(50.dp)
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = statusText,
                        style = TextStyle(
                            textAlign = TextAlign.Center,
                            color = ColorProvider(Color.White, Color.White)
                        )
                    )
                }
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