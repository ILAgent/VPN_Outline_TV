package com.ilagent.nativeoutline.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilagent.nativeoutline.R
import com.ilagent.nativeoutline.data.remote.TvLocalServer
import com.ilagent.nativeoutline.data.model.genToken
import com.ilagent.nativeoutline.data.model.getLocalIp
import com.ilagent.nativeoutline.data.model.makeQr
import com.ilagent.nativeoutline.data.model.toImageBitmap
import fi.iki.elonen.NanoHTTPD
import java.net.BindException

@Composable
fun PairByQrScreen(
    onKeyReady: (String) -> Unit
) {
    val ctx = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var token by remember { mutableStateOf(genToken()) }
    var url by remember { mutableStateOf<String?>(null) }
    var qr by remember { mutableStateOf<ImageBitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    var server by remember { mutableStateOf<TvLocalServer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { server?.stop() }
            server = null
        }
    }

    LaunchedEffect(token, isPreview) {
        error = null
        url = null
        qr = null

        if (isPreview) {
            val demoIp = "192.168.0.10"
            val demoPort = 12345
            val link = "http://$demoIp:$demoPort/session/$token"
            url = link
            qr = makeQr(link).toImageBitmap()
            return@LaunchedEffect
        }

        val ip = ctx.getLocalIp()
        if (ip == null) {
            error = ctx.getString(R.string.error_ip_not_found)
            return@LaunchedEffect
        }

        runCatching { server?.stop() }
        server = null

        val srv = TvLocalServer(
            appContext = ctx.applicationContext,
            port = 0,
            token = token,
            onKeyReceived = { key ->
                runCatching { server?.stop() }
                server = null
                onKeyReady(key)
            },
            preferClientLanguage = false
        )


        try {
            srv.start(NanoHTTPD.SOCKET_READ_TIMEOUT, /* daemon = */ false)
            val actualPort = srv.listeningPort
            if (actualPort <= 0) {
                srv.stop()
                error = ctx.getString(R.string.error_port_unavailable)
                return@LaunchedEffect
            }

            server = srv
            val link = "http://$ip:$actualPort/session/$token"
            url = link
            qr = makeQr(link).toImageBitmap()
        } catch (e: BindException) {
            runCatching { srv.stop() }
            server = null
            error = ctx.getString(R.string.error_port_in_use)
            token = genToken()
        } catch (t: Throwable) {
            runCatching { srv.stop() }
            server = null
            error = ctx.getString(R.string.error_server_start_failed, t.message ?: "unknown")
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .selectableGroup(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.scan_the_qr_code_from_your),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    qr?.let {
                        Image(
                            bitmap = it,
                            contentDescription = null,
                            modifier = Modifier.size(260.dp)
                        )
                    } ?: run {
                        Text(
                            text = stringResource(id = R.string.qr_code_generation),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            url?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { token = genToken() },
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.Generate_new_qr_code),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Preview()
@Composable
private fun PairByQrScreenPreview() {
    PairByQrScreen(onKeyReady = { })
}