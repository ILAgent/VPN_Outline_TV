package com.ilagent.nativeoutline.domain

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ilagent.nativeoutline.MainActivity
import com.ilagent.nativeoutline.R
import com.ilagent.nativeoutline.data.broadcast.BroadcastVpnServiceAction
import com.ilagent.nativeoutline.data.model.ShadowSocksInfo
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import com.ilagent.nativeoutline.utils.CrashlyticsLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import shadowsocks.Client
import shadowsocks.Config
import shadowsocks.Shadowsocks


class OutlineVpnService : VpnService() {

    companion object {
        private const val CONFIG_EXTRA = "OutlineVpnService:config"
        private const val SOURCE_EXTRA = "OutlineVpnService:source"

        private const val TAG = "OutlineVpnService"
        private const val ACTION_START = "action.start"
        private const val ACTION_STOP = "action.stop"

        private const val NOTIFICATION_CHANNEL_ID = "outline-vpn"
        private const val NOTIFICATION_CHANNEL_NAME = "Outline"
        private const val NOTIFICATION_COLOR = 0x00BFA5
        private const val NOTIFICATION_SERVICE_ID = 1

        private lateinit var preferencesManager: PreferencesManager

        fun isVpnConnected(): Boolean {
            return VpnStateManager.isVpnConnected()
        }

        fun start(context: Context, config: ShadowSocksInfo) {
            context.startService(newIntent(context, ACTION_START).putExtra(CONFIG_EXTRA, config))
        }

        fun start(context: Context, config: ShadowSocksInfo, source: String) {
            context.startService(
                newIntent(context, ACTION_START)
                    .putExtra(CONFIG_EXTRA, config)
                    .putExtra(SOURCE_EXTRA, source)
            )
        }

        fun stop(context: Context) {
            context.startService(newIntent(context, ACTION_STOP))
        }

        fun stop(context: Context, source: String) {
            context.startService(
                newIntent(context, ACTION_STOP)
                    .putExtra(SOURCE_EXTRA, source)
            )
        }

        private fun newIntent(context: Context, action: String): Intent {
            return Intent(context, OutlineVpnService::class.java).apply { this.action = action }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var vpnTunnel: VpnTunnel

    override fun onCreate() {

        preferencesManager = PreferencesManager(applicationContext)

        Log.i(TAG, "onCreate: ")
        registerNotificationChannel()
        vpnTunnel = VpnTunnel(this, preferencesManager)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        return when {
            action == ACTION_START && !VpnStateManager.isVpnConnected() -> {
                @Suppress("DEPRECATION")
                val source = intent.extras?.getString(SOURCE_EXTRA) ?: BroadcastVpnServiceAction.SOURCE_APP
                startVpn(intent.extras?.getParcelable(CONFIG_EXTRA), source)
                START_STICKY
            }

            action == ACTION_STOP -> {
                val source = intent.extras?.getString(SOURCE_EXTRA) ?: BroadcastVpnServiceAction.SOURCE_APP
                stopVpn(source)
                START_NOT_STICKY
            }

            else -> START_STICKY
        }
    }

    private fun startVpn(config: ShadowSocksInfo?, source: String) = scope.launch(Dispatchers.IO) {
        if (config == null) {
            CrashlyticsLogger.logError("startVpn: null config")
            sendBroadcast(
                Intent(BroadcastVpnServiceAction.ERROR).setPackage(packageName)
            )
            return@launch
        }

        val ssConfig = Config().apply {
            host = config.host
            port = config.port.toLong()
            cipherName = config.method
            password = config.password
            prefix = config.prefix?.toByteArray()
        }

        val started = startVpnInternal(ssConfig)
        sendBroadcast(
            Intent(
                if (started) BroadcastVpnServiceAction.STARTED
                else BroadcastVpnServiceAction.ERROR
            ).setPackage(packageName).putExtra(BroadcastVpnServiceAction.EXTRA_SOURCE, source)
        )
    }

    private fun startVpnInternal(config: Config): Boolean {
        val isAutoStart = false

        Log.d(TAG, "startVpn: Config -> $config")

        val client = try {
            Client(config)
        } catch (e: Exception) {
            CrashlyticsLogger.logException(e, "startVpn: Invalid VPN configuration")
            return false
        }

        Log.d(TAG, "startVpn: Shadowsocks Client created")

        if (!isAutoStart) {
            try {
                val errorCode = checkServerConnectivity(client)
                if (errorCode != ErrorCode.NO_ERROR && errorCode != ErrorCode.UDP_RELAY_NOT_ENABLED) {
                    CrashlyticsLogger.logError("startVpn: Server connectivity check failed with error: $errorCode")
                    return false
                }
            } catch (e: Exception) {
                CrashlyticsLogger.logException(e, "startVpn: SHADOWSOCKS_START_FAILURE")
                return false
            }
        }

        Log.d(TAG, "startVpn: Establishing VPN tunnel...")

        if (!vpnTunnel.establishVpn()) {
            Log.i(TAG, "startVpn: Failed to establish the VPN")
            return false
        }

        val remoteUdpForwardingEnabled = false
        try {
            vpnTunnel.connectTunnel(client, remoteUdpForwardingEnabled)
            VpnStateManager.setVpnRunning(true)
            Log.i(TAG, "startVpn: VPN tunnel established successfully")
            startForegroundWithNotification()
        } catch (e: Exception) {
            CrashlyticsLogger.logException(e, "startVpn: Failed to connect the tunnel")
            VpnStateManager.setVpnRunning(false)
        }

        return VpnStateManager.isVpnConnected()
    }

    private fun stopVpn(source: String) {
        stopVpnTunnel()
        stopForeground()
        stopSelf()
        VpnStateManager.setVpnRunning(false)

        sendBroadcast(
            Intent(BroadcastVpnServiceAction.STOPPED)
                .setPackage(packageName)
                .putExtra(BroadcastVpnServiceAction.EXTRA_SOURCE, source)
        )
    }

    private fun checkServerConnectivity(client: Client): ErrorCode {
        return try {
            val errorCode = Shadowsocks.checkConnectivity(client)
            val result: ErrorCode = ErrorCode.entries[errorCode.toInt()]
            Log.i(TAG, "checkServerConnectivity: Go connectivity check result: ${result.name}")
            result
        } catch (e: Exception) {
            CrashlyticsLogger.logException(e, "checkServerConnectivity: Connectivity checks failed")
            ErrorCode.UNEXPECTED
        }
    }

    private fun startForegroundWithNotification() {
        try {
            val notification: Notification = createNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_SERVICE_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                )
            } else {
                startForeground(NOTIFICATION_SERVICE_ID, notification)
            }
        } catch (e: java.lang.Exception) {
            CrashlyticsLogger.logException(e, "startForegroundWithNotification: Unable to display persistent notification")
        }
    }

    private fun registerNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setSilent(true)
            .setColor(NOTIFICATION_COLOR)
            .setContentTitle(getString(R.string.vpn_name))
            .setContentText(getString(R.string.vpn_connected))
            .addAction(0, getString(R.string.stop_vpn),
                PendingIntent.getService(
                    this,
                    0,
                    Intent(this, OutlineVpnService::class.java).setAction(ACTION_STOP),
                    PendingIntent.FLAG_IMMUTABLE,
                )
            )
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }

    // Plugin error codes. Keep in sync with www/model/errors.ts.
    enum class ErrorCode(val value: Int) {
        NO_ERROR(0),
        UNEXPECTED(1),
        VPN_PERMISSION_NOT_GRANTED(2),
        INVALID_SERVER_CREDENTIALS(3),
        UDP_RELAY_NOT_ENABLED(4),
        SERVER_UNREACHABLE(5),
        VPN_START_FAILURE(6),
        ILLEGAL_SERVER_CONFIGURATION(7),
        SHADOWSOCKS_START_FAILURE(8),
        CONFIGURE_SYSTEM_PROXY_FAILURE(9),
        NO_ADMIN_PERMISSIONS(10),
        UNSUPPORTED_ROUTING_TABLE(11),
        SYSTEM_MISCONFIGURED(12)
    }

    private fun stopVpnTunnel() {
        vpnTunnel.disconnectTunnel()
        vpnTunnel.tearDownVpn()
    }

    private fun stopForeground() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onRevoke() {
        super.onRevoke()
        Log.i(TAG, "onRevoke: ")
        stopVpnTunnel()
        stopForeground()
        VpnStateManager.setVpnRunning(false)
        sendBroadcast(Intent(BroadcastVpnServiceAction.STOPPED).setPackage(packageName))
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: ")
        VpnStateManager.setVpnRunning(false)
    }

    fun newBuilder(): Builder {
        return Builder()
    }

    @Throws(PackageManager.NameNotFoundException::class)
    fun getApplicationName(): String {
        return getString(R.string.app_name)
    }


}