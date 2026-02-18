package com.ilagent.nativeoutline.ui

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ilagent.nativeoutline.BuildConfig
import com.ilagent.nativeoutline.BuildConfig.VERSION_NAME
import com.ilagent.nativeoutline.R
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import com.ilagent.nativeoutline.utils.versionName
import com.ilagent.nativeoutline.viewmodel.AutoConnectViewModel
import com.ilagent.nativeoutline.viewmodel.ThemeViewModel
import com.ilagent.nativeoutline.viewmodel.state.SingleLiveEvent
import com.ilagent.nativeoutline.viewmodel.state.VpnServerStateUi
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isConnected: Boolean,
    errorEvent: LiveData<Unit>,
    vpnServerState: VpnServerStateUi,
    onConnectClick: (String) -> Unit,
    onDisconnectClick: () -> Unit,
    onSaveServer: (String, String) -> Unit,
    themeViewModel: ThemeViewModel,
    autoConnectViewModel: AutoConnectViewModel
) {
    val errorMessage by remember { mutableStateOf<String?>(null) }
    var elapsedTime by remember { mutableIntStateOf(0) }
    val isEditing by remember { mutableStateOf(false) }
    var isDialogOpen by remember { mutableStateOf(false) }
    var isSettingsDialogOpen by remember { mutableStateOf(false) }
    var isHelpDialogOpen by remember { mutableStateOf(false) }
    var isConnectionLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val preferencesManager = remember { PreferencesManager(context) }

    val connectButtonInteractionSource = remember { MutableInteractionSource() }

    val isConnectButtonFocused by connectButtonInteractionSource.collectIsFocusedAsState()

    LaunchedEffect(Unit) {
        errorEvent.observe(lifecycleOwner) {
            isConnectionLoading = false
            Toast.makeText(
                context,
                context.getString(R.string.vpn_start_failed),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }


    LaunchedEffect(isConnected, vpnServerState.startTime) {
        isConnectionLoading = false
        while (isConnected) {
            delay(1000L)
            elapsedTime = ((System.currentTimeMillis() - vpnServerState.startTime) / 1000).toInt()
        }
        elapsedTime = 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = context.getString(R.string.version, versionName(context)),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isHelpDialogOpen = true }) {
                        Icon(
                            imageVector = Icons.Filled.Quiz,
                            contentDescription = "Open Question",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { isSettingsDialogOpen = true }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Open Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            ServerItem(
                serverName = vpnServerState.name,
                serverHost = vpnServerState.host,
                onForwardIconClick = {
                    if (!isConnected && !isConnectionLoading) {
                        isDialogOpen = true
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.disconnect_before_settings),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
            )

            if (isDialogOpen) {
                ServerDialog(
                    currentName = vpnServerState.name,
                    currentKey = vpnServerState.url,
                    onDismiss = { isDialogOpen = false },
                    onSave = { name, key ->
                        onSaveServer(name, key)
                        isDialogOpen = false
                    },
                )
            }

            if (isSettingsDialogOpen) {
                SettingsDialog(
                    onDismiss = { isSettingsDialogOpen = false },
                    preferencesManager = preferencesManager,
                    onDnsSelected = {},
                    themeViewModel = themeViewModel,
                    autoConnectViewModel = autoConnectViewModel
                )
            }

            if (isHelpDialogOpen) {
                HelpDialog(
                    onDismiss = { isHelpDialogOpen = false }
                )
            }

            Spacer(modifier = Modifier.height(15.dp))
            Box {
                var requestPermission by remember { mutableStateOf(false) }
                NotificationPermission(requestPermission) {
                    onConnectClick(vpnServerState.url)
                }
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
                                        isDialogOpen = true
                                    } else {
                                        isConnectionLoading = true
                                        if (isConnected) {
                                            requestPermission = false
                                            onDisconnectClick()
                                        } else {
                                            requestPermission = true
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

            Spacer(modifier = Modifier.height(16.dp))

            if (isConnected) {
                Text(
                    text = context.getString(R.string.elapsed_time),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = String.format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d",
                        elapsedTime / 3600,
                        (elapsedTime % 3600) / 60,
                        elapsedTime % 60
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }

            errorMessage?.let { _message ->
                Text(
                    text = _message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red
                )
            }
            Box(modifier = Modifier.fillMaxSize()) {
                WhiteList(preferencesManager)
            }
        }

        if (BuildConfig.DEBUG) {
            val appInfo = """
    🔧 ${VERSION_NAME} (${BuildConfig.VERSION_CODE})
    📝 ${BuildConfig.COMMIT_HASH}
    ⏱️ ${BuildConfig.COMMIT_TIME}
    🏗️ ${BuildConfig.BUILD_TIME}
    🌿 ${BuildConfig.BRANCH}
""".trimIndent()
            Text(
                appInfo,
                modifier = Modifier.align(Alignment.BottomStart),
                textAlign = TextAlign.Start, fontSize = 8.sp
            )
        }
    }
}

@Preview(name = "Default", showBackground = true)
@Preview(name = "TV", widthDp = 1920, heightDp = 1080)
@Composable
fun DefaultPreview() {
    MainScreen(
        isConnected = false,
        errorEvent = SingleLiveEvent(),
        vpnServerState = VpnServerStateUi(
            name = "Server #1",
            host = "172.66.44.135:80",
            url = "ss://5df7962e-f9fe-41e6-ab49-ed96ccb856a7@172.66.44.135:80?path=%2F&security=none&encryption=none&host=v2ra1.ecrgpk.workers.dev&type=ws#United States%20#1269%20/%20OutlineKeys.com"
        ),
        onConnectClick = { _ -> },
        onDisconnectClick = {},
        onSaveServer = { _, _ -> },
        themeViewModel = ThemeViewModel(PreferencesManager(LocalContext.current)),
        autoConnectViewModel = AutoConnectViewModel(PreferencesManager(LocalContext.current))
    )
}
