package com.ilagent.nativeoutline.ui

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ilagent.nativeoutline.R
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import com.ilagent.nativeoutline.utils.CrashlyticsLogger
import com.ilagent.nativeoutline.utils.versionNameUi
import com.ilagent.nativeoutline.viewmodel.AutoConnectViewModel
import com.ilagent.nativeoutline.viewmodel.DnsViewModel
import com.ilagent.nativeoutline.viewmodel.LanguageViewModel
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
    onSelectServer: (String, String) -> Unit,
    onClearSelectedServer: () -> Unit,
    themeViewModel: ThemeViewModel,
    autoConnectViewModel: AutoConnectViewModel,
    languageViewModel: LanguageViewModel,
    dnsViewModel: DnsViewModel
) {
    val errorMessage by remember { mutableStateOf<String?>(null) }
    var elapsedTime by remember { mutableIntStateOf(0) }
    val isEditing by remember { mutableStateOf(false) }
    var isServerListDialogOpen by remember { mutableStateOf(false) }
    var isAddServerDialogOpen by remember { mutableStateOf(false) }
    var addServerInitialAction by remember { mutableStateOf<String?>(null) }
    var isSettingsDialogOpen by remember { mutableStateOf(false) }
    var isHelpDialogOpen by remember { mutableStateOf(false) }
    var isConnectionLoading by remember { mutableStateOf(false) }
    var serverListRefreshTrigger by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val preferencesManager = remember { PreferencesManager(context) }

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
        when (LocalConfiguration.current.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> MainScreenContent(
                onHelpClick = { isHelpDialogOpen = true },
                onSettingsClick = {
                    CrashlyticsLogger.logSettingsOpened()
                    isSettingsDialogOpen = true
                },
                vpnServerState = vpnServerState,
                onConnectClick = onConnectClick,
                onDisconnectClick = onDisconnectClick,
                onOpenServerDialog = {
                    CrashlyticsLogger.logServerDialogOpened()
                    if (preferencesManager.getVpnKeys().isEmpty()) {
                        addServerInitialAction = null
                        isAddServerDialogOpen = true
                    } else {
                        isServerListDialogOpen = true
                    }
                },
                isConnected = isConnected,
                isConnectionLoading = isConnectionLoading,
                isEditing = isEditing,
                elapsedTime = elapsedTime,
                errorMessage = errorMessage,
                preferencesManager = preferencesManager,
                onConnectionLoading = { isConnectionLoading = true }
            )

            else -> TvScreenContent(
                onHelpClick = { isHelpDialogOpen = true },
                onSettingsClick = {
                    CrashlyticsLogger.logSettingsOpened()
                    isSettingsDialogOpen = true
                },
                vpnServerState = vpnServerState,
                onConnectClick = onConnectClick,
                onDisconnectClick = onDisconnectClick,
                onOpenServerDialog = {
                    CrashlyticsLogger.logServerDialogOpened()
                    if (preferencesManager.getVpnKeys().isEmpty()) {
                        addServerInitialAction = null
                        isAddServerDialogOpen = true
                    } else {
                        isServerListDialogOpen = true
                    }
                },
                isConnected = isConnected,
                isConnectionLoading = isConnectionLoading,
                isEditing = isEditing,
                elapsedTime = elapsedTime,
                errorMessage = errorMessage,
                preferencesManager = preferencesManager,
                onConnectionLoading = { isConnectionLoading = true }
            )

        }
        
        // Server list dialog - shown when clicking on server selection
        if (isServerListDialogOpen) {
            ServerListDialog(
                currentServerName = vpnServerState.name,
                currentServerKey = vpnServerState.url,
                preferencesManager = preferencesManager,
                onDismiss = { isServerListDialogOpen = false },
                onSelectServer = { serverInfo ->
                    onSelectServer(serverInfo.name, serverInfo.key)
                },
                onClearSelectedServer = {
                    onClearSelectedServer()
                },
                onAddServerClick = { action ->
                    addServerInitialAction = action
                    isAddServerDialogOpen = true
                },
                refreshTrigger = serverListRefreshTrigger
            )
        }

        // Add server dialog - shown when clicking "Add" button
        if (isAddServerDialogOpen) {
            AddServerDialog(
                onDismiss = {
                    isAddServerDialogOpen = false
                    addServerInitialAction = null
                },
                onSave = { name, key ->
                    onSaveServer(name, key)
                    serverListRefreshTrigger++
                    isAddServerDialogOpen = false
                    addServerInitialAction = null
                },
                initialAction = addServerInitialAction
            )
        }

        if (isSettingsDialogOpen) {
            SettingsDialog(
                onDismiss = { isSettingsDialogOpen = false },
                preferencesManager = preferencesManager,
                onDnsSelected = {},
                themeViewModel = themeViewModel,
                autoConnectViewModel = autoConnectViewModel,
                languageViewModel = languageViewModel,
                dnsViewModel = dnsViewModel
            )
        }

        if (isHelpDialogOpen) {
            HelpDialog(
                onDismiss = { isHelpDialogOpen = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    onHelpClick: () -> Unit,
    onSettingsClick: () -> Unit,
    vpnServerState: VpnServerStateUi,
    onConnectClick: (String) -> Unit,
    onDisconnectClick: () -> Unit,
    onOpenServerDialog: () -> Unit,
    isConnected: Boolean,
    isConnectionLoading: Boolean,
    isEditing: Boolean,
    elapsedTime: Int,
    errorMessage: String?,
    preferencesManager: PreferencesManager,
    onConnectionLoading: () -> Unit,
) {
    val context = LocalContext.current
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
                        text = context.getString(R.string.version, versionNameUi(context)),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            },
            actions = {
                IconButton(onClick = onHelpClick) {
                    Icon(
                        imageVector = Icons.Filled.Quiz,
                        contentDescription = "Open Question",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onSettingsClick) {
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
                    onOpenServerDialog()
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.disconnect_before_settings),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            },
        )
        Box {
            var requestPermission by remember { mutableStateOf(false) }
            NotificationPermission(requestPermission) {
                onConnectClick(vpnServerState.url)
                requestPermission = false
            }
            VpnConnectButton(
                isConnected = isConnected,
                isConnectionLoading = isConnectionLoading,
                isEditing = isEditing,
                vpnServerState = vpnServerState,
                onOpenServerDialog = onOpenServerDialog,
                onConnectionLoading = onConnectionLoading,
                onDisconnectClick = onDisconnectClick,
                onRequestPermission = { requestPermission = true }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (isConnected) {
            val time = String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                elapsedTime / 3600,
                (elapsedTime % 3600) / 60,
                elapsedTime % 60
            )
            Text(
                text = context.getString(R.string.elapsed_time).format(time),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxSize()) {
            WhiteList(preferencesManager)
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvScreenContent(
    onHelpClick: () -> Unit,
    onSettingsClick: () -> Unit,
    vpnServerState: VpnServerStateUi,
    onConnectClick: (String) -> Unit,
    onDisconnectClick: () -> Unit,
    onOpenServerDialog: () -> Unit,
    isConnected: Boolean,
    isConnectionLoading: Boolean,
    isEditing: Boolean,
    elapsedTime: Int,
    errorMessage: String?,
    preferencesManager: PreferencesManager,
    onConnectionLoading: () -> Unit,
) {
    val context = LocalContext.current
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
                        text = context.getString(R.string.version, versionNameUi(context)),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            },
            actions = {
                IconButton(onClick = onHelpClick) {
                    Icon(
                        imageVector = Icons.Filled.Quiz,
                        contentDescription = "Open Question",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onSettingsClick) {
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
                    onOpenServerDialog()
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.disconnect_before_settings),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            },
        )


        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.5f)
            ) {

                Box {
                    var requestPermission by remember { mutableStateOf(false) }
                    NotificationPermission(requestPermission) {
                        onConnectClick(vpnServerState.url)
                        requestPermission = false
                    }
                    VpnConnectButton(
                        isConnected = isConnected,
                        isConnectionLoading = isConnectionLoading,
                        isEditing = isEditing,
                        vpnServerState = vpnServerState,
                        onOpenServerDialog = onOpenServerDialog,
                        onConnectionLoading = onConnectionLoading,
                        onDisconnectClick = onDisconnectClick,
                        onRequestPermission = { requestPermission = true }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isConnected) {
                    val time = String.format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d",
                        elapsedTime / 3600,
                        (elapsedTime % 3600) / 60,
                        elapsedTime % 60
                    )
                    Text(
                        text = context.getString(R.string.elapsed_time).format(time),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                WhiteList(preferencesManager)
            }
        }
    }
}

@Preview(name = "Default")
@Preview(name = "tv", device = "id:tv_1080p")
@Preview(name = "tablet", device = "id:7in WSVGA (Tablet)")
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
        onSelectServer = { _, _ -> },
        onClearSelectedServer = {},
        themeViewModel = ThemeViewModel(PreferencesManager(LocalContext.current)),
        autoConnectViewModel = AutoConnectViewModel(PreferencesManager(LocalContext.current)),
        languageViewModel = LanguageViewModel(LocalContext.current.applicationContext as android.app.Application),
        dnsViewModel = DnsViewModel(LocalContext.current.applicationContext as android.app.Application)
    )
}