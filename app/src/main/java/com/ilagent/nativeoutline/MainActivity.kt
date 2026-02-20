package com.ilagent.nativeoutline

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ilagent.nativeoutline.data.broadcast.BroadcastVpnServiceAction
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import com.ilagent.nativeoutline.data.remote.ParseUrlOutline
import com.ilagent.nativeoutline.data.remote.RemoteJSONFetch
import com.ilagent.nativeoutline.domain.OutlineVpnManager
import com.ilagent.nativeoutline.domain.update.UpdateManager
import com.ilagent.nativeoutline.ui.MainScreen
import com.ilagent.nativeoutline.ui.UpdateDialog
import com.ilagent.nativeoutline.ui.theme.NativeOutlineTheme
import com.ilagent.nativeoutline.utils.activityresult.VPNPermissionLauncher
import com.ilagent.nativeoutline.utils.activityresult.base.launch
import com.ilagent.nativeoutline.utils.versionName
import com.ilagent.nativeoutline.viewmodel.AutoConnectViewModel
import com.ilagent.nativeoutline.viewmodel.MainViewModel
import com.ilagent.nativeoutline.viewmodel.ThemeViewModel
import com.ilagent.nativeoutline.viewmodel.state.VpnEvent
import com.ilagent.nativeoutline.viewmodel.state.VpnServerStateUi
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(
            preferencesManager = PreferencesManager(context = applicationContext),
            vpnManager = OutlineVpnManager(context = applicationContext),
            parseUrlOutline = ParseUrlOutline.Base(RemoteJSONFetch.HttpURLConnectionJSONFetch()),
            updateManager = UpdateManager.Github(context = applicationContext),
        )
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BroadcastVpnServiceAction.STARTED -> viewModel.vpnEvent(VpnEvent.STARTED)
                BroadcastVpnServiceAction.STOPPED -> viewModel.vpnEvent(VpnEvent.STOPPED)
                BroadcastVpnServiceAction.ERROR -> viewModel.vpnEvent(VpnEvent.ERROR)
            }
        }
    }

    private val themeViewModel: ThemeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ThemeViewModel(
                    preferencesManager = PreferencesManager(applicationContext)
                ) as T
            }
        }
    }

    private val autoConnectViewModel: AutoConnectViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AutoConnectViewModel(
                    preferencesManager = PreferencesManager(applicationContext)
                ) as T
            }
        }
    }


    private val vpnPermission = VPNPermissionLauncher()
    private lateinit var preferencesManager: PreferencesManager


    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            themeViewModel.isDarkTheme.collect { isDarkMode ->
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) {
                        isDarkMode
                    },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) {
                        isDarkMode
                    }
                )
            }
        }
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(applicationContext)

        vpnPermission.register(this)

        val intentFilter = IntentFilter().apply {
            addAction(BroadcastVpnServiceAction.STARTED)
            addAction(BroadcastVpnServiceAction.STOPPED)
            addAction(BroadcastVpnServiceAction.ERROR)
        }

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, intentFilter)
        }

        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            val connectionState by viewModel.vpnConnectionState.observeAsState(false)
            val vpnServerState by viewModel.vpnServerState.observeAsState(VpnServerStateUi.DEFAULT)
            val errorMessage = remember { mutableStateOf<String?>(null) }
            val context = LocalContext.current
            var showUpdateDialog by remember { mutableStateOf(false) }
            var updateDialogCancelled by rememberSaveable { mutableStateOf(false) }
            val currentVersion = remember { versionName(context) }
            var downloadProgress by remember { mutableIntStateOf(0) }
            var isDownloadingActive by remember { mutableStateOf(false) }
            var latestVersion by remember { mutableStateOf("") }

            NativeOutlineTheme(
                darkTheme = isDarkTheme,
                dynamicColor = false
            ) {
                if (showUpdateDialog) {
                    UpdateDialog(
                        onUpdate = {
                            isDownloadingActive = true
                            viewModel.updateAppToLatest(
                                onProgress = { downloadProgress = it },
                                onFinished = { showUpdateDialog = false },
                                onError = { _ ->
                                    Toast.makeText(
                                        context,
                                        R.string.update_error,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        },
                        onDismiss = {
                            showUpdateDialog = false
                            updateDialogCancelled = true
                        },
                        isDownloading = isDownloadingActive,
                        downloadProgress = downloadProgress,
                        currentVersion = currentVersion,
                        latestVersion = latestVersion
                    )
                }

                Surface {
                    MainScreen(
                        isConnected = connectionState,
                        errorEvent = viewModel.errorEvent,
                        vpnServerState = vpnServerState,
                        onConnectClick = ::startVpn,
                        onDisconnectClick = viewModel::stopVpn,
                        onSaveServer = viewModel::saveVpnServer,
                        themeViewModel = themeViewModel,
                        autoConnectViewModel = autoConnectViewModel,
                    )
                }

                errorMessage.value?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }

                if (!updateDialogCancelled) {
                    LaunchedEffect(Unit) {
                        viewModel.checkForAppUpdates(currentVersion) { newVersion ->
                            latestVersion = newVersion
                            showUpdateDialog = true
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkVpnConnectionState()
        viewModel.loadLastVpnServerState()

        val isVpnConnected = viewModel.vpnConnectionState.value == true
        val isVpnConnecting = viewModel.isConnecting.value == true
        val isAutoConnectOn = autoConnectViewModel.isAutoConnectEnabled.value
        val lastServerUrl = viewModel.vpnServerState.value?.url.orEmpty()

        if (isAutoConnectOn && !isVpnConnected && !isVpnConnecting && lastServerUrl.isNotEmpty()) {
            startVpn(lastServerUrl)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun startVpn(configString: String) {
        vpnPermission.launch {
            success = { granted ->
                if (granted) {
                    viewModel.startVpn(configString)
                } else {
                    viewModel.vpnEvent(VpnEvent.ERROR)
                }
            }
            failed = {
                runCatching {
                    val intent = Intent(android.provider.Settings.ACTION_VPN_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                viewModel.vpnEvent(VpnEvent.ERROR)
            }
        }
    }
}
