package com.ilagent.nativeoutline.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilagent.nativeoutline.R
import com.ilagent.nativeoutline.viewmodel.state.VpnServerStateUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnConnectButton(
    isConnected: Boolean,
    isConnectionLoading: Boolean,
    isEditing: Boolean,
    vpnServerState: VpnServerStateUi,
    onOpenServerDialog: () -> Unit,
    onConnectionLoading: () -> Unit,
    onDisconnectClick: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    val context = LocalContext.current
    val connectButtonInteractionSource = remember { MutableInteractionSource() }
    val isConnectButtonFocused by connectButtonInteractionSource.collectIsFocusedAsState()

    if (isConnectionLoading) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(120.dp)
                .padding(20.dp)
        )
    } else {
        Box(
            modifier = Modifier
                .size(120.dp)
                .border(
                    width = 3.dp,
                    color = if (isConnectButtonFocused)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    else
                        Color.Transparent,
                    shape = RoundedCornerShape(34.dp)
                )
                .padding(4.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isConnected) {
                            listOf(
                                Color(0xFF5EFFB5),
                                Color(0xFF2C7151)
                            )
                        } else {
                            listOf(
                                Color(0xFFE57373),
                                Color(0xFFFF8A65)
                            )
                        }
                    )
                )
                .focusable(interactionSource = connectButtonInteractionSource)
                .clickable(
                    interactionSource = connectButtonInteractionSource,
                    indication = ripple(true)
                ) {
                    if (!isEditing) {
                        if (vpnServerState == VpnServerStateUi.DEFAULT) {
                            onOpenServerDialog()
                        } else {
                            onConnectionLoading()
                            if (isConnected) {
                                onDisconnectClick()
                            } else {
                                onRequestPermission()
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Crossfade(
                    targetState = isConnected,
                    animationSpec = tween(600),
                    label = "ConnectionStatusCrossfade"
                ) { connected ->
                    Icon(
                        imageVector = if (connected) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }
                Text(
                    text = context.getString(if (isConnected) R.string.off else R.string.on),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun VpnConnectButtonPreview() {
    VpnConnectButton(
        isConnected = false,
        isConnectionLoading = false,
        isEditing = false,
        vpnServerState = VpnServerStateUi(
            name = "Test Server",
            host = "test.example.com",
            url = "ss://test",
            startTime = 0
        ),
        onOpenServerDialog = {},
        onConnectionLoading = {},
        onDisconnectClick = {},
        onRequestPermission = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun VpnConnectButtonConnectedPreview() {
    VpnConnectButton(
        isConnected = true,
        isConnectionLoading = false,
        isEditing = false,
        vpnServerState = VpnServerStateUi(
            name = "Test Server",
            host = "test.example.com",
            url = "ss://test",
            startTime = 0
        ),
        onOpenServerDialog = {},
        onConnectionLoading = {},
        onDisconnectClick = {},
        onRequestPermission = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun VpnConnectButtonLoadingPreview() {
    VpnConnectButton(
        isConnected = false,
        isConnectionLoading = true,
        isEditing = false,
        vpnServerState = VpnServerStateUi(
            name = "Test Server",
            host = "test.example.com",
            url = "ss://test",
            startTime = 0
        ),
        onOpenServerDialog = {},
        onConnectionLoading = {},
        onDisconnectClick = {},
        onRequestPermission = {}
    )
}