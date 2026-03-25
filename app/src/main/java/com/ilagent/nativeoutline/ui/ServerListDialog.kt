package com.ilagent.nativeoutline.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ilagent.nativeoutline.R
import com.ilagent.nativeoutline.data.model.VpnServerInfo
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import com.ilagent.nativeoutline.data.remote.IpCountryCodeProvider
import com.ilagent.nativeoutline.data.remote.ParseUrlOutline
import com.ilagent.nativeoutline.data.remote.RemoteJSONFetch
import com.ilagent.nativeoutline.data.remote.ServerIconProvider
import com.ilagent.nativeoutline.utils.CrashlyticsLogger

/**
 * Dialog for selecting a VPN server from the saved list.
 * Shows a list of saved servers with flag icons and delete buttons,
 * and has a button to add a new server.
 */
@Composable
fun ServerListDialog(
    currentServerName: String,
    currentServerKey: String,
    preferencesManager: PreferencesManager,
    onDismiss: () -> Unit,
    onSelectServer: (VpnServerInfo) -> Unit,
    onClearSelectedServer: () -> Unit,
    onAddServerClick: (String?) -> Unit,
    refreshTrigger: Int = 0
) {
    val context = LocalContext.current
    var savedVpnKeys by remember { mutableStateOf(preferencesManager.getVpnKeys()) }

    // Refresh server list when refreshTrigger changes
    LaunchedEffect(refreshTrigger) {
        savedVpnKeys = preferencesManager.getVpnKeys()
    }

    // Log dialog opening
    LaunchedEffect(Unit) {
        CrashlyticsLogger.logServerListDialogOpened()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.select_server),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (savedVpnKeys.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_saved_servers),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(savedVpnKeys) { server ->
                            val isSelected =
                                server.name == currentServerName && server.key == currentServerKey

                            ServerListItem(
                                serverName = server.name,
                                serverKey = server.key,
                                isSelected = isSelected,
                                preferencesManager = preferencesManager,
                                onSelect = {
                                    CrashlyticsLogger.logServerSelected(server.name)
                                    onSelectServer(server)
                                    onDismiss()
                                },
                                onDelete = {
                                    val newSelectedServer = preferencesManager.deleteVpnKey(server.name)
                                    CrashlyticsLogger.logServerDeleted(server.name)
                                    savedVpnKeys = preferencesManager.getVpnKeys()
                                    
                                    // Если был выбран удалённый сервер
                                    if (server.name == currentServerName) {
                                        if (newSelectedServer != null) {
                                            // Выбираем первый сервер из списка
                                            onSelectServer(newSelectedServer)
                                        } else {
                                            // Если список пуст, очищаем выбранный сервер
                                            onClearSelectedServer()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(id = R.string.cancel))
                }
                TextButton(onClick = {
                    CrashlyticsLogger.logAddServerButtonClicked()
                    onAddServerClick(null)
                }) {
                    Text(stringResource(id = R.string.add_server))
                }
            }
        }
    )
}

@Composable
private fun ServerListItem(
    serverName: String,
    serverKey: String,
    isSelected: Boolean,
    preferencesManager: PreferencesManager,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    // Extract host from key for loading flag
    val serverHost = remember(serverKey) {
        try {
            val parseUrlOutline = ParseUrlOutline.Base()
            parseUrlOutline.extractServerHost(serverKey) ?: ""
        } catch (e: Exception) {
            CrashlyticsLogger.logException(e, "Failed to extract host from server key")
            ""
        }
    }

    // Load flag URL
    var flagUrl by remember(serverHost) { mutableStateOf(preferencesManager.getFlagUrl(serverHost)) }

    LaunchedEffect(serverHost) {
        if (flagUrl == null && serverHost.isNotEmpty()) {
            try {
                val iconProvider = ServerIconProvider.FlagsApiDotCom(
                    ipCountryCodeProvider = IpCountryCodeProvider.IpApiDotCo(
                        fetch = RemoteJSONFetch.HttpURLConnectionJSONFetch()
                    ),
                    preferencesManager = preferencesManager
                )
                flagUrl = iconProvider.icon(serverHost)
            } catch (e: Exception) {
                CrashlyticsLogger.logException(e, "Failed to load flag for host: $serverHost")
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clickable { onSelect() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flag icon
        if (flagUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(flagUrl)
                    .listener(
                        onError = { _, result ->
                            CrashlyticsLogger.logException(
                                result.throwable,
                                "Failed to load server flag: $flagUrl"
                            )
                        }
                    )
                    .build(),
                contentDescription = "Server flag",
                modifier = Modifier
                    .size(48.dp)
                    .padding(all = 4.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.FillBounds,
            )
        } else {
            // Placeholder when no flag
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(all = 4.dp)
            )
        }

        // Server name
        Text(
            text = serverName.ifEmpty { context.getString(R.string.server_not_specified) },
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        // Delete button
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(id = R.string.remove),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Preview
@Composable
fun ServerListDialogPreview() {
    ServerListDialog(
        currentServerName = "Server #1",
        currentServerKey = "ss://test@127.0.0.1:8080",
        preferencesManager = PreferencesManager(LocalContext.current),
        onDismiss = {},
        onSelectServer = {},
        onClearSelectedServer = {},
        onAddServerClick = { },
        refreshTrigger = 0
    )
}
